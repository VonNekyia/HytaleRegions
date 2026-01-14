package codes.wunder.hytaleregions.ecs.protection;

import codes.wunder.hytaleregions.claim.ChunkPos;
import codes.wunder.hytaleregions.claim.Claim;
import codes.wunder.hytaleregions.claim.ClaimManager;

public final class ClaimProtection {

    private final ClaimManager claimManager;

    public ClaimProtection(ClaimManager claimManager) {
        this.claimManager = claimManager;
    }

    public ProtectionResult canBuild(
            ChunkPos pos,
            String playerId
    ) {
        Claim claim = claimManager.getClaim(pos);

        if (claim == null) {
            return ProtectionResult.ALLOW;
        }

        return claim.isMember(playerId)
                ? ProtectionResult.ALLOW
                : ProtectionResult.DENY;
    }
}