package adris.altoclef.mixins;

import adris.altoclef.eventbus.EventBus;
import adris.altoclef.eventbus.events.ChunkLoadEvent;
import adris.altoclef.eventbus.events.ChunkUnloadEvent;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkManager.class)
public class LoadChunkMixin {

    @Inject(
            method = "loadChunkFromPacket",
            at = @At("RETURN")
    )
    private void onLoadChunk(CallbackInfoReturnable<WorldChunk> cir) {
        EventBus.publish(new ChunkLoadEvent(cir.getReturnValue()));
    }

    @Inject(
            method = "unload",
            at = @At("TAIL")
    )
    //#if MC >= 12002
    private void onChunkUnload(ChunkPos pos, CallbackInfo ci) {
        EventBus.publish(new ChunkUnloadEvent(pos));
    }
    //#else
    //$$ private void onChunkUnload(int x, int z, CallbackInfo ci) {
    //$$     EventBus.publish(new ChunkUnloadEvent(new ChunkPos(x, z)));
    //$$ }
    //#endif
}
