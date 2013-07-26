package org.tiernolan.bitcoin.util.protocol.endian;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class EndianDataOutputStream extends DataOutputStream {

	public EndianDataOutputStream(OutputStream out) {
		super(out);
	}
	
	public void writeLEByte(byte b) throws IOException {
		super.writeByte(b);
	}

	public void writeBEByte(byte b) throws IOException {
		super.writeByte(b);
	}
	
	public void writeLEShort(short s) throws IOException {
		super.writeShort(Endian.swap(s));
	}

	public void writeBEShort(short s) throws IOException {
		super.writeShort(s);
	}
	
	public void writeLEInt(int i) throws IOException {
		super.writeInt(Endian.swap(i));
	}

	public void writeBEInt(int i) throws IOException {
		super.writeInt(i);
	}
	
	public void writeLELong(long l) throws IOException {
		super.writeLong(Endian.swap(l));
	}

	public void writeBELong(long l) throws IOException {
		super.writeLong(l);
	}
	
}
