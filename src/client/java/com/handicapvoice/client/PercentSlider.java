package com.handicapvoice.client;

import java.util.List;

import com.handicapvoice.Config;

import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.autoconfig.util.Utils;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;

import net.minecraft.network.chat.Component;

/**
 * Shows the {@link Config.Percent} float fields (0.0 - 1.0) as a 0 - 100% slider in the config screen.
 * AutoConfig only provides sliders for int and long fields.
 */
final class PercentSlider {
	private PercentSlider() {
	}

	static void register() {
		AutoConfigClient.getGuiRegistry(Config.class).registerAnnotationProvider(
			(i18n, field, config, defaults, registry) -> List.of(ConfigEntryBuilder.create()
				.startIntSlider(Component.translatable(i18n), toPercent(Utils.getUnsafely(field, config, 0F)), 0, 100)
				.setDefaultValue(() -> toPercent(Utils.getUnsafely(field, defaults, 0F)))
				.setSaveConsumer(percent -> Utils.setUnsafely(field, config, percent / 100F))
				.setTextGetter(percent -> Component.literal(percent + "%"))
				.build()),
			field -> field.getType() == float.class,
			Config.Percent.class);
	}

	private static int toPercent(float value) {
		return Math.round(value * 100F);
	}
}
