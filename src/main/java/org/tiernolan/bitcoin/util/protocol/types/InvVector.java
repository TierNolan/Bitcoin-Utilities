package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;

import org.tiernolan.bitcoin.util.encoding.StringCreator;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class InvVector implements MessageType {
	
	public final static int ERROR = 0;
	public final static int MSG_TX = 1;
	public final static int MSG_BLOCK = 2;

	private final int type;
	private final Hash hash;
	
	public InvVector(int type, Hash hash) {
		this.type = type;
		this.hash = hash.copy();
	}
	
	public InvVector(int version, EndianDataInputStream in) throws IOException {
		this.type = in.readLEInt();
		this.hash = new Hash(in, 32);
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(type);
		hash.write(version, out);
	}
	
	public int getType() {
		return type;
	}
	
	public Hash getTxHash() {
		return hash;
	}
	
	@Override
	public int hashCode() {
		return type + hash.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof InvVector)) {
			return false;
		} else {
			InvVector other = (InvVector) o;
			
			if (other.type != type) {
				return false;
			}
			
			return hash.equals(other.hash);
			
		}
	}

	@Override
	public String toString() {
		return new StringCreator()
			.add("type", type)
			.add("txHash", hash)
			.toString();
	}
	
}