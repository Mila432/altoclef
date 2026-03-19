package adris.altoclef.control;

import adris.altoclef.AltoClef;
import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.BlockBreakingCancelEvent;
import adris.altoclef.eventbus.events.BlockBreakingEvent;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

public class PlayerExtraController {

    private final AltoClef mod;
    private BlockPos blockBreakPos;
    private double blockBreakProgress;

    public PlayerExtraController(AltoClef mod) {
        this.mod = mod;

        EventBus.subscribe(BlockBreakingEvent.class, evt -> onBlockBreak(evt.blockPos, evt.progress));
        EventBus.subscribe(BlockBreakingCancelEvent.class, evt -> onBlockStopBreaking());
    }

    private void onBlockBreak(BlockPos pos, double progress) {
        blockBreakPos = pos;
        blockBreakProgress = progress;
    }

    private void onBlockStopBreaking() {
        blockBreakPos = null;
        blockBreakProgress = 0;
    }

    public BlockPos getBreakingBlockPos() {
        return blockBreakPos;
    }

    public boolean isBreakingBlock() {
        return blockBreakPos != null;
    }

    public double getBreakingBlockProgress() {
        return blockBreakProgress;
    }

    public boolean inRange(Entity entity) {
        return mod.getPlayer().isInRange(entity, mod.getModSettings().getEntityReachRange());
    }

    public void attack(Entity entity) {
        boolean entityInRange = inRange(entity);
        if (entityInRange) {
            mod.getController().attackEntity(mod.getPlayer(), entity);
            mod.getPlayer().swingHand(Hand.MAIN_HAND);
        }
    }
}
