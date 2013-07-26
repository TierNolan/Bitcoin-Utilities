package org.tiernolan.bitcoin.util.crypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

import org.bouncycastle.math.ec.ECPoint;
import org.junit.Test;
import org.tiernolan.bitcoin.util.encoding.Base58;
import org.tiernolan.bitcoin.util.encoding.ByteArray;

public class KeyPairTest {
	
	@Test
	public void testAttach() throws IOException {
		
		KeyPair p = new KeyPair(NetPrefix.MAIN_NET);
		
		Random r = new Random();
		
		byte[] x = new byte[80];
		
		r.nextBytes(x);
		
		p.attach("key", x);
		
		byte[] attached = p.getAttach("key");
		
		assertTrue("Attachment mismatch", Arrays.equals(x, attached));
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		KeyPair.stretchEnabled = false;
		
		p.write(bos, "This is my passphrase");
		
		ByteArrayInputStream in = new ByteArrayInputStream(bos.toByteArray());
		
		KeyPair readPair = KeyPair.read(in, "This is my passphrase");
		
		attached = readPair.getAttach("key");
		
		assertTrue("Attachment mismatch after reading", Arrays.equals(x, attached));
		
	}
	
	@Test
	public void testPrefix() {

		testPrefix(NetPrefix.MAIN_NET);
		testPrefix(NetPrefix.TEST_NET);
		
	}
	
	private static void testPrefix(NetPrefix prefix) {
		KeyPair p = new KeyPair(prefix);
		
		String address = p.getAddress(false);
		
		byte[] decoded = Base58.decode(address);
		
		assertEquals("Prefix mismatch for public address", prefix.getPublicPrefix(), decoded[0]);
		
		String privImport = p.getPrivateKeyImport();
		
		decoded = Base58.decode(privImport);
		
		assertEquals("Prefix mismatch for private import", prefix.getPrivatePrefix(), decoded[0]);
	}
	
	@Test
	public void testPrivateKey() {
		
		Random r = new Random();
		
		BigInteger pri = new BigInteger(256, r);

		pri = pri.mod(Secp256k1.getN());
		
		KeyPair p = new KeyPair(NetPrefix.MAIN_NET, pri);
		
		ECPoint point = Secp256k1.getG().multiply(pri);
		
		ECPoint pub = p.getPublicKey();
		
		assertEquals("Public key does not match expected", point, pub);
		
		assertEquals("Private key does not match expected", pri, p.getPrivateKey());

		p.wipePrivateKey();
		
		assertEquals("Public key was not wiped", null, p.getPrivateKey());
		
		assertEquals("Public key lost after wiping", point, pub);
		
	}
	
	@Test
	public void readWriteTest() throws IOException {
		
		KeyPair p = new KeyPair(NetPrefix.MAIN_NET);
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		
		KeyPair.stretchEnabled = false;
		
		p.write(bos, "This is my passphrase");
		
		ByteArrayInputStream in = new ByteArrayInputStream(bos.toByteArray());
		
		KeyPair readPair = KeyPair.read(in, "This is my passphrase");
		
		assertEquals("Private keys don't match after read", p.getPrivateKey(), readPair.getPrivateKey());
		assertEquals("Public keys don't match after read", p.getPublicKey(), readPair.getPublicKey());
		
		p.wipePrivateKey();
		
		bos.reset();
		
		p.write(bos, "This is my passphrase");
		
		in = new ByteArrayInputStream(bos.toByteArray());
		
		readPair = KeyPair.read(in, "This is my passphrase");
		
		assertTrue("Private key not null after read " + readPair.getPrivateKey(), readPair.getPrivateKey() == null);
		assertEquals("Public keys don't match after read", p.getPublicKey(), readPair.getPublicKey());

		
	}
	
	@Test
	public void rightJustifyTest() {
		
		byte[] ref = new byte[] {1, 2, 3, 4, 5};
		
		assertTrue("Buffer does not match expected", Arrays.equals(new byte[] {3, 4, 5}, ByteArray.rightJustify(ref, 3)));
		assertTrue("Buffer does not match expected", Arrays.equals(new byte[] {0, 0, 1, 2, 3, 4, 5}, ByteArray.rightJustify(ref, 7)));
		assertTrue("Buffer does not match expected", Arrays.equals(new byte[] {1, 2, 3, 4, 5}, ByteArray.rightJustify(ref, 5)));
		
	}

}
