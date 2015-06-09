package org.nullbool.api.obfuscation.number;

import java.math.BigInteger;

public final class EuclideanNumberPair {

		/**
		 * The greatest common divisor, product, and quotient. Product is used
		 * to encode values, where quotient is used to decode them. GCD usually
		 * = product * quotient. True value is the decoded value {@see <init>}
		 */
		private BigInteger product, quotient, gcd, trueValue;

		/**
		 * If the unsafe flag is flagged {@code true}, the gcd was not <1>. This
		 * is a very bad thing.
		 */
		private boolean unsafe;

		/**
		 * The amount of bits in this pair of numbers. 32 = int, 64 = long
		 */
		private int bits;

		public EuclideanNumberPair(BigInteger product, BigInteger quotient,
				BigInteger gcd, int bits, boolean unsafe) {
			this.product = product;
			this.quotient = quotient;
			this.gcd = gcd;
			this.bits = bits;

			BigInteger k = gcd.multiply(product);
			this.trueValue = quotient.multiply(k);
		}

		public BigInteger product() {
			return product;
		}

		public BigInteger quotient() {
			return quotient;
		}

		public BigInteger gcd() {
			return gcd;
		}

		public BigInteger trueValue() {
			return trueValue;
		}

		public int bits() {
			return bits;
		}

		public boolean isUnsafe() {
			return unsafe;
		}
	}