package codes.wunder.hytaleregions;

import codes.wunder.hytaleregions.claim.ClaimManager;
import codes.wunder.hytaleregions.commands.ClaimCommand;
import codes.wunder.hytaleregions.ecs.BlockBreakSystem;
import codes.wunder.hytaleregions.ecs.adapters.HytaleChunkResolver;
import codes.wunder.hytaleregions.ecs.protection.ClaimProtection;
import codes.wunder.hytaleregions.persistence.ClaimStorage;
import codes.wunder.hytaleregions.persistence.YamlClaimStorage;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class HytaleRegions extends JavaPlugin {

    private static HytaleRegions instance;

    private final ClaimManager claimManager;
    private final ClaimStorage storage;

    public HytaleRegions(@NotNull JavaPluginInit init) {
        super(init);
        instance = this;

        Path dataPath = init.getDataDirectory();

        this.claimManager = new ClaimManager();
        this.storage = new YamlClaimStorage(dataPath);

        // load persisted claims
        claimManager.loadAll(storage.loadAll());

        // persist on every change
        claimManager.setOnChange(() ->
                storage.saveAll(claimManager.snapshot())
        );
    }

    public static HytaleRegions getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        registerSystems();
        registerCommands();
    }

    private void registerSystems() {
        ClaimProtection protection = new ClaimProtection(claimManager);
        HytaleChunkResolver chunkResolver = new HytaleChunkResolver();
        getEntityStoreRegistry().registerSystem(
                new BlockBreakSystem(protection, chunkResolver)
        );
    }

    private void registerCommands() {
        getCommandRegistry().registerCommand(new ClaimCommand(claimManager));
    }
}