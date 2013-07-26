package org.tiernolan.bitcoin.util.encoding;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.Arrays;
import org.tiernolan.bitcoin.util.crypt.Digest;
import org.tiernolan.bitcoin.util.crypt.NetPrefix;

public class Address {
	
	public static double getDifficulty(String prefix) {
		return getDifficulty((byte) 0, prefix, 25);
	}
	
	public static double getDifficulty(byte ver, String prefix) {
		return getDifficulty(ver, prefix, 25);
	}
	
	public static double getDifficulty(byte ver, String prefix, int byteLength) {
		return getDifficulty(ver, prefix, byteLength, byteLength - 1);
	}
	
	public static double getDifficulty(byte ver, String prefix, int byteLength, int randomBytes) {
		BigInteger[] ranges = getRange(ver, prefix, byteLength);
		
		BigInteger sum = BigInteger.ZERO;
		
		for (int i = 0; i < ranges.length; i += 2) {
			sum = sum.add(ranges[i]).subtract(ranges[i + 1]);
		}

		return BigInteger.valueOf(256).pow(randomBytes).doubleValue() / sum.doubleValue();
	}

	public static BigInteger[] getRange(byte ver, String prefix, int byteLength) {
		
		if (byteLength < 0) {
			throw new IllegalArgumentException("Negative byte length");
		}
		
		if (prefix == null) {
			throw new NullPointerException("Prefix must not be null");
		}
		
		int zeros = 0;
		
		for (int i = 0; i < prefix.length(); i++) {
			if (prefix.charAt(i) == '1') {
				zeros++;
			} else {
				break;
			}
		}
		

		
		if (zeros > byteLength) {
			System.out.println("To many leading zeros, address impossible");
			return null;
		}
		
		System.out.println("Zeros " + zeros);
		
		if (zeros == 0) {
			if (ver == 0) {
				System.out.println("Addresses that have a version of zero, must start with 1");
				return null;
			}
			return getRangeNonZero(prefix.substring(zeros), byteLength - zeros, ver, true);
		} else {
			if (ver != 0) {
				System.out.println("Addresses that start with 1 must have a version of 0");
				return null;
			}
			return getRangeNonZero(prefix.substring(zeros), byteLength - zeros, (byte) 0, false);
		}
	}
		
	private static BigInteger[] getRangeNonZero(String prefix, int byteLength, byte ver, boolean versionLocked) {
		
		if (byteLength == 0) {
			if (prefix.length() == 0) {
				return new BigInteger[] {BigInteger.ZERO, BigInteger.ZERO};
			} else {
				System.out.println("Prefix to long, address impossible");
				return null;
			}
		}
		
		if (prefix.length() > 10) {
			// TODO
			System.out.println("Unable to process prefixes longer than 10 characters");
			return null;
		}
		
		System.out.println("Starting " + prefix + " byteLength " + byteLength);
		
		BigInteger byteMin = BigInteger.valueOf(256).pow(byteLength - 1);
		BigInteger byteMax = byteMin.multiply(BigInteger.valueOf(256)).subtract(BigInteger.ONE);
		
		BigInteger verMin = byteMin.multiply(BigInteger.valueOf(ver & 0xFF));
		BigInteger verMax = byteMin.multiply(BigInteger.valueOf(1 + (ver & 0xFF))).subtract(BigInteger.ONE);
		
		long v = Base58.getPrefixValue(prefix);
		
		System.out.println("Value (v) " + v);
		
		if (v == -1) {
			System.out.println("Unable to get base58 value for " + prefix);
			return null;
		}
		
		BigInteger prefixValue = BigInteger.valueOf(v);
		
		int powEst = (int) (1.3656582373097610369574041812076 * byteLength);
		
		BigInteger refValue = prefixValue.multiply(Base58.fiftyEight.pow(powEst));
		
		while (refValue.compareTo(byteMax) <= 0) {
			refValue = refValue.multiply(Base58.fiftyEight);
		}

		while (refValue.compareTo(byteMax) > 0) {
			refValue = refValue.divide(Base58.fiftyEight);
		}
		
		List<BigInteger> ranges = new ArrayList<BigInteger>(6);
		
		BigInteger step = refValue.divide(prefixValue);
		
		BigInteger top;
		while ((top = refValue.add(step)).compareTo(byteMin) > 0) {
			BigInteger clampedTop = clamp(top.subtract(BigInteger.ONE), byteMin, byteMax);
			BigInteger clampedBottom = clamp(refValue, byteMin, byteMax);
			
			boolean failed = false;
			if (clampedTop.compareTo(clampedBottom) <= 0) {
				failed = true;
			} else if (versionLocked) {
				clampedTop = clamp(clampedTop, verMin, verMax);
				clampedBottom = clamp(clampedTop, verMin, verMax);
				if (clampedTop.compareTo(clampedBottom) <= 0) {
					System.out.println("Failed due to version locked");
					failed = true;
				}
			}
			if (!failed) {
				ranges.add(clampedTop);
				ranges.add(clampedBottom);
			}
			
			if (refValue.equals(BigInteger.ZERO)) {
				break;
			}
			refValue = refValue.divide(Base58.fiftyEight);
			step = step.divide(Base58.fiftyEight);
		}
		
		return ranges.toArray(new BigInteger[0]);
	}
	
	private static BigInteger clamp(BigInteger x, BigInteger min, BigInteger max) {
		if (x.compareTo(max) >= 0) {
			return max;
		} else if (x.compareTo(min) <= 0) {
			return min;
		} else {
			return x;
		}
	}

	public static String encodePublicKey(NetPrefix prefix, ECPoint pub) {
		return encodePublicKey(prefix, pub.getEncoded());
	}
	
	public static String encodePublicKey(NetPrefix prefix, byte[] pub) {
		return encodePublicKey(prefix.getPublicPrefix(), pub);
	}
	
	public static String encodePublicKey(byte ver, byte[] pub) {
		return encodeWithChecksum(ver, Digest.SHA256RIPEMD160(pub));
	}
	
	public static String encodeWithChecksum(byte ver, byte[] data) {
		
		byte[] concat = new byte[data.length + 5];
		
		concat[0] = ver;
		for (int i = 0; i < data.length; i++) {
			concat[i + 1] = data[i];
		}
		
		byte[] pass2 = Digest.doubleSHA256(Arrays.copyOfRange(concat, 0, concat.length - 4));
		
		for (int i = 0; i < 4; i++) {
			concat[concat.length - 4 + i] = pass2[i];
		}
		
		return Base58.encode(concat);
		
	}

}
