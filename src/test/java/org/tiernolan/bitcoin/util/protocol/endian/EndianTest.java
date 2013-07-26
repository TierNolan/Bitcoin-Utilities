package org.tiernolan.bitcoin.util.protocol.endian;

import static org.junit.Assert.assertEquals;

import java.util.Random;

import org.junit.Test;

public class EndianTest {
	
	@Test
	public void testShort() {
		
		short x = 0x0102;
		short y = 0x0201;
		
		assertEquals("Error in endian reversal", Endian.swap(x), y);
		
		x = (short) 0x8182;
		y = (short) 0x8281;
		
		assertEquals("Error in endian reversal", Endian.swap(x), y);
		
		Random r = new Random();
		
		for (int i = 0; i < 100; i++) {
			x = (short) r.nextInt();
			assertEquals("Error in endian reversal", Endian.swap(x), swap(x));
		}
	}

	private short swap(short x) {
		short y = 0;
		for (int i = 0; i < 2; i++) {
			y = (short) ((y << 8) | (x & 0xFF));
			x = (short) (x >> 8);
		}
		return y;
	}
	
	@Test
	public void testInt() {
		
		int x = 0x01020304;
		int y = 0x04030201;
		
		assertEquals("Error in endian reversal", Endian.swap(x), y);
		
		x = 0x81828384;
		y = 0x84838281;
		
		assertEquals("Error in endian reversal", Endian.swap(x), y);
		
		Random r = new Random();
		
		for (int i = 0; i < 100; i++) {
			x = r.nextInt();
			assertEquals("Error in endian reversal", Endian.swap(x), swap(x));
		}
	}

	private int swap(int x) {
		int y = 0;
		for (int i = 0; i < 4; i++) {
			y = (y << 8) | (x & 0xFF);
			x = x >> 8;
		}
		return y;
	}
	
	@Test
	public void testLong() {
		
		long x = 0x0102030405060708L;
		long y = 0x0807060504030201L;
		
		assertEquals("Error in endian reversal", Endian.swap(x), y);
		
		x = 0x8182838485868788L;
		y = 0x8887868584838281L;
		
		assertEquals("Error in endian reversal", Endian.swap(x), y);
		
		Random r = new Random();
		
		for (int i = 0; i < 100; i++) {
			x = r.nextInt();
			assertEquals("Error in endian reversal", Endian.swap(x), swap(x));
		}
	}

	private long swap(long x) {
		long y = 0;
		for (int i = 0; i < 8; i++) {
			y = (y << 8) | (x & 0xFF);
			x = x >> 8;
		}
		return y;
	}

}
