package codes.wunder.hytaleregions.ecs.adapters;

import codes.wunder.hytaleregions.claim.ChunkPos;
import com.hypixel.hytale.math.vector.Vector3i;
import com.hypixel.hytale.server.core.universe.world.World;

public final class HytaleChunkResolver {

    public ChunkPos fromBlock(World world, Vector3i blockPos) {
        int chunkX = blockPos.x >> 4;
        int chunkZ = blockPos.z >> 4;

        return new ChunkPos(
                world.getName(),
                chunkX,
                chunkZ
        );
    }
}

