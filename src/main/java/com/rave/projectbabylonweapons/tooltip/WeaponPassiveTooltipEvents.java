package com.rave.projectbabylonweapons.tooltip;
import com.mojang.datafixers.util.Either;
import com.rave.projectbabylonmaterials.tooltip.DescriptionBoxTooltipData;
import com.rave.projectbabylonmaterials.tooltip.IconLabelTooltipData;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.List;
@EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public final class WeaponPassiveTooltipEvents {
    private WeaponPassiveTooltipEvents() {
    }
    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        WeaponPassiveTooltipData data = WeaponPassiveTooltipResolver.resolve(event.getItemStack());
        if (data == null) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        if (containsPassiveTooltip(tooltip, data)) {
            return;
        }
        data.appendTooltip(tooltip);
    }
    @SubscribeEvent
    public static void onGatherTooltipComponents(RenderTooltipEvent.GatherComponents event) {
        if (!Screen.hasControlDown()) {
            return;
        }
        WeaponPassiveTooltipData data = WeaponPassiveTooltipResolver.resolve(event.getItemStack());
        if (data == null) {
            return;
        }
        List<Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>> elements = event.getTooltipElements();
        replaceTitleLine(elements, data);
        replaceDescriptionLines(elements, data);
    }
    private static boolean containsPassiveTooltip(List<Component> tooltip, WeaponPassiveTooltipData data) {
        String titleLine = data.titleLine().getString();
        String collapsedLine = data.collapsedLine().getString();
        String descriptionHeader = data.descriptionHeader().getString();
        for (Component line : tooltip) {
            String text = line.getString();
            if (text.equals(titleLine) || text.equals(collapsedLine) || text.equals(descriptionHeader)) {
                return true;
            }
        }
        return false;
    }
    private static void replaceTitleLine(List<Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>> elements,
                                         WeaponPassiveTooltipData data) {
        String titleLine = data.titleLine().getString();
        for (int i = 0; i < elements.size(); i++) {
            Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent> element = elements.get(i);
            if (element.left().isEmpty()) {
                continue;
            }
            String line = element.left().get().getString();
            if (line.equals(titleLine)) {
                elements.set(i, Either.right(new IconLabelTooltipData(data.titleLine(), data.frameTexture(), data.iconTexture())));
                return;
            }
        }
    }
    private static void replaceDescriptionLines(List<Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>> elements,
                                                WeaponPassiveTooltipData data) {
        if (data.descriptionFrameStyle() == null || data.descriptionLines().isEmpty()) {
            return;
        }
        int firstDescriptionIndex = -1;
        String firstLine = data.descriptionLines().get(0).getString();
        for (int i = 0; i < elements.size(); i++) {
            Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent> element = elements.get(i);
            if (element.left().isEmpty()) {
                continue;
            }
            if (element.left().get().getString().equals(firstLine)) {
                firstDescriptionIndex = i;
                break;
            }
        }
        if (firstDescriptionIndex < 0) {
            return;
        }
        elements.set(firstDescriptionIndex, Either.right(new DescriptionBoxTooltipData(data.descriptionLines(), data.descriptionFrameStyle())));
        for (int i = data.descriptionLines().size() - 1; i >= 1; i--) {
            int removeIndex = firstDescriptionIndex + i;
            if (removeIndex < elements.size()) {
                elements.remove(removeIndex);
            }
        }
    }
}