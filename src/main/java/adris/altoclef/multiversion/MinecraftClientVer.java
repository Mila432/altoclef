package adris.altoclef.multiversion;

import net.minecraft.client.MinecraftClient;

public class MinecraftClientVer {


    @Pattern
    private static float getTickDelta(MinecraftClient client) {
        if (client == null) {
            return 0.0f; // Return safe default to prevent NPE
        }
        
        float result;
        //#if MC >= 12100
        result = client.getRenderTickCounter().getTickProgress(true);
        //#else
        //$$ result = client.getTickDelta();
        //$$ Debug.logMessage("MinecraftClientVer.getTickDelta() [MC < 1.21] returning tick delta: " + result);
        //#endif
        
        if (Float.isNaN(result) || Float.isInfinite(result) || result < 0) {
        }
        return result;
    }

}
