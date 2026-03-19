package adris.altoclef.tasks.speedrun;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.entity.DoToClosestEntityTask;
import adris.altoclef.tasks.entity.KillEntitiesTask;
import adris.altoclef.tasks.movement.GetToBlockTask;
import adris.altoclef.tasks.movement.GetToXZTask;
import adris.altoclef.tasks.movement.GetToYTask;
import adris.altoclef.tasks.movement.ThrowEnderPearlSimpleProjectileTask;
import adris.altoclef.tasks.resources.GetBuildingMaterialsTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.StorageHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.projectile.DragonFireballEntity;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;
import java.util.function.Predicate;

// TODO:
// The 10 Portal pillars form a 43 block radius, but the angle offset/cycle is random.
// Have an internal "cycle" value or something to keep track of where that cycle is
// Detect that value by scrolling around the 43 block radius in search of obsidian and finding
// the "midpoint" between two spots of obsidian and anything else
// Then, when pillaring, make sure we move to one of those areas (so we can move further out without
// risking hitting an obsidian tower)
public class WaitForDragonAndPearlTask extends Task {

    // How far to travel away from the portal, in XZ
    private static final double XZ_RADIUS = 30;
    private static final double XZ_RADIUS_TOO_FAR = 38;
    // How high to pillar
    private static final int HEIGHT = 42; //Increase height because this too low

    private static final int CLOSE_ENOUGH_DISTANCE = 15;

    private final int Y_COORDINATE = 75;

    private static final double DRAGON_FIREBALL_TOO_CLOSE_RANGE = 40;
    private final Task buildingMaterialsTask = new GetBuildingMaterialsTask(HEIGHT + 10);
    boolean inCenter;
    private Task heightPillarTask;
    private Task throwPearlTask;
    private BlockPos targetToPearl;
    private boolean dragonIsPerching;
    // To avoid dragons breath
    private Task pillarUpFurther;

    private boolean _hasPillar = false;

    public void setExitPortalTop(BlockPos top) {
        BlockPos actualTarget = top.down();
        if (!actualTarget.equals(targetToPearl)) {
            targetToPearl = actualTarget;
            throwPearlTask = new ThrowEnderPearlSimpleProjectileTask(actualTarget);
        }
    }

    public void setPerchState(boolean perching) {
        if (dragonIsPerching != perching) {
        }
        dragonIsPerching = perching;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected Task onTick() {
        AltoClef mod = AltoClef.getInstance();

        Optional<Entity> enderMen = mod.getEntityTracker().getClosestEntity(EndermanEntity.class);
        if (enderMen.isPresent() && (enderMen.get() instanceof EndermanEntity endermanEntity) &&
                endermanEntity.getTarget()==mod.getPlayer()) {
            setDebugState("Killing angry endermen");
            Predicate<Entity> angry = entity -> endermanEntity.getTarget()==mod.getPlayer();
            return new KillEntitiesTask(angry, enderMen.get().getClass());
        }
        if (throwPearlTask != null && throwPearlTask.isActive() && !throwPearlTask.isFinished()) {
            setDebugState("Throwing pearl!");
            return throwPearlTask;
        }

        if (pillarUpFurther != null && pillarUpFurther.isActive() && !pillarUpFurther.isFinished() && (mod.getEntityTracker().getClosestEntity(AreaEffectCloudEntity.class).isPresent())) {

            Optional<Entity> cloud = mod.getEntityTracker().getClosestEntity(AreaEffectCloudEntity.class);

            if (cloud.isPresent() && cloud.get().isInRange(mod.getPlayer(), 4)) {
                setDebugState("PILLAR UP FURTHER to avoid dragon's breath");
                return pillarUpFurther;
            }

            Optional<Entity> fireball = mod.getEntityTracker().getClosestEntity(DragonFireballEntity.class);

            if (isFireballDangerous(mod, fireball)) {
                setDebugState("PILLAR UP FURTHER to avoid dragon's breath");
                return pillarUpFurther;
            }
        }

        if (!mod.getItemStorage().hasItem(Items.ENDER_PEARL) && inCenter) {
            setDebugState("First get ender pearls.");
            return TaskCatalogue.getItemTask(Items.ENDER_PEARL, 1);
        }

        if (targetToPearl == null) {
            return null;
        }

        int minHeight = targetToPearl.getY() + HEIGHT - 3;

        int deltaY = minHeight - mod.getPlayer().getBlockPos().getY();
        int requiredMaterials = Math.min(deltaY - 10, HEIGHT - 5);
        int currentMaterials = StorageHelper.getBuildingMaterialCount();
        if (currentMaterials < requiredMaterials || buildingMaterialsTask.isActive() && !buildingMaterialsTask.isFinished()) {
            setDebugState("Collecting building materials...");
            return buildingMaterialsTask;
        }

        // Our trigger to throw is that the dragon starts perching. We can be an arbitrary distance and we'll still do it lol
        if (dragonIsPerching && canThrowPearl(mod)) {
            return throwPearlTask;
        }
        if (dragonIsPerching && !canThrowPearl(mod)) {
        }
        if (mod.getPlayer().getBlockPos().getY() < minHeight) {
            if (mod.getEntityTracker().entityFound(entity ->
                //#if MC >= 12111
                mod.getPlayer().getEntityPos().isInRange(entity.getEntityPos(), 4)
                //#else
                //$$ mod.getPlayer().getPos().isInRange(entity.getPos(), 4)
                //#endif
                    , AreaEffectCloudEntity.class)) {
                if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                        !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                    LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                }
                return null;
            }
            if (heightPillarTask != null && heightPillarTask.isActive() && !heightPillarTask.isFinished()) {
                setDebugState("Pillaring up!");
                inCenter = true;
                if (mod.getEntityTracker().entityFound(EndCrystalEntity.class)) {
                    return new DoToClosestEntityTask(
                            (toDestroy) -> {
                                if (toDestroy.isInRange(mod.getPlayer(), 7)) {
                                    mod.getControllerExtras().attack(toDestroy);
                                }
                                if (mod.getPlayer().getBlockPos().getY() < minHeight) {
                                    return heightPillarTask;
                                } else {
                                    if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                                            !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                                        LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                                    }
                                    return null;
                                }
                            },
                            EndCrystalEntity.class
                    );
                }
                return heightPillarTask;
            }
        } else {
            setDebugState("We're high enough.");
            // If a fireball is too close, run UP
            Optional<Entity> dragonFireball = mod.getEntityTracker().getClosestEntity(DragonFireballEntity.class);
            if (dragonFireball.isPresent() && dragonFireball.get().isInRange(mod.getPlayer(), DRAGON_FIREBALL_TOO_CLOSE_RANGE) && LookHelper.cleanLineOfSight(mod.getPlayer(),
                //#if MC >= 12111
                dragonFireball.get().getEntityPos()
                //#else
                //$$ dragonFireball.get().getPos()
                //#endif
                    , DRAGON_FIREBALL_TOO_CLOSE_RANGE)) {
                pillarUpFurther = new GetToYTask(mod.getPlayer().getBlockY() + 5);
                return pillarUpFurther;
            }
            if (mod.getEntityTracker().entityFound(EndCrystalEntity.class)) {
                return new DoToClosestEntityTask(
                        (toDestroy) -> {
                            if (toDestroy.isInRange(mod.getPlayer(), 7)) {
                                mod.getControllerExtras().attack(toDestroy);
                            }
                            if (mod.getPlayer().getBlockPos().getY() < minHeight) {
                                return heightPillarTask;
                            } else {
                                if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                                        !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                                    LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                                }
                                return null;
                            }
                        },
                        EndCrystalEntity.class
                );
            }
            if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                    !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
            }
            return null;
        }
        if (!WorldHelper.inRangeXZ(mod.getPlayer(), targetToPearl, XZ_RADIUS_TOO_FAR) &&
            //#if MC >= 12111
            mod.getPlayer().getEntityPos().getY()
            //#else
            //$$ mod.getPlayer().getPos().getY()
            //#endif
            < minHeight && !_hasPillar) {
            if (mod.getEntityTracker().entityFound(entity ->
                //#if MC >= 12111
                mod.getPlayer().getEntityPos().isInRange(entity.getEntityPos(), 4)
                //#else
                //$$ mod.getPlayer().getPos().isInRange(entity.getPos(), 4)
                //#endif
                    , AreaEffectCloudEntity.class)) {
                if (mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).isPresent() &&
                        !mod.getClientBaritone().getPathingBehavior().isPathing()) {
                    LookHelper.lookAt(mod, mod.getEntityTracker().getClosestEntity(EnderDragonEntity.class).get().getEyePos());
                }
                return null;
            }
            setDebugState("Moving in (too far, might hit pillars)");
            return new GetToXZTask(0, 0);
        }
        // We're far enough, pillar up!
        if (!_hasPillar) {
            _hasPillar = true;
        }
        heightPillarTask = new GetToBlockTask(new BlockPos(0, minHeight, Y_COORDINATE));
        return heightPillarTask;
    }

    // basically same as LookHelper.cleanLineOfSight but edited so it has a small distance toleration
    private boolean canThrowPearl(AltoClef mod) {
        if (targetToPearl == null) {
            return false;
        }
        
        Vec3d targetPosition = WorldHelper.toVec3d(targetToPearl.up());

        // Perform a raycast from the entity's camera position to the target position with the specified max range
        BlockHitResult hitResult = LookHelper.raycast(mod.getPlayer(), LookHelper.getCameraPos(mod.getPlayer()), targetPosition, 300);

        if (hitResult == null) {
            // No hit result, clear line of sight
            return true;
        } else {
            boolean canThrow = switch (hitResult.getType()) {
                case MISS ->
                    // Missed the target, clear line of sight
                        true;
                case BLOCK ->
                    // Hit a block, check if it's the same as the target block
                        hitResult.getBlockPos().isWithinDistance(targetToPearl.up(), 10);
                case ENTITY ->
                    // Hit an entity, line of sight blocked
                        false;
            };
            return canThrow;
        }
    }

    private boolean isFireballDangerous(AltoClef mod, Optional<Entity> fireball) {
        if (fireball.isEmpty())
            return false;

        boolean fireballTooClose = fireball.get().isInRange(mod.getPlayer(), DRAGON_FIREBALL_TOO_CLOSE_RANGE);
        boolean fireballInSight = LookHelper.cleanLineOfSight(mod.getPlayer(),
            //#if MC >= 12111
            fireball.get().getEntityPos()
            //#else
            //$$ fireball.get().getPos()
            //#endif
            , DRAGON_FIREBALL_TOO_CLOSE_RANGE);

        if (fireballTooClose && fireballInSight) {
        }

        return fireballTooClose && fireballInSight;
    }

    @Override
    protected void onStop(Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof WaitForDragonAndPearlTask;
    }

    @Override
    public boolean isFinished() {
        return dragonIsPerching
                && ((throwPearlTask == null || (throwPearlTask.isActive() && throwPearlTask.isFinished()))
                || WorldHelper.inRangeXZ(AltoClef.getInstance().getPlayer(), targetToPearl, CLOSE_ENOUGH_DISTANCE));
    }

    @Override
    protected String toDebugString() {
        return "Waiting for Dragon Perch + Pearling";
    }
}
