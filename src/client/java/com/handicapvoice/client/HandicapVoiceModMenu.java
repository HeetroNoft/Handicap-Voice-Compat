package com.handicapvoice.client;

import com.handicapvoice.Config;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

import me.shedaniel.autoconfig.AutoConfigClient;

public class HandicapVoiceModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> AutoConfigClient.getConfigScreen(Config.class, parent).get();
	}
}
