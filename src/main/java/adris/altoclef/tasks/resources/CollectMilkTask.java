package adris.altoclef.tasks.resources;

import adris.altoclef.AltoClef;
import adris.altoclef.TaskCatalogue;
import adris.altoclef.tasks.ResourceTask;
import adris.altoclef.tasks.entity.AbstractDoToEntityTask;
import adris.altoclef.tasksystem.Task;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

import java.util.Optional;

public class CollectMilkTask extends ResourceTask {

    private final int count;

    public CollectMilkTask(int targetCount) {
        super(Items.MILK_BUCKET, targetCount);
        count = targetCount;
    }

    @Override
    protected boolean shouldAvoidPickingUp(AltoClef mod) {
        return false;
    }

    @Override
    protected void onResourceStart(AltoClef mod) {
    }

    @Override
    protected Task onResourceTick(AltoClef mod) {
        // Make sure we have a bucket.
        if (!mod.getItemStorage().hasItem(Items.BUCKET)) {
            return TaskCatalogue.getItemTask(Items.BUCKET, 1);
        }
        // Dimension
        boolean cowFound = mod.getEntityTracker().entityFound(CowEntity.class);
        boolean wrongDimension = isInWrongDimension(mod);
        if (!cowFound && wrongDimension) {
            return getToCorrectDimensionTask(mod);
        }
        if (wrongDimension && cowFound) {
        }
        return new MilkCowTask();
    }

    @Override
    protected void onResourceStop(AltoClef mod, Task interruptTask) {
    }

    @Override
    protected boolean isEqualResource(ResourceTask other) {
        return other instanceof CollectMilkTask;
    }

    @Override
    protected String toDebugStringName() {
        return "Collecting " + count + " milk buckets.";
    }

    static class MilkCowTask extends AbstractDoToEntityTask {

        public MilkCowTask() {
            super(0, -1, -1);
        }

        @Override
        protected boolean isSubEqual(AbstractDoToEntityTask other) {
            return other instanceof MilkCowTask;
        }

        @Override
        protected Task onEntityInteract(AltoClef mod, Entity entity) {
            if (!mod.getItemStorage().hasItem(Items.BUCKET)) {
                return null;
            }
            if (mod.getSlotHandler().forceEquipItem(Items.BUCKET)) {
                mod.getController().interactEntity(mod.getPlayer(), entity, Hand.MAIN_HAND);
            } else {
                int bucketCount = mod.getItemStorage().getItemCount(Items.BUCKET);
            }


            return null;
        }

        @Override
        protected Optional<Entity> getEntityTarget(AltoClef mod) {
            //#if MC >= 12111
            return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getEntityPos(), CowEntity.class);
            //#else
            //$$ return mod.getEntityTracker().getClosestEntity(mod.getPlayer().getPos(), CowEntity.class);
            //#endif
        }

        @Override
        protected String toDebugString() {
            return "Milking Cow";
        }
    }
}
