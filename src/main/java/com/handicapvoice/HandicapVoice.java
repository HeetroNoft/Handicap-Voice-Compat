package com.handicapvoice;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HandicapVoice implements ModInitializer {
	public static final String MOD_ID = "handicapvoice";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static volatile MinecraftServer server;

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}

	@Override
	public void onInitialize() {
		PayloadTypeRegistry.clientboundPlay().register(ConfigPayload.TYPE, ConfigPayload.CODEC);

		Config.register();
		// Saving from the config screen (singleplayer / LAN host) applies to everyone connected right away.
		Config.holder().registerSaveListener((holder, config) -> {
			config.validatePostLoad();
			MinecraftServer s = server;
			if (s != null) {
				s.execute(() -> s.getPlayerList().getPlayers().forEach(HandicapVoice::sendConfig));
			}
			return InteractionResult.PASS;
		});

		// Re-read the file so edits made while the server was off are picked up.
		ServerLifecycleEvents.SERVER_STARTING.register(s -> Config.holder().load());
		ServerLifecycleEvents.SERVER_STARTED.register(s -> server = s);
		ServerLifecycleEvents.SERVER_STOPPED.register(s -> server = null);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, s) -> sendConfig(handler.getPlayer()));
	}

	private static void sendConfig(ServerPlayer player) {
		if (ServerPlayNetworking.canSend(player, ConfigPayload.TYPE)) {
			ServerPlayNetworking.send(player, ConfigPayload.of(Config.get().profiles()));
		}
	}
}
