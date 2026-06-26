package com.perfectlan.client;

import com.perfectlan.client.config.PerfectLANConfig;
import com.perfectlan.network.PausePayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import com.perfectlan.client.server.ServerProcessManager;

public class PerfectLANClient implements ClientModInitializer {

    private static int lastSimulationDistance = -1;

    @Override
    public void onInitializeClient() {
        // Load config when client starts
        PerfectLANConfig.load();

        // Track and send simulation distance changes
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null && client.options != null) {
                int currentDistance = client.options.simulationDistance().get();
                if (lastSimulationDistance != currentDistance) {
                    lastSimulationDistance = currentDistance;
                    if (ClientPlayNetworking.canSend(com.perfectlan.network.SimulationDistancePayload.TYPE)) {
                        ClientPlayNetworking.send(new com.perfectlan.network.SimulationDistancePayload(currentDistance));
                    }
                }
            }
        });

        // Stop background server when client disconnects or exits to main menu
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            ServerProcessManager.stopServer();
        });

        // Ensure background server shuts down when client closes completely
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ServerProcessManager.stopServer();
        }));
    }
}