package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;
import java.util.Arrays;

import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class VarString implements MessageType {
	
	private final VarInt length;
	private final char[] string;
	
	public VarString(String string) {
		this.string = string.toCharArray();
		this.length = new VarInt(string.length());
	}
	
	public VarString(int version, EndianDataInputStream in) throws IOException {
		length = new VarInt(version, in);
		if (length.get() > 4096) {
			throw new IOException("String length to long " + length.get());
		} else if (length.get() < 0) {
			throw new IOException("Negative length string " + length.get());
		}
		string = new char[(int) length.get()];
		for (int i = 0; i < string.length; i++) {
			string[i] = (char) (in.readByte() & 0xFF);
		}
	}
	
	public String get() {
		return new String(string);
	}
	

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		length.write(version, out);
		if (string.length != length.get()) {
			throw new IllegalStateException("String length mismatch with varint");
		}
		for (int i = 0; i < string.length; i++) {
			out.writeByte((byte) string[i]);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof VarString)) {
			return false;
		} else {
			VarString other = (VarString) o;
			
			return Arrays.equals(other.string, string);
		}
	}
	
	@Override
	public int hashCode() {
		int hash = 1;
		for (int i = 0; i < string.length; i++) {
			hash = (hash << 5) - hash + (int) string[i];
		}
		return hash;
	}
	
	@Override
	public String toString() {
		return get();
	}

}
