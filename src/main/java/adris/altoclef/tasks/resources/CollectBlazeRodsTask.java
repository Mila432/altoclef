package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.multiversion.blockpos.BlockPosVer;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.construction.PutOutFireTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.DefaultGoToDimensionTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.RunAwayFromHostilesTask;
import adris.altoclef.tasks.movement.SearchChunkForBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.Dimension;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.Optional;
import java.util.function.Predicate;

public class CollectBlazeRodsTask extends ResourceTask {

    private static final double SPAWNER_BLAZE_RADIUS = 32;
    private static final double TOO_LITTLE_HEALTH_BLAZE = 10;
    private static final int TOO_MANY_BLAZES = 5;
    private final int _count;
    private final Task _searcher = new SearchChunkForBlockTask(Blocks.NETHER_BRICKS);

    // Why was this here???
    //private Entity _toKill;
    private BlockPos _foundBlazeSpawner = null;

    public CollectBlazeRodsTask(int count) {
        super(Items.BLAZE_ROD, count);
        _count = count;
    }

    private static boolean isHoveringAboveLavaOrTooHigh(AltoClef mod, Entity entity) {
        int MAX_HEIGHT = 11;
        for (BlockPos check = entity.getBlockPos(); entity.getBlockPos().getY() - check.getY() < MAX_HEIGHT; check = check.down()) {
            if (mod.getWorld().getBlockState(check).getBlock() == Blocks.LAVA) return true;
            if (WorldHelper.isSolidBlock(check)) return false;
        }
        return true;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // We must go to the nether.
        if (WorldHelper.getCurrentDimension() != Dimension.NETHER) {
            setDebugState("Going to nether");
            return new DefaultGoToDimensionTask(Dimension.NETHER);
        }

        Optional<Entity> toKill = Optional.empty();
        // If there is a blaze, kill it.
        if (mod.getEntityTracker().entityFound(BlazeEntity.class)) {
            toKill = mod.getEntityTracker().getClosestEntity(BlazeEntity.class);
            if (toKill.isPresent()) {
                int nearbyBlazesCount = mod.getEntityTracker().getTrackedEntities(BlazeEntity.class).size();
                boolean healthTooLow = mod.getPlayer().getHealth() <= TOO_LITTLE_HEALTH_BLAZE;
                boolean tooManyBlazes = nearbyBlazesCount >= TOO_MANY_BLAZES;
                if (healthTooLow && tooManyBlazes) {
                    setDebugState("Running away as there are too many blazes nearby.");
                    return new RunAwayFromHostilesTask(15 * 2, true);
                }
            }

            if (_foundBlazeSpawner != null && toKill.isPresent()) {
                Entity kill = toKill.get();
                //#if MC >= 12111
                Vec3d nearest = kill.getEntityPos();
                //#else
                //$$ Vec3d nearest = kill.getPos();
                //#endif

                //#if MC >= 12111
                double sqDistanceToPlayer = nearest.squaredDistanceTo(mod.getPlayer().getEntityPos());//_foundBlazeSpawner.getX(), _foundBlazeSpawner.getY(), _foundBlazeSpawner.getZ());
                //#else
                //$$ double sqDistanceToPlayer = nearest.squaredDistanceTo(mod.getPlayer().getPos());//_foundBlazeSpawner.getX(), _foundBlazeSpawner.getY(), _foundBlazeSpawner.getZ());
                //#endif
                // Ignore if the blaze is too far away.
                if (sqDistanceToPlayer > SPAWNER_BLAZE_RADIUS * SPAWNER_BLAZE_RADIUS) {
                    // If the blaze can see us it needs to go lol
                    BlockHitResult hit = mod.getWorld().raycast(new RaycastContext(mod.getPlayer().getCameraPosVec(1.0F), kill.getCameraPosVec(1.0F), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, mod.getPlayer()));
                    //#if MC >= 12111
                    boolean isBlockedByTerrain = hit != null && BlockPosVer.getSquaredDistance(hit.getBlockPos(),mod.getPlayer().getEntityPos()) < sqDistanceToPlayer;
                    //#else
                    //$$ boolean isBlockedByTerrain = hit != null && BlockPosVer.getSquaredDistance(hit.getBlockPos(),mod.getPlayer().getPos()) < sqDistanceToPlayer;
                    //#endif
                    if (isBlockedByTerrain) {
                        toKill = Optional.empty();
                    }
                }
            }
        }
        if (toKill.isPresent() && toKill.get().isAlive() && !isHoveringAboveLavaOrTooHigh(mod, toKill.get())) {
            setDebugState("Killing blaze");
            Predicate<Entity> safeToPursue = entity -> !isHoveringAboveLavaOrTooHigh(mod, entity);
            return new KillEntitiesTask(safeToPursue, toKill.get().getClass());
        }


        // If the blaze spawner somehow isn't valid
        if (_foundBlazeSpawner != null && mod.getChunkTracker().isChunkLoaded(_foundBlazeSpawner) && !isValidBlazeSpawner(mod, _foundBlazeSpawner)) {
            boolean isLoaded = mod.getChunkTracker().isChunkLoaded(_foundBlazeSpawner);
            boolean isValid = isValidBlazeSpawner(mod, _foundBlazeSpawner);
            _foundBlazeSpawner = null;
        }

        // If we have a blaze spawner, go near it.
        if (_foundBlazeSpawner != null) {
            //#if MC >= 12111
            if (!_foundBlazeSpawner.isWithinDistance(mod.getPlayer().getEntityPos(), 4)) {
            //#else
            //$$ if (!_foundBlazeSpawner.isWithinDistance(mod.getPlayer().getPos(), 4)) {
            //#endif
                setDebugState("Going to blaze spawner");
                return new GetToBlockTask(_foundBlazeSpawner.up(), false);
            } else {

                // Put out fire that might mess with us.
                Optional<BlockPos> nearestFire = mod.getBlockScanner().getNearestWithinRange(_foundBlazeSpawner, 5, Blocks.FIRE);
                if (nearestFire.isPresent()) {
                    setDebugState("Clearing fire around spawner to prevent loss of blaze rods.");
                    return new PutOutFireTask(nearestFire.get());
                }

                setDebugState("Waiting near blaze spawner for blazes to spawn");
                return null;
            }
        } else {
            // Search for blaze
            Optional<BlockPos> pos = mod.getBlockScanner().getNearestBlock(blockPos->isValidBlazeSpawner(mod, blockPos),Blocks.SPAWNER);

            pos.ifPresent(blockPos -> {
                _foundBlazeSpawner = blockPos;
            });
        }

        // We need to find our fortress.
        setDebugState("Searching for fortress/Traveling around fortress");
        return _searcher;
    }

    private boolean isValidBlazeSpawner(AltoClef mod, BlockPos pos) {
        if (!mod.getChunkTracker().isChunkLoaded(pos)) {
            // If unloaded, go to it. Unless it's super far away.
            return false;
            //return pos.isWithinDistance(mod.getPlayer().getPos(),3000);
        }
        return WorldHelper.getSpawnerEntity(pos) instanceof BlazeEntity;
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectBlazeRodsTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collect blaze rods - "+ AltoClef.getInstance().getItemStorage().getItemCount(Items.BLAZE_ROD)+"/"+_count;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }
}
