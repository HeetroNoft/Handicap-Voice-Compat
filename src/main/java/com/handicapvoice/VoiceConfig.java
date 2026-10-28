package com.handicapvoice;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Server-side config file, sent to every client on join so players can't weaken it locally.
 */
public final class VoiceConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public static final VoiceProfile DEFAULT_MUTE = new VoiceProfile(0.7F, 0.9F, 0.85F);
	public static final VoiceProfile DEFAULT_DEAF = new VoiceProfile(0.3F, 0.95F, 0.2F);
	public static final VoiceProfile DEFAULT_PARTIAL = new VoiceProfile(0.2F, 0.6F, 0F);

	public record Profiles(VoiceProfile mute, VoiceProfile deaf, VoiceProfile partialDeafness) {
		public static final Profiles DEFAULT = new Profiles(DEFAULT_MUTE, DEFAULT_DEAF, DEFAULT_PARTIAL);
	}

	private static final class Data {
		String info = "Every value goes from 0.0 to 1.0. volume: 1 = unchanged, 0 = silent. "
			+ "muffle: 0 = off, 1 = only a low rumble remains. distortion: 0 = off, 1 = unintelligible. "
			+ "mute alters what a mute player sends, deaf/partialDeafness alter what those players hear. "
			+ "Changes apply when the server (or singleplayer world) restarts.";
		Entry mute = Entry.of(DEFAULT_MUTE);
		Entry deaf = Entry.of(DEFAULT_DEAF);
		Entry partialDeafness = Entry.of(DEFAULT_PARTIAL);
	}

	private static final class Entry {
		float volume;
		float muffle;
		float distortion;

		static Entry of(VoiceProfile profile) {
			Entry e = new Entry();
			e.volume = profile.volume();
			e.muffle = profile.muffle();
			e.distortion = profile.distortion();
			return e;
		}

		static VoiceProfile toProfile(Entry e, VoiceProfile fallback) {
			return e == null ? fallback : new VoiceProfile(e.volume, e.muffle, e.distortion);
		}
	}

	/** What the server enforces (loaded from disk). */
	private static volatile Profiles server = Profiles.DEFAULT;
	/** What this client applies (received from the server). */
	private static volatile Profiles synced = Profiles.DEFAULT;

	private VoiceConfig() {
	}

	public static void load(Path directory) {
		Path file = directory.resolve(HandicapVoice.MOD_ID + ".json");
		try {
			if (Files.exists(file)) {
				try (Reader reader = Files.newBufferedReader(file)) {
					Data data = GSON.fromJson(reader, Data.class);
					if (data != null) {
						server = new Profiles(
							Entry.toProfile(data.mute, DEFAULT_MUTE),
							Entry.toProfile(data.deaf, DEFAULT_DEAF),
							Entry.toProfile(data.partialDeafness, DEFAULT_PARTIAL));
						return;
					}
				}
			} else {
				Files.createDirectories(directory);
				Files.writeString(file, GSON.toJson(new Data()));
			}
		} catch (Exception e) {
			HandicapVoice.LOGGER.error("Could not read {}", file, e);
		}
		server = Profiles.DEFAULT;
	}

	public static Profiles server() {
		return server;
	}

	public static Profiles synced() {
		return synced;
	}

	public static void setSynced(Profiles profiles) {
		synced = profiles;
	}
}
