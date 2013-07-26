package org.tiernolan.bitcoin.util.protocol.endian;

public class Endian {
	
	public static byte swap(byte b) {
		return b;
	}
	
	public static short swap(short s) {
		return (short) (((s & 0xFFFF) >> 8) | (s << 8));
	}
	
	public static int swap(int x) {
		return (x >>> 24) | ((x & 0x00FF0000) >>> 8) | ((x & 0x0000FF00) << 8) | (x << 24);
	}
	
	public static long swap(long x) {
		return (((long) swap((int) x)) << 32) | (swap((int) (x >>> 32)) & 0xFFFFFFFFL);
	}

}
