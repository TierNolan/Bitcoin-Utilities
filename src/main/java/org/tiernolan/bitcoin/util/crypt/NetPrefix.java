package org.tiernolan.bitcoin.util.crypt;

public class NetPrefix {

	public static final NetPrefix MAIN_NET = new NetPrefix((byte) 0x00, (byte) 0x80);
	public static final NetPrefix TEST_NET = new NetPrefix((byte) 0x6F, (byte) 0xEF);
	
	private final byte pub;
	private final byte pri;
	
	public NetPrefix(byte pub, byte pri) {
		this.pub = pub;
		this.pri = pri;
	}
	
	public byte getPublicPrefix() {
		return pub;
	}
	
	public byte getPrivatePrefix() {
		return pri;
	}
	
}