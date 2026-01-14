package codes.wunder.hytaleregions.claim;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ClaimManager {

    private final Map<ChunkPos, Claim> claims = new HashMap<>();

    /**
     * Called whenever claim state changes.
     * Wired by bootstrap to persistence.
     */
    private Runnable onChange = () -> {
    };

    /**
     * Allows persistence to register a callback
     * that is invoked on every mutation.
     */
    public void setOnChange(Runnable onChange) {
        this.onChange = onChange != null ? onChange : () -> {
        };
    }

    /* ------------------------------------------------------------
     * READ API (used by ECS / commands)
     * ------------------------------------------------------------ */

    /**
     * Returns the claim at the given chunk position,
     * or null if the chunk is not claimed.
     */
    public Claim getClaim(ChunkPos pos) {
        return claims.get(pos);
    }

    /**
     * Returns true if the chunk is claimed.
     */
    public boolean isClaimed(ChunkPos pos) {
        return claims.containsKey(pos);
    }

    /**
     * Read-only snapshot for persistence.
     */
    public Map<ChunkPos, Claim> snapshot() {
        return Collections.unmodifiableMap(claims);
    }

    /* ------------------------------------------------------------
     * WRITE API (used by commands)
     * ------------------------------------------------------------ */

    /**
     * Claims a chunk for the given owner.
     */
    public void claim(ChunkPos pos, String ownerId) {
        if (claims.containsKey(pos)) {
            throw new ClaimException("This chunk is already claimed.");
        }

        claims.put(pos, new Claim(ownerId));
        onChange.run();
    }

    /**
     * Unclaims a chunk. Only the owner may do this.
     */
    public void unclaim(ChunkPos pos, String actorId) {
        Claim claim = requireClaim(pos);

        if (!claim.isOwner(actorId)) {
            throw new ClaimException("You are not the owner of this claim.");
        }

        claims.remove(pos);
        onChange.run();
    }

    /**
     * Adds a trusted user to a claim.
     */
    public void addUser(ChunkPos pos, String ownerId, String targetId) {
        Claim claim = requireClaim(pos);

        if (!claim.isOwner(ownerId)) {
            throw new ClaimException("You are not the owner of this claim.");
        }

        claim.addUser(targetId);
        onChange.run();
    }

    /**
     * Removes a trusted user from a claim.
     */
    public void removeUser(ChunkPos pos, String ownerId, String targetId) {
        Claim claim = requireClaim(pos);

        if (!claim.isOwner(ownerId)) {
            throw new ClaimException("You are not the owner of this claim.");
        }

        claim.removeUser(targetId);
        onChange.run();
    }

    /* ------------------------------------------------------------
     * PERSISTENCE HOOKS
     * ------------------------------------------------------------ */

    /**
     * Loads all claims from persistence.
     * Called once during bootstrap.
     */
    public void loadAll(Map<ChunkPos, Claim> loadedClaims) {
        claims.clear();
        claims.putAll(loadedClaims);
    }

    /* ------------------------------------------------------------
     * INTERNAL HELPERS
     * ------------------------------------------------------------ */

    private Claim requireClaim(ChunkPos pos) {
        Claim claim = claims.get(pos);
        if (claim == null) {
            throw new ClaimException("This chunk is not claimed.");
        }
        return claim;
    }
}