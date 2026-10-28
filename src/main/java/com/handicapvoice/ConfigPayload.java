package com.handicapvoice;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ConfigPayload(VoiceProfile mute, VoiceProfile deaf, VoiceProfile partialDeafness) implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<ConfigPayload> TYPE = new CustomPacketPayload.Type<>(HandicapVoice.id("config"));

	public static final StreamCodec<ByteBuf, ConfigPayload> CODEC = StreamCodec.composite(
		VoiceProfile.CODEC, ConfigPayload::mute,
		VoiceProfile.CODEC, ConfigPayload::deaf,
		VoiceProfile.CODEC, ConfigPayload::partialDeafness,
		ConfigPayload::new);

	public static ConfigPayload of(VoiceConfig.Profiles profiles) {
		return new ConfigPayload(profiles.mute(), profiles.deaf(), profiles.partialDeafness());
	}

	public VoiceConfig.Profiles profiles() {
		return new VoiceConfig.Profiles(mute, deaf, partialDeafness);
	}

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
