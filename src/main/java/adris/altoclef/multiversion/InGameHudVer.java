package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;

public class InGameHudVer {

    public static boolean shouldShowDebugHud() {
        if (MinecraftClient.getInstance() == null) {
        }
        //#if MC > 12001
        boolean result = MinecraftClient.getInstance().inGameHud.getDebugHud().shouldShowDebugHud();
        if (result) {
        }
        return result;
        //#else
        //$$ boolean debugEnabled = MinecraftClient.getInstance().options.debugEnabled;
        //$$ if (debugEnabled) {
        //$$     Debug.logMessage("InGameHudVer.shouldShowDebugHud (MC<=12001) debugEnabled: " + debugEnabled);
        //$$ }
        //$$ return debugEnabled;
        //#endif
    }

}
