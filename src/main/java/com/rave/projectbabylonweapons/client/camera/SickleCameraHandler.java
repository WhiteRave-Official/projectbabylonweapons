package com.rave.projectbabylonweapons.client.camera;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.lang.reflect.Method;
import java.util.UUID;

@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.GAME)
public class SickleCameraHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    private static double fadeStrength = 0.0;
    private static final double FADE_SPEED = 0.3;
    private static final double FADE_EPS = 0.001;


    private static Method setPositionMethod = null;
    private static boolean setPositionMethodInitTried = false;

    private static Method getSetPositionMethod() {
        if (setPositionMethod != null) return setPositionMethod;
        if (setPositionMethodInitTried) return null;
        setPositionMethodInitTried = true;
        try {
            Method m = Camera.class.getDeclaredMethod("setPosition", double.class, double.class, double.class);
            m.setAccessible(true);
            setPositionMethod = m;
            return m;
        } catch (Exception e) {
            LOGGER.warn("SickleCameraHandler: failed to cache Camera.setPosition via reflection", e);
            return null;
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.getCameraType().isFirstPerson()) {
            return;
        }

        UUID playerUUID = mc.player.getUUID();

        boolean isCharging = com.rave.projectbabylonweapons.skill.weapon_innate.SickleThrowSkill.isCharging(playerUUID);

        double targetFade = isCharging ? 1.0 : 0.0;
        fadeStrength = lerp(fadeStrength, targetFade, FADE_SPEED);
        fadeStrength = Mth.clamp((float)fadeStrength, 0.0f, 1.0f);

        if (fadeStrength <= FADE_EPS) return;

        Camera camera = event.getCamera();

        float yRot = mc.player.getYRot();
        double yRotRad = Math.toRadians(yRot);


        double rightX = Math.cos(yRotRad);
        double rightZ = Math.sin(yRotRad);
        double forwardX = -Math.sin(yRotRad);
        double forwardZ = Math.cos(yRotRad);


        double rightOffset = -0.5;
        double upOffset = -0.1;
        double forwardOffset = 1.0;

        double offsetX = (rightX * rightOffset + forwardX * forwardOffset) * fadeStrength;
        double offsetY = upOffset * fadeStrength;
        double offsetZ = (rightZ * rightOffset + forwardZ * forwardOffset) * fadeStrength;

        double currentX = camera.getPosition().x;
        double currentY = camera.getPosition().y;
        double currentZ = camera.getPosition().z;

        double newX = currentX + offsetX;
        double newY = currentY + offsetY;
        double newZ = currentZ + offsetZ;

        Method m = getSetPositionMethod();
        if (m != null) {
            try {
                m.invoke(camera, newX, newY, newZ);
            } catch (Exception e) {
                LOGGER.warn("SickleCameraHandler: failed to invoke setPosition", e);
            }
        }
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}
