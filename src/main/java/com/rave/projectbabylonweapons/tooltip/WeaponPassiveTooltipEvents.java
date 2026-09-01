package com.rave.projectbabylonweapons.tooltip;
import com.mojang.datafixers.util.Either;
import com.rave.projectbabylonmaterials.tooltip.DescriptionBoxTooltipData;
import com.rave.projectbabylonmaterials.tooltip.IconLabelTooltipData;
import com.rave.projectbabylonweapons.ProjectBabylonWeapons;
import com.rave.projectbabylonweapons.item.special.ArclightSwordItem;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.List;
@Mod.EventBusSubscriber(modid = ProjectBabylonWeapons.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        List<Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>> elements = event.getTooltipElements();
        replaceEvergateName(event.getItemStack(), elements);
        if (!Screen.hasControlDown()) {
            return;
        }
        WeaponPassiveTooltipData data = WeaponPassiveTooltipResolver.resolve(event.getItemStack());
        if (data == null) {
            return;
        }
        replaceTitleLine(elements, data);
        replaceDescriptionLines(elements, data);
    }

    private static void replaceEvergateName(net.minecraft.world.item.ItemStack stack,
                                             List<Either<net.minecraft.network.chat.FormattedText, net.minecraft.world.inventory.tooltip.TooltipComponent>> elements) {
        if (!ArclightSwordItem.isEvergate(stack)) {
            return;
        }
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).left().isPresent()) {
                elements.set(i, Either.right(new EvergateNameTooltipData(stack.getHoverName())));
                return;
            }
        }
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