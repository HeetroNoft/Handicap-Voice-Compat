package com.handicapvoice;

/**
 * Default profiles and the profiles this client applies, as received from the server (see {@link Config}).
 */
public final class VoiceConfig {
	public static final VoiceProfile DEFAULT_MUTE = new VoiceProfile(0.7F, 0.9F, 0.85F);
	public static final VoiceProfile DEFAULT_DEAF = new VoiceProfile(0.3F, 0.95F, 0.2F);
	public static final VoiceProfile DEFAULT_PARTIAL = new VoiceProfile(0.2F, 0.6F, 0F);

	public record Profiles(VoiceProfile mute, VoiceProfile deaf, VoiceProfile partialDeafness) {
		public static final Profiles DEFAULT = new Profiles(DEFAULT_MUTE, DEFAULT_DEAF, DEFAULT_PARTIAL);
	}

	private static volatile Profiles synced = Profiles.DEFAULT;

	private VoiceConfig() {
	}

	public static Profiles synced() {
		return synced;
	}

	public static void setSynced(Profiles profiles) {
		synced = profiles;
	}
}
