package org.tiernolan.bitcoin.util.protocol.endian;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EndianDataInputStream extends DataInputStream {

	public EndianDataInputStream(InputStream in) {
		super(in);
	}
	
	public byte readLEByte() throws IOException {
		return readByte();
	}

	public byte readBEByte() throws IOException {
		return readByte();
	}
	
	public short readLEShort() throws IOException {
		return Endian.swap(readShort());
	}

	public short readBEShort() throws IOException {
		return readShort();
	}
	
	public int readLEInt() throws IOException {
		return Endian.swap(readInt());
	}

	public int readBEInt() throws IOException {
		return readInt();
	}
	
	public long readLELong() throws IOException {
		return Endian.swap(readLong());
	}

	public long readBELong() throws IOException {
		return readLong();
	}
	
}
