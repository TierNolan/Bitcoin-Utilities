package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class VarInt implements MessageType {

	private final long value;
	
	public VarInt(long value) {
		this.value = value;
	}
	
	public VarInt(int version, EndianDataInputStream in) throws IOException {
		int a = in.readUnsignedByte();
		if (a < 0xFD) {
			value = a;
		} else if (a == 0xFD) {
			value = in.readLEShort() & 0xFFFF;
		} else if (a == 0xFE) {
			value = in.readLEInt() & 0xFFFFFFFF;
		} else {
			value = in.readLELong();
		}
	}
	
	public long get() {
		return value;
	}
	
	@Override
	public String toString() {
		return Long.toString(get());
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		if ((value & 0xFFFFFFFF00000000L) != 0) {
			out.writeByte(0xFF);
			out.writeLELong(value);
		} else if ((value & 0xFFFFFFFFFFFF0000L) != 0) {
			out.writeByte(0xFE);
			out.writeLEInt((int) value);
		} else if (value >= 0xFD) {
			out.writeByte(0xFD);
			out.writeLEShort((short) value);
		} else {
			out.writeByte((byte) value);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof VarInt)) {
			return false;
		} else {
			VarInt other = (VarInt) o;
			
			return other.value == value;
		}
	}
	
}
