package codes.wunder.hytaleregions.commands;

import codes.wunder.hytaleregions.claim.ChunkPos;
import codes.wunder.hytaleregions.claim.ClaimManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public final class ClaimCommand extends AbstractAsyncCommand {

    private final ClaimManager claimManager;

    public ClaimCommand(ClaimManager claimManager) {
        super("claim", "Claim the chunk you are standing in");
        this.claimManager = claimManager;
    }

    @Override
    protected @NotNull CompletableFuture<Void> executeAsync(@NotNull CommandContext ctx) {
        if (!ctx.isPlayer()) return CompletableFuture.completedFuture(null);

        Player player = ctx.senderAs(Player.class);
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();

        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);

        PlayerRef playerRef =
                ref.getStore().getComponent(ref, PlayerRef.getComponentType());

        if (!PermissionsModule.get().hasPermission(
                playerRef.getUuid(), "hytaleregions.claim")) {
            player.sendMessage(Message.raw("§cYou do not have permission."));
            return CompletableFuture.completedFuture(null);
        }

        Store<EntityStore> store = ref.getStore();
        World world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            try {
                TransformComponent transform =
                        store.getComponent(ref, TransformComponent.getComponentType());

                int chunkX = ((int) Math.floor(transform.getPosition().getX())) >> 4;
                int chunkZ = ((int) Math.floor(transform.getPosition().getZ())) >> 4;

                ChunkPos pos = new ChunkPos(world.getName(), chunkX, chunkZ);

                claimManager.claim(pos, playerRef.getUuid().toString());
                player.sendMessage(Message.raw("§aChunk claimed."));
            } catch (Exception e) {
                player.sendMessage(Message.raw("§c" + e.getMessage()));
            }
        }, world);
    }
}