package adris.altoclef.multiversion.world;

import adris.altoclef.multiversion.Pattern;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

//#if MC >= 11904
import net.minecraft.registry.RegistryKey;
//#else
//$$ import net.minecraft.util.registry.Registry;
//$$ import net.minecraft.util.registry.RegistryKey;
//#endif

//#if MC >= 11802
import net.minecraft.registry.entry.RegistryEntry;
//#endif

public class WorldVer {

    public static boolean isBiomeAtPos(World world, RegistryKey<Biome> biome, BlockPos pos) {
        // Critical null checks - these would cause silent failures
        if (world == null) {
            return false;
        }
        if (biome == null) {
        }
        if (pos == null) {
            return false;
        }
        
        //#if MC >= 11802
        RegistryEntry<Biome> b = world.getBiome(pos);
        if (b == null) {
            return false;
        }
        boolean matches = b.matchesKey(biome);
        return matches;
        //#else
        //$$ java.util.Optional<RegistryKey<Biome>> keyOpt = world.getBiomeKey(pos);
        //$$ if (!keyOpt.isPresent()) {
        //$$     Debug.logWarning("WorldVer.isBiomeAtPos: No biome key found at position " + pos);
        //$$     return false;
        //$$ }
        //$$ RegistryKey<Biome> key = keyOpt.get();
        //$$ if (key == null) {
        //$$     Debug.logWarning("WorldVer.isBiomeAtPos: Biome key is null at position " + pos);
        //$$     return false;
        //$$ }
        //$$ boolean matches = key == biome;
        //$$ Debug.logMessage("WorldVer.isBiomeAtPos: Biome key comparison at " + pos + " between found " + key.getValue() + " and target " + biome.getValue() + " returned " + matches);
        //$$ return matches;
        //#endif
    }

    //#if MC >= 11802
    public static boolean isBiome(RegistryEntry<Biome> biome1, RegistryKey<Biome> biome2) {
        if (biome1 == null) {
            return false;
        }
        if (biome2 == null) {
            return false;
        }
        boolean matches = biome1.matchesKey(biome2);
        return matches;
    }
    //#else
    //$$ public static boolean isBiome(Biome biome1, RegistryKey<Biome> biome2) {
    //$$     if (biome1 == null) {
    //$$         Debug.logWarning("WorldVer.isBiome (pre-1.18.2): Biome is null");
    //$$         return false;
    //$$     }
    //$$     if (biome2 == null) {
    //$$         Debug.logWarning("WorldVer.isBiome (pre-1.18.2): Biome key is null");
    //$$         return false;
    //$$     }
    //$$     World world = MinecraftClient.getInstance().world;
    //$$     if (world == null) {
    //$$         Debug.logError("WorldVer.isBiome (pre-1.18.2): Minecraft world is null");
    //$$         return false;
    //$$     }
    //$$     Biome foundBiome = world.getRegistryManager().get(Registry.BIOME_KEY).get(biome2);
    //$$     if (foundBiome == null) {
    //$$         Debug.logWarning("WorldVer.isBiome (pre-1.18.2): Biome key '" + biome2.getValue() + "' not found in registry");
    //$$     }
    //$$     boolean matches = foundBiome == biome1;
    //$$     Debug.logMessage("WorldVer.isBiome (pre-1.18.2): Registry lookup for " + biome2.getValue() + " returned " + foundBiome + ", comparing to input biome1 = " + biome1 + " yields " + matches);
    //$$     return matches;
    //$$ }
    //#endif

    @Pattern
    public static int getBottomY(World world) {
        if (world == null) {
            return -64; // Safe default for most versions
        }
        
        //#if MC >= 11701
        return world.getBottomY();
        //#else
        //$$ return adris.altoclef.multiversion.world.WorldHelper.getBottomY(world);
        //#endif
    }

    public static int getTopY(World world) {
        if (world == null) {
            return 320; // Safe default for most versions
        }
        
        //#if MC >= 11701
        int bottom = world.getBottomY();
        int height = world.getHeight();
        if (height <= 0) {
        }
        int topY = bottom + height;
        return topY;
        //#else
        //$$ return adris.altoclef.multiversion.world.WorldHelper.getTopY(world);
        //#endif
    }

    @Pattern
    private static boolean isOutOfHeightLimit(World world, BlockPos pos) {
        if (world == null) {
            return true; // Conservative: assume out of bounds
        }
        if (pos == null) {
            return true; // Conservative: assume out of bounds
        }
        
        //#if MC >= 11701
        return world.isOutOfHeightLimit(pos);
        //#else
        //$$ return adris.altoclef.multiversion.world.WorldHelper.isOutOfHeightLimit(world, pos);
        //#endif
    }
}
