package com.perfectlan.mixin;

import net.minecraft.network.protocol.common.ServerboundClientInformationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mixin(ServerGamePacketListenerImpl.class)
public class ViewDistanceMixin {
    private static final Logger LOGGER = LoggerFactory.getLogger("PerfectLAN-ViewDistance");

    @Shadow public ServerPlayer player;

    @Inject(method = "handleClientInformation", at = @At("RETURN"))
    private void onClientInfo(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        MinecraftServer server = ((ServerCommonPacketListenerImplAccessor) this).getServer();
        int newDistance = packet.information().viewDistance();
        
        // We consider the player from localhost as the host
        boolean isHost = this.player.getIpAddress().equals("127.0.0.1") || this.player.getIpAddress().equals("0:0:0:0:0:0:0:1");

        if (isHost && server.getPlayerList().getViewDistance() != newDistance && newDistance >= 2) {
            LOGGER.info("Host requested new render distance: {}. Updating server dynamically!", newDistance);
            server.getPlayerList().setViewDistance(newDistance);
        }
    }
}
