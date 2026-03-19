package adris.altoclef.eventbus.events;

import net.minecraft.util.math.ChunkPos;

public class ChunkUnloadEvent {
    public ChunkPos chunkPos;

    public ChunkUnloadEvent(ChunkPos chunkPos) {
        if (chunkPos == null) {
        }
        this.chunkPos = chunkPos;
    }
}
