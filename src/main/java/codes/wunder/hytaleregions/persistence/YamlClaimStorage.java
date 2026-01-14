package codes.wunder.hytaleregions.persistence;

import codes.wunder.hytaleregions.claim.ChunkPos;
import codes.wunder.hytaleregions.claim.Claim;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class YamlClaimStorage implements ClaimStorage {

    private final Path file;
    private final Yaml yaml = new Yaml();

    public YamlClaimStorage(Path dataDir) {
        this.file = dataDir.resolve("claims.yml");
    }

    @Override
    public Map<ChunkPos, Claim> loadAll() {
        if (!Files.exists(file)) {
            return new HashMap<>();
        }

        try (InputStream in = Files.newInputStream(file)) {
            Map<String, Object> root = yaml.load(in);
            if (root == null) {
                return new HashMap<>();
            }

            Map<ChunkPos, Claim> result = new HashMap<>();

            for (Map.Entry<String, Object> entry : root.entrySet()) {
                String key = entry.getKey(); // world:x:z
                Map<String, Object> data = (Map<String, Object>) entry.getValue();

                String[] parts = key.split(":");
                String world = parts[0];
                int x = Integer.parseInt(parts[1]);
                int z = Integer.parseInt(parts[2]);

                String owner = (String) data.get("owner");
                List<String> users = (List<String>) data.getOrDefault("users", List.of());

                Claim claim = new Claim(owner);
                users.forEach(claim::addUser);

                result.put(new ChunkPos(world, x, z), claim);
            }

            return result;

        } catch (IOException e) {
            throw new RuntimeException("Failed to load claims.yml", e);
        }
    }

    @Override
    public void saveAll(Map<ChunkPos, Claim> claims) {
        Map<String, Object> root = new HashMap<>();

        for (Map.Entry<ChunkPos, Claim> entry : claims.entrySet()) {
            ChunkPos pos = entry.getKey();
            Claim claim = entry.getValue();

            Map<String, Object> data = new HashMap<>();
            data.put("owner", claim.owner());
            data.put("users", claim.usersView().stream().toList());

            String key = pos.world() + ":" + pos.x() + ":" + pos.z();
            root.put(key, data);
        }

        try {
            Files.createDirectories(file.getParent());
            try (OutputStreamWriter out =
                         new OutputStreamWriter(Files.newOutputStream(file))) {
                yaml.dump(root, out);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save claims.yml", e);
        }
    }
}