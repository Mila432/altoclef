package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.control.InputControls;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.time.TimerGame;
import baritone.api.utils.input.Input;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.state.property.Properties;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
//#if MC <= 12006
//$$ import adris.altoclef.mixins.EntityAccessor;
//#endif

import java.util.ArrayList;
import java.util.List;

public class SafeNetherPortalTask extends Task {
    private final TimerGame wait = new TimerGame(1);
    private boolean keyReset = false;
    private boolean finished = false;
    private List<BlockPos> positions = null;
    private List<Direction> directions = null;
    private Direction.Axis axis = null;

    @Override
    protected void onStart() {
        AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
        wait.reset();
    }

    @Override
    protected Task onTick() {
        if (!wait.elapsed()) {
            return null;
        }
        AltoClef mod = AltoClef.getInstance();

        if (!keyReset) {
            keyReset = true;
            mod.getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }

        if (mod.getPlayer().getPortalCooldown() < 10) {
            if (positions != null && directions != null) {
                BlockPos pos1 = mod.getPlayer().getSteppingPos().offset(axis, 1);
                BlockPos pos2 = mod.getPlayer().getSteppingPos().offset(axis, -1);

                boolean pos1SafeOrSoul = mod.getWorld().getBlockState(pos1).isAir() || mod.getWorld().getBlockState(pos1).getBlock().equals(Blocks.SOUL_SAND);
                if (pos1SafeOrSoul) {
                    boolean passed = false;
                    for (Direction dir : Direction.values()) {
                        if (mod.getWorld().getBlockState(pos1.up().offset(dir)).getBlock().equals(Blocks.NETHER_PORTAL)) {
                            passed = true;
                            break;
                        }
                    }
                    if (passed) {
                        return new ReplaceSafeBlock(pos1);
                    }
                }

                boolean pos2SafeOrSoul = mod.getWorld().getBlockState(pos2).isAir() || mod.getWorld().getBlockState(pos2).getBlock().equals(Blocks.SOUL_SAND);
                if (pos2SafeOrSoul) {
                    boolean passed = false;
                    for (Direction dir : Direction.values()) {
                        if (mod.getWorld().getBlockState(pos2.up().offset(dir)).getBlock().equals(Blocks.NETHER_PORTAL)) {
                            passed = true;
                            break;
                        }
                    }
                    if (passed) {
                        return new ReplaceSafeBlock(pos2);
                    }
                }
            }
            finished = true;
            setDebugState("We are not in a portal");
            return null;
        }

        BlockState state = mod.getWorld().getBlockState(mod.getPlayer().getBlockPos());
        if (positions == null || directions == null) {
            if (state.getBlock().equals(Blocks.NETHER_PORTAL)) {
                axis = state.get(Properties.HORIZONTAL_AXIS);

                positions = new ArrayList<>();
                positions.add(mod.getPlayer().getBlockPos());
                for (Direction dir : Direction.values()) {
                    if (dir.getAxis().isVertical()) continue;

                    BlockPos pos = mod.getPlayer().getBlockPos().offset(dir);
                    if (mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.NETHER_PORTAL)) {
                        positions.add(pos);
                    }
                }

                directions = List.of(Direction.WEST, Direction.EAST);

                if (axis == Direction.Axis.X) {
                    directions = List.of(Direction.NORTH, Direction.SOUTH);
                }
            } else {
                finished = true;
                setDebugState("We are not standing inside a nether portal block");
            }
        } else {

            for (BlockPos pos : positions) {
                for (Direction dir : directions) {
                    BlockPos newPos = pos.down().offset(dir);
                    boolean isAir = mod.getWorld().getBlockState(newPos).isAir();
                    boolean isSoulSand = mod.getWorld().getBlockState(newPos).getBlock().equals(Blocks.SOUL_SAND);
                    if (isAir || isSoulSand) {
                        setDebugState("Changing block...");
                        return new ReplaceSafeBlock(newPos);
                    }
                }
            }

            // did not find any block thats not safe
            finished = true;
            setDebugState("Portal is safe");
            return null;
        }


        return null;
    }

    @Override
    protected void onStop(Task interruptTask) {
        InputControls controls = AltoClef.getInstance().getInputControls();

        controls.release(Input.MOVE_FORWARD);
        controls.release(Input.SNEAK);
        controls.release(Input.CLICK_LEFT);
        AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
    }

    @Override
    protected boolean isEqual(Task other) {
        return other instanceof SafeNetherPortalTask;
    }

    @Override
    protected String toDebugString() {
        return "Making nether portal safe";
    }

    @Override
    public boolean isFinished() {
        return finished;
    }

    private static class ReplaceSafeBlock extends Task {

        private final BlockPos pos;
        private boolean finished = false;

        public ReplaceSafeBlock(BlockPos pos) {
            this.pos = pos;
        }


        @Override
        protected void onStart() {
            AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }

        @Override
        protected Task onTick() {
            AltoClef mod = AltoClef.getInstance();

            if (mod.getWorld().getBlockState(pos).isAir()) {
                setDebugState("Placing block...");
                return new PlaceStructureBlockTask(pos);
            }

            if (mod.getWorld().getBlockState(pos).getBlock().equals(Blocks.SOUL_SAND)) {
                LookHelper.lookAt(mod, pos);

                //#if MC >= 12100
                float tickDelta = MinecraftClient.getInstance().getRenderTickCounter().getTickProgress(true);
                //#else
                //$$ float tickDelta = MinecraftClient.getInstance().getTickDelta();
                //#endif
                HitResult result = mod.getPlayer().raycast(3, tickDelta, true);
                if (result instanceof BlockHitResult blockHitResult) {
                    boolean hitPortal = mod.getWorld().getBlockState(blockHitResult.getBlockPos()).getBlock().equals(Blocks.NETHER_PORTAL);
                    if (hitPortal) {
                        setDebugState("Getting closer to target...");
                        mod.getInputControls().hold(Input.MOVE_FORWARD);
                        mod.getInputControls().hold(Input.SNEAK);
                    } else {
                        setDebugState("Breaking block");
                        mod.getInputControls().release(Input.MOVE_FORWARD);
                        mod.getInputControls().release(Input.SNEAK);
                        mod.getInputControls().hold(Input.CLICK_LEFT);
                    }
                } else {
                    setDebugState("Breaking block");
                    mod.getInputControls().release(Input.MOVE_FORWARD);
                    mod.getInputControls().release(Input.SNEAK);
                    mod.getInputControls().hold(Input.CLICK_LEFT);
                }
                return null;
            }

            this.finished = true;
            return null;
        }

        @Override
        protected void onStop(Task interruptTask) {
            InputControls controls = AltoClef.getInstance().getInputControls();

            controls.release(Input.MOVE_FORWARD);
            controls.release(Input.SNEAK);
            controls.release(Input.CLICK_LEFT);
            AltoClef.getInstance().getClientBaritone().getInputOverrideHandler().clearAllKeys();
        }

        @Override
        public boolean isFinished() {
            return finished;
        }

        @Override
        protected boolean isEqual(Task other) {
            return other instanceof ReplaceSafeBlock same && same.pos.equals(this.pos);
        }

        @Override
        protected String toDebugString() {
            return "Making sure " + pos + " is safe";
        }
    }

}
