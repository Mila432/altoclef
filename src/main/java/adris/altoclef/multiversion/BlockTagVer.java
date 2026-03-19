package adris.altoclef.multiversion;

import net.minecraft.block.Block;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.Registries;

public class BlockTagVer {


    public static boolean isWool(Block block) {
        // Only log critical issues - null blocks are unexpected and indicate a bug
        if (block == null) {
            return false;
        }
        
        //#if MC >= 12111
        boolean result = block.getRegistryEntry().streamTags().anyMatch(t -> t == BlockTags.WOOL);
        //#elseif MC >= 11802
        //$$ boolean result = Registries.BLOCK.getKey(block).map(e -> Registries.BLOCK.entryOf(e).streamTags().anyMatch(t -> t == BlockTags.WOOL)).orElse(false);
        //$$ Debug.logMessage("BlockTagVer.isWool: MC>=11802 check for block=" + block + " result=" + result);
        //#else
        //$$ boolean result = BlockTags.WOOL.contains(block);
        //$$ Debug.logMessage("BlockTagVer.isWool: MC<11802 check for block=" + block + " result=" + result);
        //#endif
        
        // Critical: If registry lookup fails silently, we might misidentify blocks
        // This could cause task failures if wool is expected but not found
        if (!result && block != null) {
        }
        
        return result;
    }

}
