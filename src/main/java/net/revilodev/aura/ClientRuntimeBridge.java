package net.revilodev.aura;

import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import java.lang.reflect.Method;

public final class ClientRuntimeBridge {
    private static final String IMPL = "net.revilodev.aura.client.ClientRuntimeBridgeImpl";

    private ClientRuntimeBridge() {}

    public static Player getClientPlayer() {
        Object player = invoke("getClientPlayer");
        return player instanceof Player p ? p : null;
    }

    public static void openSkillsBook() {
        invoke("openSkillsBook");
    }

    public static void afterSkillsSync() {
        invoke("afterSkillsSync");
    }

    public static void showLevelUpToast(int oldLevel, int newLevel, int skillPointsGained, int abilityPointsGained) {
        invoke("showLevelUpToast", int.class, int.class, int.class, int.class, oldLevel, newLevel, skillPointsGained, abilityPointsGained);
    }

    private static Object invoke(String methodName, Class<?>[] parameterTypes, Object... args) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            return null;
        }
        try {
            Class<?> implClass = Class.forName(IMPL);
            Method method = implClass.getMethod(methodName, parameterTypes);
            return method.invoke(null, args);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private static Object invoke(String methodName, Object... args) {
        return invoke(methodName, new Class<?>[0], args);
    }

    private static Object invoke(String methodName, Class<?> a, Class<?> b, Class<?> c, Class<?> d, Object... args) {
        return invoke(methodName, new Class<?>[]{a, b, c, d}, args);
    }
}
