package codes.wunder.hytaleregions.persistence;

import codes.wunder.hytaleregions.claim.ChunkPos;
import codes.wunder.hytaleregions.claim.Claim;

import java.util.Map;

public interface ClaimStorage {

    Map<ChunkPos, Claim> loadAll();

    void saveAll(Map<ChunkPos, Claim> claims);
}