package com.handicapvoice;

import fr.heetstudio.confee.api.Confee;
import fr.heetstudio.confee.api.ConfeeConfig;
import fr.heetstudio.confee.api.annotation.Comment;
import fr.heetstudio.confee.api.annotation.Icon;
import fr.heetstudio.confee.api.annotation.Range;
import fr.heetstudio.confee.api.annotation.Section;
import fr.heetstudio.confee.api.annotation.Slider;

/**
 * Handicap Voice Compat config, registered with Confee and stored in config/handicapvoice.toml.
 * Confee writes the file and generates the config screen from these fields.
 * The server's values are sent to every client, so players can't weaken them locally.
 */
@fr.heetstudio.confee.api.annotation.Config(modId = HandicapVoice.MOD_ID)
public class Config {

	@Comment("Alters what a mute player sends in Simple Voice Chat.")
	@Icon("minecraft:goat_horn")
	public Profile mute = new Profile(VoiceConfig.DEFAULT_MUTE);

	@Comment("Alters what a deaf player hears in Simple Voice Chat.")
	@Icon("minecraft:sculk_sensor")
	public Profile deaf = new Profile(VoiceConfig.DEFAULT_DEAF);

	@Comment("Alters what a partially deaf player hears in Simple Voice Chat.")
	@Icon("minecraft:amethyst_shard")
	public Profile partialDeafness = new Profile(VoiceConfig.DEFAULT_PARTIAL);

	@Section
	public static class Profile {
		@Comment("Defines the volume of the voice: 1 = unchanged, 0 = silent.")
		@Range(min = 0, max = 1)
		@Slider(step = 0.05)
		public float volume;

		@Comment("Defines how muffled the voice is: 0 = clear, 1 = only a low rumble remains.")
		@Range(min = 0, max = 1)
		@Slider(step = 0.05)
		public float muffle;

		@Comment("Defines how garbled the voice is: 0 = clean, 1 = unintelligible.")
		@Range(min = 0, max = 1)
		@Slider(step = 0.05)
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
	}

	private static ConfeeConfig<Config> config;

	/**
	 * Loads the file (Confee creates it with every key and comment, and clamps values out of range)
	 */
	public static void register() {
		config = Confee.register(Config.class);
	}

	public static ConfeeConfig<Config> config() {
		return config;
	}

	public static Config get() {
		return config.get();
	}

	public VoiceConfig.Profiles profiles() {
		return new VoiceConfig.Profiles(mute.toVoiceProfile(), deaf.toVoiceProfile(), partialDeafness.toVoiceProfile());
	}
}
