package adris.altoclef.util.baritone;

import baritone.api.pathing.goals.Goal;
import baritone.api.pathing.goals.GoalBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;

public class GoalFollowEntity implements Goal {

    private final Entity entity;
    private final double closeEnoughDistance;

    public GoalFollowEntity(Entity entity, double closeEnoughDistance) {
        if (entity == null) {
        }
        if (closeEnoughDistance < 0) {
        }
        this.entity = entity;
        this.closeEnoughDistance = closeEnoughDistance;
    }

    @Override
    public boolean isInGoal(int x, int y, int z) {
        if (entity == null) {
            return false;
        }
        BlockPos p = new BlockPos(x, y, z);
        //#if MC >= 12111
        boolean sameBlock = entity.getBlockPos().equals(p);
        boolean withinDist = p.isWithinDistance(entity.getEntityPos(), closeEnoughDistance);
        boolean result = sameBlock || withinDist;
        return result;
        //#else
        //$$ boolean sameBlock = entity.getBlockPos().equals(p);
        //$$ boolean withinDist = p.isWithinDistance(entity.getPos(), closeEnoughDistance);
        //$$ boolean result = sameBlock || withinDist;
        //$$ Debug.logMessage("GoalFollowEntity.isInGoal: sameBlock=" + sameBlock + ", withinDist=" + withinDist + ", closeEnoughDistance=" + closeEnoughDistance + ", result=" + result);
        //$$ return result;
        //#endif
    }

    @Override
    public double heuristic(int x, int y, int z) {
        if (entity == null) {
            return Double.MAX_VALUE;
        }
        //synchronized (BaritoneHelper.MINECRAFT_LOCK) {
        //#if MC >= 12111
        double xDiff = x - entity.getEntityPos().getX();
        int yDiff = y - entity.getBlockPos().getY();
        double zDiff = z - entity.getEntityPos().getZ();
        //#else
        //$$ double xDiff = x - entity.getPos().getX();
        //$$ int yDiff = y - entity.getBlockPos().getY();
        //$$ double zDiff = z - entity.getPos().getZ();
        //#endif
        return GoalBlock.calculate(xDiff, yDiff, zDiff);
        //}
    }
}
