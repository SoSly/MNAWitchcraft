package org.sosly.witchcraft.utils;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.sosly.witchcraft.Witchcraft;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChunkLoader {
    private static final TicketType<UUID> TICKET_TYPE = TicketType.create("mnaw:chunk_loader", 
            UUID::compareTo, 300);
    
    private final ServerLevel level;
    private final UUID ticketId;
    private final Set<ChunkPos> loadedChunks;
    
    public ChunkLoader(ServerLevel level, UUID ticketId) {
        this.level = level;
        this.ticketId = ticketId;
        this.loadedChunks = new HashSet<>();
    }
    
    public void loadChunksAround(Vec3 position) {
        if (level == null) {
            return;
        }
        
        Set<ChunkPos> newChunks = calculateRequiredChunks(position);
        
        removeUnneededChunks(newChunks);
        addNewChunks(newChunks);
        
        loadedChunks.clear();
        loadedChunks.addAll(newChunks);
    }
    
    public void unloadAll() {
        if (level == null) {
            return;
        }
        
        for (ChunkPos chunk : loadedChunks) {
            level.getChunkSource().removeRegionTicket(TICKET_TYPE, chunk, 1, ticketId);
        }
        loadedChunks.clear();
    }
    
    private Set<ChunkPos> calculateRequiredChunks(Vec3 position) {
        Set<ChunkPos> chunks = new HashSet<>();
        ChunkPos centerChunk = new ChunkPos((int) position.x >> 4, (int) position.z >> 4);
        
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                chunks.add(new ChunkPos(centerChunk.x + dx, centerChunk.z + dz));
            }
        }
        
        return chunks;
    }
    
    private void removeUnneededChunks(Set<ChunkPos> newChunks) {
        for (ChunkPos chunk : loadedChunks) {
            if (!newChunks.contains(chunk)) {
                level.getChunkSource().removeRegionTicket(TICKET_TYPE, chunk, 1, ticketId);
            }
        }
    }
    
    private void addNewChunks(Set<ChunkPos> newChunks) {
        for (ChunkPos chunk : newChunks) {
            if (!loadedChunks.contains(chunk)) {
                level.getChunkSource().addRegionTicket(TICKET_TYPE, chunk, 1, ticketId);
            }
        }
    }
}