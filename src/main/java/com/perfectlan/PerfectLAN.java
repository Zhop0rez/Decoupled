package com.perfectlan;

import com.perfectlan.network.PausePayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PerfectLAN implements ModInitializer {
	public static final String MOD_ID = "perfectlan";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static boolean isOopPaused = false;

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing Perfect LAN (OOP-Server)!");

		// Register the custom payload type
		PayloadTypeRegistry.serverboundPlay().register(PausePayload.TYPE, PausePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(com.perfectlan.network.SimulationDistancePayload.TYPE, com.perfectlan.network.SimulationDistancePayload.CODEC);

		// Handle incoming pause packets from the client
		ServerPlayNetworking.registerGlobalReceiver(PausePayload.TYPE, (payload, context) -> {
			if (context.player() != null && context.server() != null) {
				net.minecraft.server.MinecraftServer server = context.server();
				
				server.execute(() -> {
					// Only allow pause if the player is the host AND they are the ONLY player online
					boolean shouldPause = payload.paused() && server.getPlayerCount() <= 1;
					
					if (server.tickRateManager().isFrozen() != shouldPause) {
						server.tickRateManager().setFrozen(shouldPause);
						if (shouldPause) {
							LOGGER.info("Client requested pause and is alone. Background server frozen via TickRateManager.");
						} else {
							LOGGER.info("Client unpaused or someone joined. Resuming world ticks.");
						}
					}
				});
			}
		});

		// Handle incoming simulation distance packets from the host
		ServerPlayNetworking.registerGlobalReceiver(com.perfectlan.network.SimulationDistancePayload.TYPE, (payload, context) -> {
			if (context.player() != null && context.server() != null) {
				net.minecraft.server.MinecraftServer server = context.server();
				
				server.execute(() -> {
					// We only trust the host to change the simulation distance dynamically
					String ip = context.player().getIpAddress();
					boolean isHost = ip.contains("127.0.0.1") || ip.contains("0:0:0:0:0:0:0:1") || ip.contains("local");
					
					if (isHost) {
						int newDistance = payload.distance();
						if (server.getPlayerList().getSimulationDistance() != newDistance) {
							LOGGER.info("Host requested new simulation distance: {}. Updating server dynamically!", newDistance);
							server.getPlayerList().setSimulationDistance(newDistance);
						}
					}
				});
			}
		});

		// Auto-shutdown when the last player leaves
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
			server.execute(() -> {
				// getPlayerCount() is updated after this event, so if it's <= 1, it means 0 players will be left
				if (server.getPlayerCount() <= 1) {
					LOGGER.info("Last player left! Shutting down background server automatically.");
					server.halt(false);
				}
			});
		});

		// Auto-OP the host
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			String ip = handler.player.getIpAddress();
			if (ip.equals("127.0.0.1") || ip.equals("0:0:0:0:0:0:0:1")) {
				server.execute(() -> {
					server.getPlayerList().op(new net.minecraft.server.players.NameAndId(handler.player.getGameProfile()));
					LOGGER.info("Granted operator privileges to the host: {}", handler.player.getName().getString());
					
					// Tell the host the port so friends can join
					int port = server.getPort();
					handler.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§a[Perfect LAN] §fServer is running on port: §e" + port));
					handler.player.sendSystemMessage(net.minecraft.network.chat.Component.literal("§7Friends can join using your IP address + port (e.g. 192.168.1.5:" + port + ")"));
				});
			}
		});
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
