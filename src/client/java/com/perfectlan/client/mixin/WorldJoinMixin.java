package com.perfectlan.client.mixin;

import com.perfectlan.client.config.PerfectLANConfig;
import com.perfectlan.client.server.ServerProcessManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.worldselection.WorldSelectionList;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(WorldSelectionList.WorldListEntry.class)
public abstract class WorldJoinMixin {

    @Shadow public abstract LevelSummary getLevelSummary();
    @Shadow @org.spongepowered.asm.mixin.Final private net.minecraft.client.gui.screens.Screen screen;

    @Inject(method = "joinWorld", at = @At("HEAD"), cancellable = true)
    private void onJoinWorld(CallbackInfo ci) {
        if (!PerfectLANConfig.get().enablePerfectLAN) {
            return; // Let vanilla handle it
        }

        System.out.println("[PerfectLAN] Intercepted Singleplayer world join!");

        Minecraft client = Minecraft.getInstance();
        LevelSummary summary = this.getLevelSummary();
        
        // Find the world path
        Path savesDir = client.getLevelSource().getBaseDir();
        Path worldDir = savesDir.resolve(summary.getLevelId());

        // 1. Cancel the vanilla integrated server launch
        ci.cancel();
        
        // 2. Find an available port dynamically
        int foundPort = 25565;
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            foundPort = socket.getLocalPort();
        } catch (java.io.IOException e) {
            System.err.println("[PerfectLAN] Failed to find free port, using default 25565");
        }
        final int port = foundPort;
        
        System.out.println("[PerfectLAN] Starting background server on port " + port);

        // Show a loading screen so the player knows the server is starting
        net.minecraft.client.gui.screens.ProgressScreen progressScreen = new net.minecraft.client.gui.screens.ProgressScreen(false);
        progressScreen.progressStartNoAbort(net.minecraft.network.chat.Component.literal("Starting Perfect LAN Server..."));
        client.setScreenAndShow(progressScreen);

        ServerProcessManager.startServer(worldDir, port, () -> {
            client.execute(() -> {
                // 3. Connect client to localhost:port
                ServerAddress address = new ServerAddress("127.0.0.1", port);
                ServerData serverData = new ServerData(summary.getLevelName(), "127.0.0.1:" + port, ServerData.Type.OTHER);
                ConnectScreen.startConnecting(this.screen, client, address, serverData, false, null);
            });
        });
    }
}
