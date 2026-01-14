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
import com.hypixel.hytale.server.core.event.events.ecs.BreakBlockEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

public final class BlockBreakSystem
        extends EntityEventSystem<EntityStore, BreakBlockEvent> {

    private final ClaimProtection protection;
    private final HytaleChunkResolver chunkResolver;

    public BlockBreakSystem(
            ClaimProtection protection,
            HytaleChunkResolver chunkResolver
    ) {
        super(BreakBlockEvent.class);
        this.protection = protection;
        this.chunkResolver = chunkResolver;
    }

    @Override
    public void handle(
            int entityIndex,
            @Nonnull ArchetypeChunk<EntityStore> chunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer,
            @Nonnull BreakBlockEvent event
    ) {
        Ref<EntityStore> ref = chunk.getReferenceTo(entityIndex);

        Player player = store.getComponent(ref, Player.getComponentType());
        PlayerRef playerRef = store.getComponent(ref, PlayerRef.getComponentType());

        if (player == null || playerRef == null) {
            return;
        }

        onBlockBreak(player, playerRef, event);
    }

    @Override
    @Nullable
    public Query<EntityStore> getQuery() {
        return PlayerRef.getComponentType();
    }

    @Override
    @Nonnull
    public Set<Dependency<EntityStore>> getDependencies() {
        return Collections.singleton(RootDependency.first());
    }

    private void onBlockBreak(
            Player player,
            PlayerRef playerRef,
            BreakBlockEvent event
    ) {
        ChunkPos pos = chunkResolver.fromBlock(
                player.getWorld(),
                event.getTargetBlock()
        );

        ProtectionResult result = protection.canBuild(
                pos,
                playerRef.getUuid().toString()
        );

        if (result == ProtectionResult.DENY) {
            event.setCancelled(true);
            player.sendMessage(Message.raw("You cannot break blocks here."));
        }
    }
}
