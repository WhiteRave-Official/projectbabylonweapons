package com.rave.projectbabylonweapons.client.tooltip;

import com.rave.projectbabylonweapons.tooltip.EvergateNameTooltipData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.joml.Matrix4f;

public final class EvergateNameClientTooltip implements ClientTooltipComponent {
    private static final int FULL_BRIGHT = 15728880;
    private final EvergateNameTooltipData data;

    public EvergateNameClientTooltip(EvergateNameTooltipData data) {
        this.data = data;
    }

    @Override
    public int getHeight() {
        return 12;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(data.name());
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix, MultiBufferSource.BufferSource bufferSource) {
        String name = data.name().getString();
        float time = (System.nanoTime() % 4_000_000_000L) / 1_000_000_000.0F;
        float cursor = x;

        for (int index = 0; index < name.length(); index++) {
            String glyph = String.valueOf(name.charAt(index));
            FormattedCharSequence sequence = Component.literal(glyph).getVisualOrderText();
            float wave = (float) Math.sin(time * 4.2F + index * 0.7F);
            float glyphY = y + 2.0F + wave * 1.25F;
            int color = shimmerColor(wave);

            font.drawInBatch(sequence, cursor - 0.6F, glyphY, 0x5068D8FF, false, matrix,
                    bufferSource, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            font.drawInBatch(sequence, cursor + 0.6F, glyphY, 0x5068D8FF, false, matrix,
                    bufferSource, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            font.drawInBatch(sequence, cursor, glyphY - 0.6F, 0x5098E8FF, false, matrix,
                    bufferSource, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            font.drawInBatch(sequence, cursor, glyphY, color, true, matrix,
                    bufferSource, Font.DisplayMode.NORMAL, 0, FULL_BRIGHT);
            cursor += font.width(glyph);
        }
    }

    private static int shimmerColor(float wave) {
        float blend = wave * 0.5F + 0.5F;
        int red = 210 + Math.round(45.0F * blend);
        int green = 225 + Math.round(30.0F * blend);
        int blue = 255;
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}