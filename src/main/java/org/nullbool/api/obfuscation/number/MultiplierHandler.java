package org.nullbool.api.obfuscation.number;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplierHandler {

	private final Map<String, List<Long>> decoders;
	private final Map<String, List<Long>> encoders;

	public MultiplierHandler() {
		decoders = encoders = new HashMap<>();
	}
	
	public void test() {
//		for(Entry<String, List<Long>> e : decoders.entrySet()) {
//			if(e.getKey().startsWith("client.h")) {
//				System.out.println("m for " + e.getKey());
//			}
//		}
//		
//		for(Entry<String, List<Long>> e : encoders.entrySet()) {
//			if(e.getKey().startsWith("client.h")) {
//				System.out.println("m for " + e.getKey());
//			}
//		}
	}

	public long getDecoder(final String key) {
		long mostUsed = 0;
		int highestFreq = 0;
		final List<Long> multipliers = decoders.get(key);
		if (multipliers == null)
			return 0;
		for (final long modulus : multipliers) {
			final int count = (int) multipliers.stream().filter(i -> i.equals(modulus)).count();
			if (count > highestFreq) {
				highestFreq = count;
				mostUsed = modulus;
			}
		}
		return mostUsed;
	}

	public long getEncoder(final String key) {
		long mostUsed = 0;
		int highestFreq = 0;
		final List<Long> multipliers = encoders.get(key);
		if (multipliers == null)
			return 0;
		for (final long modulus : multipliers) {
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
	
	public long inverseDecoderLong(final String key) {
		try {
			final BigInteger num = BigInteger.valueOf(getDecoder(key));
			return num.modInverse(new BigInteger(String.valueOf(1L << 64))).longValue();
		} catch (final Exception e) {
			return 0;
		}
	}

	public void addDecoder(String key, long decoder) {
		if (!decoders.containsKey(key))
			decoders.put(key, new ArrayList<>());
		decoders.get(key).add(decoder);
	}

	public void addEncoder(String key, long encoder) {
		if (!encoders.containsKey(key))
			encoders.put(key, new ArrayList<>());
		encoders.get(key).add(encoder);
	}

	public Map<String, List<Long>> getDecoders() {
		return decoders;
	}

	public Map<String, List<Long>> getEncoders() {
		return encoders;
	}
}