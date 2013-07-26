package org.tiernolan.bitcoin.util.crypt;

import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;
import org.tiernolan.bitcoin.util.crypt.Crypt;
import org.tiernolan.bitcoin.util.crypt.Digest;

public class DigestTest {
	
	String[][] SHA256Vectors = new String[][] {
		new String[] {"abc", " BA7816BF 8F01CFEA 414140DE 5DAE2223 B00361A3 96177A9C B410FF61 F20015AD"},
		new String[] {"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", " 248D6A61 D20638B8 E5C02693 0C3E6039 A33CE459 64FF2167 F6ECEDD4 19DB06C1"},
		new String[] {repeat("a", 1000000),  " CDC76E5C 9914FB92 81A1C7E2 84D73E67 F1809A48 A497200E 046D39CC C7112CD0"}
	};
	
	@Test
	public void SHA256Test() {
		
		for (int i = 0; i < SHA256Vectors.length; i++) {
			byte[] input = SHA256Vectors[i][0].getBytes(StandardCharsets.UTF_8);
			byte[] expected = Hex.decode(SHA256Vectors[i][1]);
			assertTrue("SHA256 Text vectors failed", Arrays.equals(expected, Digest.SHA256(input)));
		}
		
	}

	String[][] RIPEMDVectors = new String[][] {

			new String[] {"", "9c1185a5c5e9fc54612808977ee8f548b2258d31"}, 
			new String[] {"a", " 	0bdc9d2d256b3ee9daae347be6f4dc835a467ffe"},
			new String[] {"abc", " 	8eb208f7e05d987a9b044a8e98c6b087f15a0bfc"},
			new String[] {"message digest", "  	5d0689ef49d2fae572b881b123a85ffa21595f36"},
			new String[] {"abcdefghijklmnopqrstuvwxyz", " 	f71c27109c692c1b56bbdceb5b9d2865b3708dbc"},
			new String[] {"abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq", " 	12a053384a9c0c88e405a06c27dcf49ada62eb2b"},
			new String[] {"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789", " 	b0e20b6e3116640286ed3a87a5713079b21f5189"},
			new String[] {repeat("1234567890", 8), " 	9b752e45573d4b39f4dbd3323cab82bf63326bfb"},
			new String[] {repeat("a", 1000000), " 	52783243c1697bdbe16d37f97f68f08325dc1528"}

	};
	
	@Test
	public void RIPEMDTest() {
		for (int i = 0; i < RIPEMDVectors.length; i++) {
			byte[] input = RIPEMDVectors[i][0].getBytes(StandardCharsets.UTF_8);
			byte[] expected = Hex.decode(RIPEMDVectors[i][1]);
			assertTrue("RIPEMD Text vectors failed", Arrays.equals(expected, Digest.RIPEMD160(input)));
		}
		
	}
	
	@Test 
	public void doubleSHA256() {
		Crypt.init();
		
		Random r = new Random();
		
		for (int i = 0; i < 10; i++) {
			
			byte[] in = new byte[r.nextInt(1024)];
			
			byte[] doubleHash = Digest.doubleSHA256(in);
			
			byte[] reference = Digest.SHA256(Digest.SHA256(in));
			
			assertTrue("Double SHA256 did not match 2 SHA256 hashes", Arrays.equals(reference, doubleHash));
			
		}
		
	}

	private static String repeat(String s, int n) {
		StringBuilder sb = new StringBuilder(s.length() * n);
		for (int i = 0; i < n; i++) {
			sb.append(s);
		}
		return sb.toString();
	}

}
