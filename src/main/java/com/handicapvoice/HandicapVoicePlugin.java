package com.handicapvoice;

import com.handicap.Handicap;
import com.handicap.client.ClientState;

import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientReceiveSoundEvent;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple Voice Chat plugin. Both hooks run on the client, reading Handicap's synced client state:
 * a mute player's microphone is garbled before it is sent, and a deaf player's incoming audio is
 * filtered before it is played. A mute player talking to a deaf one therefore gets both filters.
 */
public class HandicapVoicePlugin implements VoicechatPlugin {
	/** Run after other plugins so we filter the final audio. */
	private static final int PRIORITY = -1000;

	private static final VoiceFilter MICROPHONE = new VoiceFilter();
	private static final Map<UUID, VoiceFilter> RECEIVED = new ConcurrentHashMap<>();

	@Override
	public String getPluginId() {
		return HandicapVoice.MOD_ID;
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(ClientSoundEvent.class, HandicapVoicePlugin::onMicrophone, PRIORITY);
		// Events are dispatched by exact class, so each received sound kind needs its own registration.
		registration.registerEvent(ClientReceiveSoundEvent.EntitySound.class, HandicapVoicePlugin::onReceive, PRIORITY);
		registration.registerEvent(ClientReceiveSoundEvent.LocationalSound.class, HandicapVoicePlugin::onReceive, PRIORITY);
		registration.registerEvent(ClientReceiveSoundEvent.StaticSound.class, HandicapVoicePlugin::onReceive, PRIORITY);
	}

	public static void reset() {
		RECEIVED.clear();
	}

	private static void onMicrophone(ClientSoundEvent event) {
		short[] audio = event.getRawAudio();
		if (audio == null || audio.length == 0 || !ClientState.has(Handicap.MUTE)) {
			return;
		}
		event.setRawAudio(MICROPHONE.process(audio, VoiceConfig.synced().mute()));
	}

	private static void onReceive(ClientReceiveSoundEvent event) {
		short[] audio = event.getRawAudio();
		if (audio == null || audio.length == 0) {
			// End of transmission: the channel's filter state is no longer needed.
			RECEIVED.remove(event.getId());
			return;
		}
		VoiceProfile profile = receivingProfile();
		if (profile == null) {
			return;
		}
		VoiceFilter filter = RECEIVED.computeIfAbsent(event.getId(), id -> new VoiceFilter());
		event.setRawAudio(filter.process(audio, profile));
	}

	private static VoiceProfile receivingProfile() {
		// Same precedence as Handicap's game sound handling: Deaf overrides Partial Deafness.
		if (ClientState.has(Handicap.DEAF)) {
			return VoiceConfig.synced().deaf();
		}
		if (ClientState.has(Handicap.PARTIAL_DEAFNESS)) {
			return VoiceConfig.synced().partialDeafness();
		}
		return null;
	}
}
