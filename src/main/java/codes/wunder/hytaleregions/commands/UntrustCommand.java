package codes.wunder.hytaleregions.commands;

import codes.wunder.hytaleregions.claim.ChunkPos;
import codes.wunder.hytaleregions.claim.ClaimManager;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.Message;
import com.hypixel.hytale.server.core.NameMatching;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractAsyncCommand;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.permissions.PermissionsModule;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.Universe;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class UntrustCommand extends AbstractAsyncCommand {

    private final ClaimManager claimManager;

    public UntrustCommand(ClaimManager claimManager) {
        super("untrust", "Remove a trusted player from the current claim");
        this.claimManager = claimManager;
        this.setAllowsExtraArguments(true);
    }

    @Override
    protected @NotNull CompletableFuture<Void> executeAsync(@NotNull CommandContext ctx) {
        if (!ctx.isPlayer()) return CompletableFuture.completedFuture(null);

        String[] args = ctx.getInputString().split("\\s+");
        if (args.length < 2) {
            ctx.sendMessage(Message.raw("§cUsage: /untrust <player>"));
            return CompletableFuture.completedFuture(null);
        }

        Player player = ctx.senderAs(Player.class);
        Ref<EntityStore> ref = ctx.senderAsPlayerRef();
        if (ref == null || !ref.isValid()) return CompletableFuture.completedFuture(null);

        Store<EntityStore> store = ref.getStore();
        PlayerRef ownerRef =
                store.getComponent(ref, PlayerRef.getComponentType());

        if (!PermissionsModule.get().hasPermission(
                ownerRef.getUuid(), "hytaleregions.untrust")) {
            player.sendMessage(Message.raw("§cYou do not have permission."));
            return CompletableFuture.completedFuture(null);
        }

        String targetName = args[1];
        World world = store.getExternalData().getWorld();

        return CompletableFuture.runAsync(() -> {
            try {
                PlayerRef targetPlayer =
                        Universe.get().getPlayerByUsername(
                                targetName, NameMatching.EXACT_IGNORE_CASE);

                if (targetPlayer == null) {
                    player.sendMessage(
                            Message.raw("§cPlayer not found: " + targetName));
                    return;
                }

                UUID targetUuid = targetPlayer.getUuid();

                TransformComponent transform =
                        store.getComponent(ref, TransformComponent.getComponentType());

                int chunkX = ((int) Math.floor(transform.getPosition().getX())) >> 4;
                int chunkZ = ((int) Math.floor(transform.getPosition().getZ())) >> 4;

                ChunkPos pos = new ChunkPos(
                        world.getName(), chunkX, chunkZ);

                claimManager.removeUser(
                        pos,
                        ownerRef.getUuid().toString(),
                        targetUuid.toString()
                );

                player.sendMessage(
                        Message.raw("§aRemoved trust from " + targetName + "."));
            } catch (Exception e) {
                player.sendMessage(
                        Message.raw("§c" + e.getMessage()));
            }
        }, world);
    }
}
