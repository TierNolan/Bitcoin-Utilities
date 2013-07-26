package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;
import java.io.InputStream;

public class MutableHash extends Hash {

	public MutableHash(InputStream in, int length) throws IOException {
		super(in, length);
	}
	
	public MutableHash(byte[] data) {
		super(data);
	}
	
	public MutableHash(int length) {
		super(length);
	}
	
	public void set(int i, byte d) {
		data[i] = d;
		computeHashCode();
	}
	
	public void setASCII(String s) {
		for (int i = 0; i < s.length() && i < data.length; i++) {
			data[i] = (byte) s.charAt(i);
		}
	}
	
	public void read(InputStream in) throws IOException {
		int read = 0;
		while (read < data.length) {
			read += in.read(data, read, data.length - read);
		}
		computeHashCode();
	}

}
