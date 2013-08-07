package org.tiernolan.bitcoin.util.crypt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.math.ec.ECPoint;
import org.tiernolan.bitcoin.util.encoding.Address;
import org.tiernolan.bitcoin.util.encoding.ByteArray;

public class KeyPair {
	
	private static final SecureRandom random;
	
	protected static boolean stretchEnabled = true;
	
	static {
		Crypt.init();
		random = getSecureRandom();
		random.nextInt();
	}
	
	private final ECPoint pub;
	private final byte[] pri;
	private boolean hasPri;
	private final NetPrefix prefix;
	private final HashMap<String, byte[]> attach = new HashMap<String, byte[]>(1);
	
	public KeyPair() {
		this((NetPrefix) null);
	}
	
	public KeyPair(byte pubPrefix, byte priPrefix) {
		this(new NetPrefix(pubPrefix, priPrefix));
	}
	
	public KeyPair(NetPrefix prefix) {
		this(prefix, getRandomKey());
	}

	public KeyPair(BigInteger pri) {
		this(null, pri);
	}
	
	public KeyPair(byte pubPrefix, byte PriPrefix, BigInteger pri) {
		this(new NetPrefix(pubPrefix, PriPrefix), pri);
	}
	
	public KeyPair(NetPrefix prefix, BigInteger pri) {
		this(prefix, pri, null, true);
	}
	
	public KeyPair(byte[] bytes, boolean isPub) {
		this(null, bytes, isPub);
	}
	
	public KeyPair(byte pubPrefix, byte priPrefix, byte[] bytes, boolean isPub) {
		this(new NetPrefix(pubPrefix, priPrefix), bytes, isPub);
	}

	public KeyPair(NetPrefix network, byte[] bytes, boolean isPub) {
		this(network, isPub ? null : new BigInteger(padBytes(bytes)), isPub ? decode(bytes) : null, false);
	}
	
	public KeyPair(ECPoint pub) {
		this(null, pub);
	}
	
	public KeyPair(byte pubPrefix, byte priPrefix, ECPoint pub) {
		this(new NetPrefix(pubPrefix, priPrefix), pub);
	}
	
	public KeyPair(NetPrefix prefix, ECPoint pub) {
		this(prefix, null, pub, false);
	}
	
	private KeyPair(NetPrefix prefix, BigInteger pri, ECPoint pub, boolean hasPri) {
		if (hasPri) {
			if (pri == null) {
				throw new IllegalArgumentException("Private key not provided");
			}
			if (pub != null) {
				throw new IllegalStateException("Public and private key provide together");
			}
			if (pri.compareTo(BigInteger.ZERO) < 0) {
				throw new IllegalArgumentException("Private key cannot be negative");
			}
			if (pri.compareTo(Secp256k1.getN()) >= 0) {
				throw new IllegalArgumentException("Private key cannot be greater or equal to N");
			}
		} else {
			if (pub == null) {
				throw new IllegalArgumentException("Public key not provided");
			}
			if (pri != null) {
				throw new IllegalStateException("Public and private key provide together");
			}
		}
		
		this.hasPri = hasPri;
		this.pri = hasPri ? pri.toByteArray() : null;
		this.pub = hasPri ? Secp256k1.getG().multiply(new BigInteger(this.pri)) : pub;

		this.prefix = prefix;
	}
	
	public KeyPair attach(String key, byte[] data) {
		if (key == null || key.length() == 0) {
			throw new IllegalArgumentException("Invalid key");
		}
		synchronized (attach) {
			if (data == null) {
				attach.remove(key);
			} else {
				byte[] newData = new byte[data.length];
				System.arraycopy(data, 0, newData, 0, data.length);
				attach.put(key, newData);
			}
		}
		return this;
	}
	
	public KeyPair attach(Map<String, byte[]> map) {
		synchronized (attach) {
			for (Map.Entry<String, byte[]> e : map.entrySet()) {
				attach(e.getKey(), e.getValue());
			}
		}
		return this;
	}
	
	public byte[] getAttach(String key) {
		synchronized (attach) {
			return attach.get(key);
		}
	}
	
	public NetPrefix getPrefix() {
		return prefix;
	}
	
	public int getPublicPrefix() {
		if (prefix == null) {
			return -1;
		}
		return prefix.getPublicPrefix() & 0xFF;
	}
	
	public int getPrivatePrefix() {
		if (prefix == null) {
			return -1;
		}
		return prefix.getPrivatePrefix() & 0xFF;
	}
	
	public String getAddress(boolean compressed) {
		if (prefix == null) {
			return null;
		}
		return getAddress(prefix.getPublicPrefix(), compressed);
	}
	
	public String getAddress(byte pubPrefix, boolean compressed) {
		return Address.encodePublicKey(pubPrefix, pub.getEncoded(compressed));
	}
	
	public BigInteger getPrivateKey() {
		if (pri == null || !hasPri) {
			return null;
		}
		return new BigInteger(pri);
	}
	
	public String getPrivateKeyImport() {
		if (pri == null || !hasPri) {
			return null;
		}

		int standardSize = Secp256k1.getFieldSize() / 8;

		final byte[] standard = ByteArray.rightJustify(pri, standardSize);
		
		return Address.encodeWithChecksum(prefix.getPrivatePrefix(), standard);
		
	}
	
	public ECPoint getPublicKey() {
		return pub;
	}
	
	/**
	 * Gets a copy of this KeyPair with only the public key included
	 * 
	 * @return
	 */
	public KeyPair getPublicKeyPair() {
		return new KeyPair(prefix, null, pub, false).attach(attach);
	}
	
	/**
	 * Gets a copy of this KeyPair using the given ECPoint as public key
	 * 
	 * @param pub
	 * @return
	 */
	public KeyPair getNewKeyPair(ECPoint pub) {
		return new KeyPair(prefix, null, pub, false).attach(attach);
	}
	
	/**
	 * Gets a copy of this KeyPair using the given BigInteger as public key
	 * 
	 * @param pri
	 * @return
	 */
	public KeyPair getNewKeyPair(BigInteger pri) {
		return new KeyPair(prefix, pri, null, true).attach(attach);
	}
	
	/**
	 * Overwrites the private key with zeros.  This changes a public/private key
	 * pair to one that only contains a public key.
	 */
	public void wipePrivateKey() {
		this.hasPri = false;
		if (pri != null) {
			for (int i = 0; i < pri.length; i++) {
				pri[i] = 0;
			}
		}
	}
	
	public void write(File f, String passphrase) throws IOException {
		write(f, passphrase, false);
	}
	
	public void write(File f, String passphrase, boolean publicOnly) throws IOException {
		if (f.exists()) {
			throw new IOException("File exists");
		}
		FileOutputStream out = new FileOutputStream(f);
		try {
			write(out, passphrase, publicOnly);
		} finally {
			out.flush();
			out.getFD().sync();
			out.close();
		}
		
	}
	
	public void write(OutputStream out, String passphrase) throws IOException {
		write(out, passphrase, false);
	}
	
	public void write(OutputStream out, String passphrase, boolean publicOnly) throws IOException {
		
		byte[] stretched = stretch(passphrase);
		if (stretched == null) {
			throw new IOException("Illegal passphrase");
		}
		DataOutputStream dos = new DataOutputStream(Crypt.encrypt(stretched, out));

		if (this.pri == null || publicOnly || !hasPri) {
			dos.write(0);
			writePrefix(dos);
			byte[] encoded = pub.getEncoded();
			dos.writeInt(encoded.length);
			dos.write(encoded);
		} else {
			dos.write(1);
			writePrefix(dos);
			byte[] encoded = pri;
			dos.writeInt(encoded.length);
			dos.write(encoded);
		}
		synchronized (attach) {
			for (Map.Entry<String, byte[]> e : attach.entrySet()) {
				dos.writeBoolean(true);
				dos.writeUTF(e.getKey());
				dos.writeInt(e.getValue().length);
				dos.write(e.getValue());
			}
			dos.writeBoolean(false);
		}
		dos.flush();
	}
	
	public static KeyPair read(File f, String passphrase) throws IOException {
		
		InputStream in = new FileInputStream(f);
		try {
			return read(in, passphrase);
		} finally {
			in.close();
		}
	}
	
	public static KeyPair read(InputStream in, String passphrase) throws IOException {

		byte[] stretched = stretch(passphrase);
		if (stretched == null) {
			throw new IOException("Illegal passphrase");
		}
		
		DataInputStream dis = new DataInputStream(Crypt.decrypt(stretched, in));
		
		int hasPri = dis.read();

		NetPrefix prefix = readPrefix(dis);
		
		int length = dis.readInt();
		if (length > 2048) {
			throw new IOException("Key length greater than 2048 bytes");
		}
		byte[] bytes = new byte[length];
		
		dis.readFully(bytes);
		
		KeyPair p;
		if (hasPri == 0) {
			p = new KeyPair(prefix, bytes, true);
		} else if (hasPri == 1) {
			BigInteger pri = new BigInteger(bytes);
			p = new KeyPair(prefix, pri);
		} else {
			throw new IOException("Unable to decode has private key flag");
		}
		
		boolean done = false;
		
		while (!done) {
			done = !dis.readBoolean();
			if (!done) {
				String key = dis.readUTF();
				length = dis.readInt();
				if (length < 0 || length > 2048) {
					throw new IOException("Attachment length is restricted to 2048 bytes");
				}
				byte[] data = new byte[length];
				dis.read(data);
				p.attach(key, data);
			}
		}
		
		return p;
		
	}

	private void writePrefix(OutputStream out) throws IOException {
		if (prefix == null) {
			out.write(0);
		} else {
			out.write(1);
			out.write(prefix.getPrivatePrefix());
			out.write(prefix.getPublicPrefix());
		}
	}
	
	private static NetPrefix readPrefix(InputStream in) throws IOException {
		int nullCheck = in.read();
		if (nullCheck == -1) {
			throw new EOFException("Unable to read file");
		} else if (nullCheck == 0) {
			return null;
		} else if (nullCheck == 1) {
			int priPrefix = in.read();
			int pubPrefix = in.read();
			if (priPrefix == -1 || pubPrefix == -1) {
				throw new IOException("Unable to decode network prefix");
			}
			return new NetPrefix((byte) pubPrefix, (byte) priPrefix);
		} else {
			throw new IOException("Unable to decode network prefix");
		}
	}
	
	public static byte[] stretch(String passphrase) {
		if (passphrase == null) {
			return null;
		}

		byte[] bytes = typedTextToBytes(passphrase);
		
		byte[] digest = new byte[32];
		
		digest = Digest.SHA256(bytes);
		
		if (!stretchEnabled || "".equals(passphrase)) {
			return digest;
		}
		
		byte[][] buf = new byte[32768][32];
		
		for (int i = 0; i < 256; i++) {
			for (int j = 0; j < buf.length; j++) {
				byte[] current = buf[j];
				xor(current, digest);
				
				digest = Digest.SHA256(digest);

				int index = ((current[15] & 0x7F) << 8) | (current[16] & 0xFF);
				byte[] source = buf[index];
				
				xor(digest, source);
				
				digest = Digest.SHA256(digest);
			}
		}
		
		return digest;
	}
	
	private static byte[] typedTextToBytes(String text) {
		char[] chars = text.toCharArray();
		byte[] bytes = new byte[chars.length];
		for (int i = 0; i < chars.length; i++) {
			int x = chars[i] & 0xFFFF;
			if (x < 32 || x > 127) {
				return null;
			}
			bytes[i] = (byte) chars[i];
		}
		return bytes;
	}
	
	private static void xor(byte[] target, byte[] source) {
		for (int i = 0; i < target.length; i++) {
			target[i] ^= source[i];
		}
	}
	
	private static byte[] padBytes(byte[] bytes) {
		if ((bytes[0] & 0x80) != 0) {
			byte[] temp = new byte[bytes.length + 1];
			System.arraycopy(bytes, 0, temp, 1, bytes.length);
			return temp;
		}
		return bytes;
	}
	
	private static ECPoint decode(byte[] encoded) {
		if (encoded == null) {
			throw new NullPointerException("Encoded public key is null");
		}
		if (!checkStandard(encoded)) {
			throw new IllegalArgumentException("Non-standard public key");
		}
		return Secp256k1.getCurve().decodePoint(encoded);
	}
	
	private static boolean checkStandard(byte[] encoded) {
		
		if (encoded == null) {
			return false;
		}
		
		if (encoded[0] == 0x04 && encoded.length == 65) {
			return true;
		}
		
		if ((encoded[0] == 0x02 || encoded[0] == 0x03) && encoded.length == 33) {
			return true;
		}
		
		return false;
		
	}
	
	public static SecureRandom getSeededSecureRandom() {
		synchronized (random) {
			SecureRandom r = getSecureRandom();
			
			byte[] seed = new byte[20];
			random.nextBytes(seed);
			
			r.setSeed(seed);
			
			return r;
		}
	}
	
	private static SecureRandom getSecureRandom() {
			try {
				return SecureRandom.getInstance("SHA1PRNG", "SUN");
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				e.printStackTrace();
				try {
					return SecureRandom.getInstance("SHA1PRNG");
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
					return new SecureRandom();
				}
			}
	}
	
	private static BigInteger getRandomKey() {
		synchronized (random) {
			BigInteger b = new BigInteger(Secp256k1.getFieldSize(), random);
			if (b.compareTo(Secp256k1.getN()) >= 0) {
				return getRandomKey();
			} else {
				return b;
			}
		}
	}

}
