package com.handicapvoice.client;

import com.handicapvoice.ConfigPayload;
import com.handicapvoice.HandicapVoicePlugin;
import com.handicapvoice.VoiceConfig;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class HandicapVoiceClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientPlayNetworking.registerGlobalReceiver(ConfigPayload.TYPE, (payload, context) ->
			VoiceConfig.setSynced(payload.profiles()));

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			VoiceConfig.setSynced(VoiceConfig.Profiles.DEFAULT);
			HandicapVoicePlugin.reset();
		});
	}
}
