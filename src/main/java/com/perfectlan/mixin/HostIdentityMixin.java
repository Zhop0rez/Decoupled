package com.perfectlan.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.UUID;

@Mixin(ServerLoginPacketListenerImpl.class)
public class HostIdentityMixin {

    @Shadow @Final private MinecraftServer server;
    @Shadow @Final public Connection connection;

    @ModifyVariable(method = "startClientVerification", at = @At("HEAD"), argsOnly = true)
    private GameProfile modifyHostProfile(GameProfile original) {
        String ip = this.connection.getRemoteAddress().toString();
        
        // Check if the connection is from the localhost (the host running Perfect LAN)
        if (ip.contains("127.0.0.1") || ip.contains("0:0:0:0:0:0:0:1") || ip.contains("local")) {
            UUID spUUID = this.server.getWorldData().getSinglePlayerUUID();
            
            // If the world has a registered singleplayer owner and the host's UUID changed
            if (spUUID != null && !original.id().equals(spUUID)) {
                // If the Host is ALREADY on the server, this must be an alt account connecting from the same PC.
                // We shouldn't forge the UUID, otherwise the Host will get kicked with "Logged in from another location".
                if (this.server.getPlayerList().getPlayer(spUUID) != null) {
                    return original;
                }

                // We forge a new GameProfile using the original Singleplayer UUID
                // This forces the server to give the host their original inventory and stats back!
                // We pass original.properties() to preserve the skin and other data.
                GameProfile forgedProfile = new GameProfile(spUUID, original.name(), original.properties());
                
                return forgedProfile;
            }
        }
        
        return original;
    }
}
