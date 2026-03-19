package adris.altoclef.multiversion;

import net.minecraft.client.render.RenderLayer;

public class RenderLayerVer {

    public static RenderLayer getGuiOverlay() {
        //#if MC >= 12111
        // For 1.21.11+, the RenderLayer.getGuiOverlay() method no longer exists.
        // Since we don't have evidence of what to use instead, we return null.
        return null;
        //#elseif MC >= 12001
        //$$ return RenderLayer.getGuiOverlay();
        //#else
        //$$ return null;
        //#endif
    }

}
