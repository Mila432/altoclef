package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalNear;
import net.minecraft.util.math.BlockPos;

public class GetWithinRangeOfBlockTask extends CustomBaritoneGoalTask {

    public final BlockPos blockPos;
    public final int range;

    public GetWithinRangeOfBlockTask(BlockPos blockPos, int range) {
        if (blockPos == null) {
        }
        if (range <= 0) {
        }
        this.blockPos = blockPos;
        this.range = range;
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        return new GoalNear(blockPos, range);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GetWithinRangeOfBlockTask task) {
            boolean posEqual = task.blockPos.equals(blockPos);
            boolean rangeEqual = task.range == range;
            boolean result = posEqual && rangeEqual;
            if (!result) {
            }
            return result;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Getting within " + range + " blocks of " + blockPos.toShortString();
    }
}
