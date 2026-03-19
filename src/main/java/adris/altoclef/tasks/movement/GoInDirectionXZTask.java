package adris.altoclef.tasks.movement;

import adris.altoclef.AltoClef;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.baritone.GoalDirectionXZ;
import baritone.api.pathing.goals.Goal;
import net.minecraft.util.math.Vec3d;

public class GoInDirectionXZTask extends CustomBaritoneGoalTask {

    private final Vec3d _origin;
    private final Vec3d _delta;
    private final double _sidePenalty;

    public GoInDirectionXZTask(Vec3d origin, Vec3d delta, double sidePenalty) {
        _origin = origin;
        _delta = delta;
        _sidePenalty = sidePenalty;
    }

    private static boolean closeEnough(Vec3d a, Vec3d b) {
        return a.squaredDistanceTo(b) < 0.001;
    }

    @Override
    protected Goal newGoal(AltoClef mod) {
        try {
            return new GoalDirectionXZ(_origin, _delta, _sidePenalty);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    protected boolean isEqual(Task other) {
        if (other instanceof GoInDirectionXZTask) {
            GoInDirectionXZTask task = (GoInDirectionXZTask) other;
            boolean originClose = closeEnough(task._origin, _origin);
            boolean deltaClose = closeEnough(task._delta, _delta);
            boolean result = originClose && deltaClose;
            if (!result) {
            }
            return result;
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Going in direction: <" + _origin.x + "," + _origin.z + "> direction: <" + _delta.x + "," + _delta.z + ">";
    }
}
