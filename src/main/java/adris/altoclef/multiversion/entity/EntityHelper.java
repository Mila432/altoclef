package adris.altoclef.multiversion.entity;

import adris.altoclef.mixins.PortalManagerAccessor;
import net.minecraft.block.NetherPortalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.NetherPortal;

public class EntityHelper {

    //#if MC >= 12100
    public static boolean isInNetherPortal(Entity entity) {
       boolean portalManagerNotNull = entity.portalManager != null;
       boolean portalBlockIsNether = portalManagerNotNull && ((PortalManagerAccessor)entity.portalManager).accessPortal() instanceof NetherPortalBlock;
       boolean isInPortal = portalManagerNotNull && entity.portalManager.isInPortal();
       boolean cooldownActive = entity.getPortalCooldown() > 0;
       boolean result = (portalManagerNotNull && portalBlockIsNether && isInPortal) || cooldownActive;
       
       
       return result;
    }
    //#endif

    //#if MC <= 11605
    //$$ public static Vec3d getEyePos(Entity entity) {
    //$$     return new Vec3d(entity.getX(), entity.getEyeY(), entity.getZ());
    //$$ }
    //$$
    //$$ public static ChunkPos getChunkPos(Entity entity) {
    //$$    return new ChunkPos(entity.getBlockPos());
    //$$ }
    //$$
    //$$ public static int getBlockX(Entity entity) {
    //$$      return entity.getBlockPos().getX();
    //$$  }
    //$$
    //$$ public static int getBlockY(Entity entity) {
    //$$      return entity.getBlockPos().getY();
    //$$  }
    //$$
    //$$ public static int getBlockZ(Entity entity) {
    //$$      return entity.getBlockPos().getZ();
    //$$  }
    //#endif

}
