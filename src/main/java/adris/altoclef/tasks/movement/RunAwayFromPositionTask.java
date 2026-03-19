package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalRunAway;
import net.minecraft.util.math.BlockPos;

import java.util.Arrays;

public class RunAwayFromPositionTask extends CustomBaritoneGoalTask {

    private final BlockPos[] _dangerBlocks;
    private final double _distance;
    private final Integer _maintainY;

    public RunAwayFromPositionTask(double distance, BlockPos... toRunAwayFrom) {
        this(distance, null, toRunAwayFrom);
    }

    public RunAwayFromPositionTask(double distance, Integer maintainY, BlockPos... toRunAwayFrom) {
        _distance = distance;
        _dangerBlocks = toRunAwayFrom;
        _maintainY = maintainY;
        if (toRunAwayFrom == null || toRunAwayFrom.length == 0) {
        }
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        if (_dangerBlocks == null || _dangerBlocks.length == 0) {
        }
        return new GoalRunAway(_distance, _maintainY, _dangerBlocks);
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof RunAwayFromPositionTask task) {
            boolean arraysEqual = Arrays.equals(task._dangerBlocks, _dangerBlocks);
            if (!arraysEqual) {
            }
            return arraysEqual;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Running away from " + Arrays.toString(_dangerBlocks);
    }
}
