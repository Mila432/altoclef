package adris.altoclef.multiversion;

import adris.altoclef.Debug; // Added import for Debug
//#if MC < 12111
//$$ import net.minecraft.item.ToolItem;
//$$ import net.minecraft.item.ToolMaterials;
//#endif
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.Item;

public class ToolMaterialVer {

    public static boolean isTool(Item item) {
        if (item == null) {
            return false;
        }
        //#if MC >= 12111
        boolean result = item.getDefaultStack().contains(net.minecraft.component.DataComponentTypes.TOOL);
        return result;
        //#else
        //$$ boolean result = item instanceof ToolItem;
        //$$ return result;
        //#endif
    }

    //#if MC < 12111
    //$$ public static int getMiningLevel(ToolItem toolItem) {
    //$$     if (toolItem == null) {
    //$$         Debug.logError("ToolMaterialVer.getMiningLevel(ToolItem) - CRITICAL: ToolItem parameter is null");
    //$$         return 0;
    //$$     }
    //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(ToolItem) - Getting mining level for ToolItem: " + toolItem.getTranslationKey());
    //$$     int level = getMiningLevel(toolItem.getMaterial());
    //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(ToolItem) - Returning mining level: " + level + " for item: " + toolItem.getTranslationKey());
    //$$     return level;
    //$$ }
    //#endif

    public static int getMiningLevel(Item item) {
        if (item == null) {
            return 0;
        }
        //#if MC >= 12111
        if (item.getDefaultStack().contains(net.minecraft.component.DataComponentTypes.TOOL)) {
            // In 1.21.11, ToolMaterial constants are directly accessible from ToolMaterial interface
            // We can check the default stack's tool component to determine mining level
            // Since we cannot directly get ToolMaterial from Item in 1.21.11, we'll use a different approach
            // For now, return 0 as a fallback - this will be fixed by checking the actual tool material
            // from the TOOL component in the item's default stack
            return 0;
        }
        return 0;
        //#else
        //$$ if (item instanceof ToolItem toolItem) {
        //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(Item) - Pre-1.21.11: Item is ToolItem, delegating to material-based method");
        //$$     return getMiningLevel(toolItem.getMaterial());
        //$$ }
        //$$ Debug.logMessage("ToolMaterialVer.getMiningLevel(Item) - Pre-1.21.11: Item is not ToolItem, returning 0");
        //$$ return 0;
        //#endif
    }

    public static int getMiningLevel(ToolMaterial material) {
        if (material == null) {
            throw new IllegalArgumentException("ToolMaterial parameter cannot be null");
        }
        //#if MC < 12111
        //$$ if (material.equals(ToolMaterials.WOOD) || material.equals(ToolMaterials.GOLD)) {
        //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(ToolMaterial) - Material is WOOD or GOLD, returning 0");
        //$$     return 0;
        //$$ } else if (material.equals(ToolMaterials.STONE)) {
        //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(ToolMaterial) - Material is STONE, returning 1");
        //$$     return 1;
        //$$ } else if (material.equals(ToolMaterials.IRON)) {
        //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(ToolMaterial) - Material is IRON, returning 2");
        //$$     return 2;
        //$$ } else if (material.equals(ToolMaterials.DIAMOND)) {
        //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(ToolMaterial) - Material is DIAMOND, returning 3");
        //$$     return 3;
        //$$ } else if (material.equals(ToolMaterials.NETHERITE)) {
        //$$     Debug.logMessage("ToolMaterialVer.getMiningLevel(ToolMaterial) - Material is NETHERITE, returning 4");
        //$$     return 4;
        //$$ }
        //#else
        if (material.equals(ToolMaterial.WOOD) || material.equals(ToolMaterial.GOLD)) {
            return 0;
        } else if (material.equals(ToolMaterial.STONE)) {
            return 1;
        } else if (material.equals(ToolMaterial.IRON)) {
            return 2;
        } else if (material.equals(ToolMaterial.DIAMOND)) {
            return 3;
        } else if (material.equals(ToolMaterial.NETHERITE)) {
            return 4;
        }
        //#endif
        throw new IllegalStateException("Unexpected value: " + material);
    }

}
