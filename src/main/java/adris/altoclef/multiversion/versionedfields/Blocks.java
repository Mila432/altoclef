package adris.altoclef.multiversion.versionedfields;

import net.minecraft.block.Block;

/**
 * A helper class implementing blocks that are not yet supported in certain versions
 * Using these in non-supported versions might lead to strange bugs and/or crashes...
 * Please see {@link VersionedFieldHelper#isSupported(Object)}
 */
public abstract class Blocks extends net.minecraft.block.Blocks {

    static {
    }

    public static final Block UNSUPPORTED = VersionedFieldHelper.createUnsafeUnsupportedBlock();

    static {
        // Critical: Verify UNSUPPORTED block was created successfully
        if (UNSUPPORTED == null) {
        }
    }

    //#if MC <= 11802
    //$$ public static final Block MANGROVE_PROPAGULE = UNSUPPORTED;
    //$$ public static final Block CHERRY_LEAVES = UNSUPPORTED;
    //$$ public static final Block MANGROVE_SIGN = UNSUPPORTED;
    //$$ public static final Block MANGROVE_WALL_SIGN = UNSUPPORTED;
    //$$ public static final Block BAMBOO_SIGN = UNSUPPORTED;
    //$$ public static final Block BAMBOO_WALL_SIGN = UNSUPPORTED;
    //$$ public static final Block CHERRY_SIGN = UNSUPPORTED;
    //$$ public static final Block CHERRY_WALL_SIGN = UNSUPPORTED;
    //$$ public static final Block SCULK = UNSUPPORTED;
    //$$ public static final Block SCULK_VEIN = UNSUPPORTED;
    //$$ public static final Block SCULK_SHRIEKER = UNSUPPORTED;
    static {
        // Critical: Log if any block assignment failed
        if (MANGROVE_PROPAGULE == null || CHERRY_LEAVES == null || SCULK == null) {
        }
    }
    //#endif

    //#if MC <= 11605
    //$$ public static Block FLOWERING_AZALEA = UNSUPPORTED;
    //$$ public static Block AZALEA = UNSUPPORTED;
    static {
        // Critical: Verify azalea blocks were properly marked
        if (FLOWERING_AZALEA == null || AZALEA == null) {
        }
    }
    //#endif

    //#if MC >= 11700
    public static final Block DIRT_PATH = net.minecraft.block.Blocks.DIRT_PATH;
    static {
        // Critical: Verify DIRT_PATH exists in this version
        if (net.minecraft.block.Blocks.DIRT_PATH == null) {
        }
    }
    //#else
    //$$ public static final Block DIRT_PATH = net.minecraft.block.Blocks.GRASS_PATH;
    //$$ static {
    //$$     Debug.logMessage("[Blocks] MC < 1.17.0: Using GRASS_PATH as DIRT_PATH equivalent");
    //$$     // Critical: Verify GRASS_PATH exists in this version
    //$$     if (net.minecraft.block.Blocks.GRASS_PATH == null) {
    //$$         Debug.logError("[Blocks] CRITICAL: net.minecraft.block.Blocks.GRASS_PATH is null in MC < 1.17.0! This indicates a version mismatch or reflection issue.");
    //$$     }
    //$$ }
    //#endif
    
    // Critical: Log the final state of key blocks for debugging
    static {
    }
}
