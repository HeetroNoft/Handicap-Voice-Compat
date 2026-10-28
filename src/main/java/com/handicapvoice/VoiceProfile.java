package com.handicapvoice;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * How one handicap alters voice audio. Every value is clamped to 0..1.
 *
 * @param volume     gain applied last (1 = unchanged)
 * @param muffle     low-pass strength (0 = off, 1 = only a low rumble remains)
 * @param distortion garbling strength (0 = off, 1 = unintelligible)
 */
public record VoiceProfile(float volume, float muffle, float distortion) {
	public static final StreamCodec<ByteBuf, VoiceProfile> CODEC = StreamCodec.composite(
		ByteBufCodecs.FLOAT, VoiceProfile::volume,
		ByteBufCodecs.FLOAT, VoiceProfile::muffle,
		ByteBufCodecs.FLOAT, VoiceProfile::distortion,
		VoiceProfile::new);

	public VoiceProfile {
		volume = clamp(volume);
		muffle = clamp(muffle);
		distortion = clamp(distortion);
	}

	private static float clamp(float value) {
		return Float.isNaN(value) ? 0F : Math.max(0F, Math.min(1F, value));
	}
}
