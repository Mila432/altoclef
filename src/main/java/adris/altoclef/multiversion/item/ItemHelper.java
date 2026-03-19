package adris.altoclef.multiversion.item;

import adris.altoclef.mixins.AxeItemAccessor;
import adris.altoclef.mixins.MiningToolItemAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;

//#if MC <= 11605
//$$ import net.minecraft.item.MiningToolItem;
//$$ import net.minecraft.item.PickaxeItem;
//#endif

import java.util.Set;

public class ItemHelper {


    //#if MC <= 11605
    //$$ public static boolean isSuitableFor(Item item, BlockState state){
    //$$     // Critical null check - log error if item is null
    //$$     if (item == null) {
    //$$         Debug.logError("ItemHelper.isSuitableFor called with NULL item parameter! state=" + state);
    //$$     }
    //$$     
    //$$     if (item instanceof PickaxeItem pickaxe) {
    //$$         boolean result = pickaxe.isSuitableFor(state);
    //$$         return result;
    //$$     }
    //$$
    //$$     if (item instanceof MiningToolItem) {
    //$$         boolean isInEffectiveBlocks = ((MiningToolItemAccessor)item).getEffectiveBlocks().contains(state.getBlock());
    //$$
    //$$         if (item instanceof AxeItem) {
    //$$             boolean hasEffectiveMaterial = ((AxeItemAccessor)item).getEffectiveMaterials().contains(state.getMaterial());
    //$$             boolean combinedResult = isInEffectiveBlocks || hasEffectiveMaterial;
    //$$             Debug.logMessage("AxeItem suitability: block=" + state.getBlock() + ", material=" + state.getMaterial() + 
    //$$                 " -> isInEffectiveBlocks=" + isInEffectiveBlocks + ", hasEffectiveMaterial=" + hasEffectiveMaterial + 
    //$$                 " -> combinedResult=" + combinedResult);
    //$$             return combinedResult;
    //$$         }
    //$$         return isInEffectiveBlocks;
    //$$     }
    //$$
    //$$     boolean defaultResult = item.isSuitableFor(state);
    //$$     return defaultResult;
    //$$ }
    //$$
    //#endif

}
