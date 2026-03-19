package adris.altoclef.tasks.entity;

import adris.altoclef.AltoClef;
import adris.altoclef.tasks.AbstractDoToClosestObjectTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Finds the closest entity and runs a task on that entity
 */
@SuppressWarnings("rawtypes")
public class DoToClosestEntityTask extends AbstractDoToClosestObjectTask<Entity> {

    private final Class[] targetEntities;

    private final Supplier<Vec3d> getOriginPos;

    private final Function<Entity, Task> getTargetTask;

    private final Predicate<Entity> shouldInteractWith;

    public DoToClosestEntityTask(Supplier<Vec3d> getOriginSupplier, Function<Entity, Task> getTargetTask, Predicate<Entity> shouldInteractWith, Class... entities) {
        getOriginPos = getOriginSupplier;
        this.getTargetTask = getTargetTask;
        this.shouldInteractWith = shouldInteractWith;
        targetEntities = entities;
    }

    public DoToClosestEntityTask(Supplier<Vec3d> getOriginSupplier, Function<Entity, Task> getTargetTask, Class... entities) {
        this(getOriginSupplier, getTargetTask, entity -> true, entities);
    }

    public DoToClosestEntityTask(Function<Entity, Task> getTargetTask, Predicate<Entity> shouldInteractWith, Class... entities) {
        this(null, getTargetTask, shouldInteractWith, entities);
    }

    public DoToClosestEntityTask(Function<Entity, Task> getTargetTask, Class... entities) {
        this(null, getTargetTask, entity -> true, entities);
    }

    @Override
    protected Vec3d getPos(AltoClef mod, Entity obj) {
        //#if MC >= 12111
        return obj.getEntityPos();
        //#else
        //$$ return obj.getPos();
        //#endif
    }

    @Override
    protected Optional<Entity> getClosestTo(AltoClef mod, Vec3d pos) {
        if (!mod.getEntityTracker().entityFound(targetEntities)) {
            return Optional.empty();
        }
        Optional<Entity> closest = mod.getEntityTracker().getClosestEntity(pos, shouldInteractWith, targetEntities);
        return closest;
    }

    @Override
    protected Vec3d getOriginPos(AltoClef mod) {
        if (getOriginPos != null) {
            return getOriginPos.get();
        }
        //#if MC >= 12111
        return mod.getPlayer().getEntityPos();
        //#else
        //$$ return mod.getPlayer().getPos();
        //#endif
    }

    @Override
    protected Task getGoalTask(Entity obj) {
        return getTargetTask.apply(obj);
    }

    @Override
    protected boolean isValid(AltoClef mod, Entity obj) {
        boolean alive = obj.isAlive();
        boolean reachable = mod.getEntityTracker().isEntityReachable(obj);
        if (!alive || !reachable) {
        }
        return alive && reachable;
    }

    @Override
    protected void onStart() {
    }

    @Override
    protected void onStop(Task interruptTask) {
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof DoToClosestEntityTask task) {
            return Arrays.equals(task.targetEntities, targetEntities);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Doing something to closest entity...";
    }
}
