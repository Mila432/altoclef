package adris.altoclef.tasksystem;

import adris.altoclef.AltoClef;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Some tasks may mess up royally if we interrupt them while mid air.
 * For instance, if we're doing some parkour and a baritone task is stopped,
 * the player will fall to whatever is below them, perhaps their death.
 */
public interface ITaskRequiresGrounded extends ITaskCanForce {
    @Override
    default boolean shouldForce(Task interruptingCandidate) {
        if (interruptingCandidate instanceof ITaskOverridesGrounded)
            return false;

        ClientPlayerEntity player = AltoClef.getInstance().getPlayer();
        if (player == null) {
            return false;
        }

        boolean isGrounded = player.isOnGround() || player.isSwimming() || player.isTouchingWater() || player.isClimbing();
        if (!isGrounded) {
        }
        return !isGrounded;
    }
}
