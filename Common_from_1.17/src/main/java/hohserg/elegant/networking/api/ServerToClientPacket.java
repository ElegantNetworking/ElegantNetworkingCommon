package hohserg.elegant.networking.api;

import hohserg.elegant.networking.impl.Network;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Base interface for packet, which can be send from server to client
 */
public interface ServerToClientPacket extends IByteBufSerializable {
    /**
     * Called when the packet is received
     *
     * @param mc Minecraft class instance
     */
    @OnlyIn(Dist.CLIENT)
    void onReceive(Minecraft mc);

    /**
     * Use it for send packet instance to concrete player
     */
    default void sendToPlayer(ServerPlayer player) {
        Network.getNetwork().sendToPlayer(this, player);
    }

    /**
     * Use it for send packet instance to all players
     */
    default void sendToClients() {
        Network.getNetwork().sendToClients(this);

    }

    /**
     * Use it for send packet instance to all players around coordinates
     */
    default void sendPacketToAllAround(Level world, double x, double y, double z, double range) {
        Network.getNetwork().sendPacketToAllAround(this, world, x, y, z, range);
    }

    /**
     * Use it for send packet instance to all players in concrete dimension
     */
    default void sendToDimension(Level world) {
        Network.getNetwork().sendToDimension(this, world);
    }

    /**
     * Use it for send packet instance to all players in concrete chunk
     */
    default void sendToChunk(Level world, int chunkX, int chunkZ) {
        Network.getNetwork().sendToChunk(this, world, chunkX, chunkZ);
    }
}
