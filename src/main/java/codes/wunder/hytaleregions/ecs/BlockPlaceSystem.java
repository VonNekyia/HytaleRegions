package codes.wunder.hytaleregions.ecs;

import codes.wunder.hytaleregions.claim.ChunkPos;
import codes.wunder.hytaleregions.ecs.adapters.HytaleChunkResolver;
import codes.wunder.hytaleregions.ecs.protection.ClaimProtection;
import codes.wunder.hytaleregions.ecs.protection.ProtectionResult;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.dependency.Dependency;
import com.hypixel.hytale.component.dependency.RootDependency;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.EntityEventSystem;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.event.events.ecs.PlaceBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public final class BlockPlaceSystem extends EntityEventSystem<EntityStore, PlaceBlockEvent> {

    private final ClaimProtection protection;
    private final HytaleChunkResolver chunkResolver;

    public BlockPlaceSystem(
            ClaimProtection protection,
            HytaleChunkResolver chunkResolver
    ) {
        super(PlaceBlockEvent.class);
        this.protection = protection;
        this.chunkResolver = chunkResolver;
    }

    @Override
    public void handle(
            int entityIndex,
            @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull PlaceBlockEvent event
    ) {
        Ref<EntityStore> ref = chunk.getReferenceTo(entityIndex);

        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        onBlockPlace(player, playerRef, event);
    }

    @Override
    @Nullable
    public Query<EntityStore> getQuery() {
        // Only run for player entities
        return PlayerRef.getComponentType();
    }

    @Override
    @Nonnull
    public Set<Dependency<EntityStore>> getDependencies() {
        // Run as early as possible (important for protection systems)
        return Collections.singleton(RootDependency.first());
    }

    private void onBlockPlace(
            Player player,
            PlayerRef playerRef,
            PlaceBlockEvent event
    ) {
        ChunkPos pos = chunkResolver.fromBlock(
                player.getWorld(),
                event.getTargetBlock()
        );

        if (protection.canBuild(pos, playerRef.getUuid().toString())
                == ProtectionResult.DENY) {

            event.setCancelled(true);
            player.sendMessage(Message.raw("You cannot place blocks here."));
        }
    }
}
