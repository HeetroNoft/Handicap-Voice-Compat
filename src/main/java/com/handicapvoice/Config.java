package com.handicapvoice;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

/**
 * Handicap Voice Compat config, registered with AutoConfig (Cloth Config) and stored in config/handicapvoice.toml.
 * The config screen is generated from these fields, the file is written by {@link CommentedTomlSerializer}.
 * The server's values are sent to every client, so players can't weaken them locally.
 */
@me.shedaniel.autoconfig.annotation.Config(name = HandicapVoice.MOD_ID)
public class Config implements ConfigData {

	@Comment("Alters what a mute player sends in Simple Voice Chat.")
	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	public Profile mute = new Profile(VoiceConfig.DEFAULT_MUTE);

	@Comment("Alters what a deaf player hears in Simple Voice Chat.")
	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	public Profile deaf = new Profile(VoiceConfig.DEFAULT_DEAF);

	@Comment("Alters what a partially deaf player hears in Simple Voice Chat.")
	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	public Profile partialDeafness = new Profile(VoiceConfig.DEFAULT_PARTIAL);

	public static class Profile {
		@Comment("Defines the volume of the voice: 1 = unchanged, 0 = silent.\n Range: 0.0 - 1.0")
		@ConfigEntry.Gui.Tooltip
		public float volume;

		@Comment("Defines how muffled the voice is: 0 = clear, 1 = only a low rumble remains.\n Range: 0.0 - 1.0")
		@ConfigEntry.Gui.Tooltip
		public float muffle;

		@Comment("Defines how garbled the voice is: 0 = clean, 1 = unintelligible.\n Range: 0.0 - 1.0")
		@ConfigEntry.Gui.Tooltip
		public float distortion;

		public Profile() {
		}

		Profile(VoiceProfile defaults) {
			volume = defaults.volume();
			muffle = defaults.muffle();
			distortion = defaults.distortion();
		}

		public VoiceProfile toVoiceProfile() {
			return new VoiceProfile(volume, muffle, distortion);
		}

		/**
		 * Values out of range are reset to their default
		 */
		void validate(String name, Profile defaults) {
			volume = checkRange(name + ".volume", volume, defaults.volume);
			muffle = checkRange(name + ".muffle", muffle, defaults.muffle);
			distortion = checkRange(name + ".distortion", distortion, defaults.distortion);
		}
	}

	private static ConfigHolder<Config> holder;

	/**
	 * Loads the file and rewrites it, so missing values and comments are added
	 */
	public static void register() {
		holder = AutoConfig.register(Config.class, CommentedTomlSerializer::new);
		holder.save();
	}

	public static ConfigHolder<Config> holder() {
		return holder;
	}

	public static Config get() {
		return holder.getConfig();
	}

	public VoiceConfig.Profiles profiles() {
		return new VoiceConfig.Profiles(mute.toVoiceProfile(), deaf.toVoiceProfile(), partialDeafness.toVoiceProfile());
	}

	@Override
	public void validatePostLoad() {
		Config defaults = new Config();
		if (mute == null) {
			mute = defaults.mute;
		}
		if (deaf == null) {
			deaf = defaults.deaf;
		}
		if (partialDeafness == null) {
			partialDeafness = defaults.partialDeafness;
		}
		mute.validate("mute", defaults.mute);
		deaf.validate("deaf", defaults.deaf);
		partialDeafness.validate("partialDeafness", defaults.partialDeafness);
	}

	private static float checkRange(String name, float value, float defaultValue) {
		if (Float.isNaN(value) || value < 0F || value > 1F) {
			HandicapVoice.LOGGER.warn("Config value {} = {} is outside 0.0 - 1.0, using the default {}", name, value, defaultValue);
			return defaultValue;
		}
		return value;
	}
}
