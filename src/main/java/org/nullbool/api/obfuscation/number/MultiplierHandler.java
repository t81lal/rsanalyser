package org.nullbool.api.obfuscation.number;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplierHandler {

	private final Map<String, List<Integer>> decoders;
	private final Map<String, List<Integer>> encoders;

	public MultiplierHandler() {
		decoders = encoders = new HashMap<>();
	}

	public int getDecoder(final String key) {
		int mostUsed = 0;
		int highestFreq = 0;
		final List<Integer> multipliers = decoders.get(key);
		if (multipliers == null)
			return 0;
		for (final int modulus : multipliers) {
			final int count = (int) multipliers.stream().filter(i -> i.equals(modulus)).count();
			if (count > highestFreq) {
				highestFreq = count;
				mostUsed = modulus;
			}
		}
		return mostUsed;
	}

	public int getEncoder(final String key) {
		int mostUsed = 0;
		int highestFreq = 0;
		final List<Integer> multipliers = encoders.get(key);
		if (multipliers == null)
			return 0;
		for (final int modulus : multipliers) {
			final int count = (int) multipliers.stream().filter(i -> i.equals(modulus)).count();
			if (count > highestFreq) {
				highestFreq = count;
				mostUsed = modulus;
			}
		}
		return mostUsed;
	}

	public int inverseDecoder(final String key) {
		try {
			final BigInteger num = BigInteger.valueOf(getDecoder(key));
			return num.modInverse(new BigInteger(String.valueOf(1L << 32))).intValue();
		} catch (final Exception e) {
			return 0;
		}
	}

	public void addDecoder(String key, int decoder) {
		if (!decoders.containsKey(key))
			decoders.put(key, new ArrayList<>());
		decoders.get(key).add(decoder);
	}

	public void addEncoder(String key, int encoder) {
		if (!encoders.containsKey(key))
			encoders.put(key, new ArrayList<>());
		encoders.get(key).add(encoder);
	}
}