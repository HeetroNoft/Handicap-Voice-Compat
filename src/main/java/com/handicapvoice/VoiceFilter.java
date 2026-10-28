package com.handicapvoice;

/**
 * Stateful DSP chain for one audio stream (48 kHz, 16 bit mono PCM frames):
 * distortion (sample-and-hold + ring modulation + overdrive), then a cascaded low-pass, then gain.
 * Keep one instance per stream so the filter state stays continuous between frames.
 */
public final class VoiceFilter {
	private static final float SAMPLE_RATE = 48000F;
	private static final int LOW_PASS_STAGES = 4;
	private static final float MAX_CUTOFF = 8000F;
	private static final float MIN_CUTOFF = 120F;
	private static final float CARRIER_HZ = 90F;
	private static final int MAX_HOLD = 12;
	private static final float MAX_DRIVE = 6F;
	private static final float TWO_PI = (float) (2 * Math.PI);

	private final float[] lowPass = new float[LOW_PASS_STAGES];
	private float carrierPhase;
	private float heldSample;
	private int holdCounter;

	public short[] process(short[] input, VoiceProfile profile) {
		float distortion = profile.distortion();
		int hold = 1 + Math.round(distortion * (MAX_HOLD - 1));
		float drive = 1F + distortion * MAX_DRIVE;
		float driveNorm = (float) Math.tanh(drive);
		float carrierStep = TWO_PI * CARRIER_HZ / SAMPLE_RATE;
		float alpha = profile.muffle() > 0F ? lowPassAlpha(profile.muffle()) : 1F;

		short[] output = new short[input.length];
		for (int i = 0; i < input.length; i++) {
			float x = input[i] / 32768F;

			if (distortion > 0F) {
				if (holdCounter <= 0) {
					heldSample = x;
					holdCounter = hold;
				}
				holdCounter--;
				x = heldSample;

				x *= 1F - distortion + distortion * (float) Math.sin(carrierPhase);
				carrierPhase += carrierStep;
				if (carrierPhase > TWO_PI) {
					carrierPhase -= TWO_PI;
				}

				x = (float) Math.tanh(x * drive) / driveNorm;
			}

			if (alpha < 1F) {
				for (int s = 0; s < LOW_PASS_STAGES; s++) {
					lowPass[s] += alpha * (x - lowPass[s]);
					x = lowPass[s];
				}
			}

			x *= profile.volume();
			output[i] = (short) Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, Math.round(x * 32768F)));
		}
		return output;
	}

	/** Maps muffle 0..1 to a cutoff from MAX_CUTOFF down to MIN_CUTOFF (log scale) and returns the one-pole coefficient. */
	private static float lowPassAlpha(float muffle) {
		double cutoff = MAX_CUTOFF * Math.pow(MIN_CUTOFF / MAX_CUTOFF, muffle);
		return (float) (1 - Math.exp(-2 * Math.PI * cutoff / SAMPLE_RATE));
	}
}
