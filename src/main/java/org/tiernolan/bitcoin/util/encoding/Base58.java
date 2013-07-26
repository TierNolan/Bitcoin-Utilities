package org.tiernolan.bitcoin.util.encoding;

import java.math.BigInteger;

import org.bouncycastle.util.encoders.Hex;

public class Base58 {
	
	private static final char[] encodeTable = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();
	private static final int[] reverseTable = computeReverseTable();
	
	public static final BigInteger fiftyEight = BigInteger.valueOf(58);

	
	public static String encode(String hex) {
		return encode(Hex.decode(hex));
	}
	
	public static int getSymbolValue(char c) {
		return reverseTable[c & 0xFF];
	}
	
	public static char getValueSymbol(int v) {
		return encodeTable[v];
	}

	public static String encode(byte[] bytes) {
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == 0) {
				sb.append(1);
			} else {
				break;
			}
		}
		
		byte[] temp = new byte[bytes.length + 1];
		
		for (int i = 1; i < temp.length; i++) {
			temp[i] = bytes[i - 1];
		}
		
		BigInteger n = new BigInteger(temp);
		
		BigInteger d = fiftyEight;
		
		BigInteger[] result;
		
		StringBuilder sb2 = new StringBuilder();
		
		while (n.compareTo(BigInteger.ZERO) != 0) {
			
			result = n.divideAndRemainder(d);
			
			BigInteger div = result[0];
			
			BigInteger rem = result[1];
			
			n = div;
			
			sb2.append(getValueSymbol(rem.intValue()));
			
		}
		
		return sb.toString() + sb2.reverse().toString();
		
	}
	
	public static byte[] decode(String hex) {
		
		int zeros = 0;
		for (int i = 0; i < hex.length(); i++) {
			if (hex.charAt(i) == '1') {
				zeros++;
			} else {
				break;
			}
		}
		
		BigInteger n = BigInteger.ZERO;
		
		BigInteger d = fiftyEight;
		
		String encoded = hex.substring(zeros);
		
		for (int i = 0; i < encoded.length(); i++) {
			n = n.multiply(d);
			
			n = n.add(BigInteger.valueOf(getSymbolValue(encoded.charAt(i))));
		}
		
		byte[] temp = n.toByteArray();
		
		byte[] temp2;
		if (temp[0] == 0) {
			temp2 = new byte[temp.length - 1 + zeros];
		} else {
			temp2 = new byte[temp.length + zeros];
		}
		
		for (int i = Math.max(0, temp.length - temp2.length); i < temp.length; i++) {
			temp2[i - temp.length + temp2.length] = temp[i];
		}
		
		return temp2;
		
	}
	
	public static long getPrefixValue(String prefix) {
		if (prefix.length() > 10) {
			return -1;
		}
		long total = 0;
		long value = 1;
		
		for (int i = prefix.length() - 1; i >= 0; i--) {
			int v = getSymbolValue(prefix.charAt(i));
			if (v == -1) {
				return -1;
			}
			total += value * v;
			value *= 58;
		}
		
		return total;
	}
		
	public static final int[] computeReverseTable() {
		
		int[] table = new int[256];
		
		for (int i = 0; i < 256; i++) {
			table[i] = -1;
		}
		
		for (int i = 0; i < encodeTable.length; i++) {
			table[encodeTable[i] & 0xFF] = i;
		}
		
		return table;
	}
	
}
