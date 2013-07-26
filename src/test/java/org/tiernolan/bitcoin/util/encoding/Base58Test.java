package org.tiernolan.bitcoin.util.encoding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Test;

public class Base58Test {
	
	private static final String[] vectors = new String[] {
		"", "", 
		"61", "2g", 
		"626262", "a3gV", 
		"636363", "aPEr", 
		"73696d706c792061206c6f6e6720737472696e67", "2cFupjhnEsSn59qHXstmK2ffpLv2", 
		"00eb15231dfceb60925886b67d065299925915aeb172c06647", "1NS17iag9jJgTHD1VXjvLCEnZuQ3rJDE9L", 
		"516b6fcd0f", "ABnLTmg", 
		"bf4f89001e670274dd", "3SEo3LWLoPntC", 
		"572e4794", "3EFU7m", 
		"ecac89cad93923c02321", "EJDM8drfXA6uyA", 
		"10c8511e", "Rt5zm", 
		"00000000000000000000", "1111111111"
	};

	@Test
	public void encodeTest() {
		for (int i = 0; i < vectors.length; i += 2) {
			assertEquals("Encode did not match expected", vectors[i + 1], Base58.encode(vectors[i]));
		}
	}
	
	@Test
	public void encodeByteArrayTest() {
		for (int i = 0; i < vectors.length; i += 2) {
			assertEquals("Encode did not match expected", vectors[i + 1], Base58.encode(Hex.decode(vectors[i])));
		}
	}
	
	@Test
	public void decodeTest() {
		for (int i = 0; i < vectors.length; i += 2) {
			assertEquals("Encode did not match expected", vectors[i], Hex.toHexString(Base58.decode(vectors[i + 1])));
		}
	}
	
	@Test
	public void encodeDecodeTest() {
		
		Random r = new Random();
		
		for (int i = 0; i < 100; i++) {
			
			byte[] buf = new byte[r.nextInt(20)];
			
			r.nextBytes(buf);
			
			String encoded = Base58.encode(buf);
			
			byte[] decoded = Base58.decode(encoded);
			
			assertTrue("Decode of encoded array did not match " + Hex.toHexString(buf), Arrays.equals(buf, decoded));
			
		}
		
	}
	
}
