package org.tiernolan.bitcoin.util.armory;

import java.math.BigInteger;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.tiernolan.bitcoin.util.crypt.Digest;
import org.tiernolan.bitcoin.util.crypt.KeyPair;
import org.tiernolan.bitcoin.util.crypt.NetPrefix;
import org.tiernolan.bitcoin.util.crypt.Secp256k1;
import org.tiernolan.bitcoin.util.encoding.Base58;

public class Armory {
	
	private final static char[] encodeTable = "asdfghjkwertuion".toCharArray();
	private final static int[] reverseTable = computeReverseTable();
	
	public static KeyPair addRandomChaincode(KeyPair pair) {
		byte[] chaincode = new byte[32];
		KeyPair.getSeededSecureRandom().nextBytes(chaincode);
		return pair.attach("chaincode", chaincode);
	}
	
	public static KeyPair getKeyPairFromPaper(NetPrefix prefix, String root1, String root2, String chain1, String chain2) {
		byte[] decodedRoot1 = Armory.decodeLine(root1.replace(" ", ""), 0);
		byte[] decodedRoot2 = Armory.decodeLine(root2.replace(" ", ""), 0);			
		byte[] decodedChain1 = Armory.decodeLine(chain1.replace(" ", ""), 0);
		byte[] decodedChain2 = Armory.decodeLine(chain2.replace(" ", ""), 0);
		
		if (decodedRoot1 == null || decodedRoot2 == null || decodedChain1 == null || decodedChain2 == null) {
			System.out.println(decodedRoot1 + " " + decodedRoot2 + " " + decodedChain1 + " " + decodedChain2);
			return null;
		}
		
		byte[] key = ByteUtils.concatenate(new byte[] {0}, ByteUtils.concatenate(decodedRoot1, decodedRoot2));
		if (key.length != 33) {
			return null;
		}
		
		byte[] chaincode = ByteUtils.concatenate(decodedChain1, decodedChain2);
		
		BigInteger privateKey = new BigInteger(key);
		
		KeyPair keyPair = new KeyPair(prefix, privateKey);
		keyPair.attach("chaincode", chaincode);
		
		return keyPair;
	}
	
	public static KeyPair getNext(KeyPair pair) {
		if (pair == null) {
			System.out.println("Pair is null");
			return null;
		}
		if (pair.getPrivateKey() != null) {
			return getNextPrivateKey(pair);
		} else {
			return getNextPublicKey(pair);
		}
	}
	
	private static KeyPair getNextPublicKey(KeyPair pair) {
		byte[] chaincode = pair.getAttach("chaincode");
		if (chaincode == null) {
			return null;
		}
		BigInteger step = getStep(pair.getPublicKey(), chaincode);
		KeyPair newPair = pair.getNewKeyPair(pair.getPublicKey().multiply(step));
		return newPair.attach("chaincode", chaincode);
	}
	
	private static KeyPair getNextPrivateKey(KeyPair pair) {
		byte[] chaincode = pair.getAttach("chaincode");
		if (chaincode == null) {
			return null;
		}
		ECPoint pubKey = pair.getPublicKey();
		BigInteger step = getStep(pubKey, chaincode);
		KeyPair newPair = pair.getNewKeyPair(pair.getPrivateKey().multiply(step).mod(Secp256k1.getN()));
		return newPair.attach("chaincode", chaincode);
	}
	
	private static BigInteger getStep(ECPoint pub, byte[] chaincode) {
		byte[] extend = Digest.doubleSHA256(pub.getEncoded(false));
		for (int i = 0; i < extend.length; i++) {
			extend[i] = (byte) (extend[i] ^ chaincode[i]);
		}
		extend = ByteUtils.concatenate(new byte[] {0}, extend);
		return new BigInteger(extend);
	}
	
	public static String getWalletId(KeyPair root) {
		KeyPair first = Armory.getNextPublicKey(root);
		if (first == null) {
			return null;
		}
		
		byte[] hash = Digest.SHA256RIPEMD160(first.getPublicKey().getEncoded(false));
		
		byte[] clipped = ByteUtils.concatenate(new byte[] {(byte) first.getPublicPrefix()}, ByteUtils.subArray(hash, 0, 5));
		
		for (int i = 0; i < clipped.length / 2; i++) {
			byte t = clipped[i];
			clipped[i] = clipped[clipped.length - 1 - i];
			clipped[clipped.length - 1 - i] = t;
		}
		
		return Base58.encode(clipped);
	}
	
	public static int checkLine(String line) {
		for (int i = 0; i <= 2; i++) {
			if (decodeLine(line, i) != null) {
				return i;
			}
		}
		return -1;
	}
	
	public static String correctLine(String line) {
		byte[] decoded = decodeLine(line, 2);
		if (decoded == null) {
			return null;
		}
		return encodeLine(decoded);
	}
	
	public static byte[] decodeLine(String line) {
		return decodeLine(line.toCharArray());
	}

	public static byte[] decodeLine(String line, boolean attemptFix) {
		int runs = attemptFix ? 2 : 0;
		for (int i = 0; i <= runs; i++) {
			byte[] result = decodeLine(line, i);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
	public static byte[] decodeLine(String line, int errors) {
		line = line.replace(" ", "");
		char[] chars = line.toCharArray();
		byte[] result = decodeLine(chars);
		if (result != null) {
			return result;
		}
		char[] temp = line.toCharArray();

		int[] i = new int[errors];
		do {
			int[] j = new int[errors];
			do {
				for (int k = 0; k < errors; k++) {
					temp[i[k]] = encodeTable[j[k]];
				}
				result = decodeLine(temp);
				if (result != null) {
					return result;
				}
			} while (incLoop(j, encodeTable.length));
			for (int k = 0; k < errors; k++) {
				temp[i[k]] = chars[i[k]];
			}
		} while (incLoop(i, chars.length));
		return result;
	}
	
	private static boolean incLoop(int[] index, int max) {
		for (int i = 0; i < index.length; i++) {
			index[i] ++;
			if (index[i] < max) {
				return true;
			}
			index[i] = 0;
		}
		return false;
	}
		
	public static byte[] decodeLine(char[] line) {

		
		byte[] decoded = fromEasyHex(line);
		
		if (decoded == null || decoded.length != 18) {
			return null;
		}
		
		byte[] result = new byte[16];
		
		System.arraycopy(decoded, 0, result, 0, 16);
		
		byte[] crc = new byte[2];
		
		System.arraycopy(decoded, 16, crc, 0, 2);
		
		byte[] digest = Digest.doubleSHA256(result);
		
		if (checkEqual(crc, digest, 2)) {
			return result;
		}
		
		return null;
		
	}
	
	public static String encodeLine(byte[] decoded) {
		byte[] digest = Digest.doubleSHA256(decoded);
		byte[] crc = new byte[2];
		
		System.arraycopy(digest, 0, crc, 0, 2);
		
		return toEasyHex(ByteUtils.concatenate(decoded, crc));
	}
	
	public static boolean checkEqual(byte[] a, byte[] b, int length) {
		if (a == null || b == null || a.length < length || b.length < length) {
			return false;
		}
		for (int i = 0; i < length; i++) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}
	
	public static String toEasyHex(byte[] decoded) {
		return toEasyHex(decoded, 2);
	}
	
	public static String toEasyHex(byte[] decoded, int step) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < decoded.length; i++) {
			sb.append(getValueSymbol((decoded[i] >> 4) & 0xF));
			sb.append(getValueSymbol(decoded[i] & 0xF));
			if (i % step == step - 1) {
				sb.append(" ");
			}
		}
		return sb.toString();
	}
	
	public static byte[] fromEasyHex(char[] encoded) {
		if (encoded == null) {
			return null;
		}
		if ((encoded.length & 0x1) != 0) {
			return null;
		}
		
		byte[] decoded = new byte[encoded.length / 2];
		
		for (int i = 0; i < encoded.length; i += 2) {
			int msb = getSymbolValue(encoded[i]);
			int lsb = getSymbolValue(encoded[i + 1]);
			if (msb == -1 || lsb == -1) {
				return null;
			}
			decoded[i >> 1] = (byte) ((msb << 4) | (lsb & 0xF));
		}
		return decoded;
	}
	
	
	public static int getSymbolValue(char c) {
		return reverseTable[c & 0xFF];
	}
	
	public static char getValueSymbol(int v) {
		return encodeTable[v];
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
