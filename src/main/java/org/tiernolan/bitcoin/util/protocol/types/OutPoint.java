package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;

import org.tiernolan.bitcoin.util.encoding.StringCreator;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class OutPoint implements MessageType {

	private final Hash prev;
	private final int index;
	
	public OutPoint(Hash prev, int index) {
		this.prev = prev.copy();
		this.index = index;
	}
	
	public OutPoint(int version, EndianDataInputStream in) throws IOException {
		this.prev = new Hash(in, 32);
		this.index = in.readLEInt();
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		this.prev.write(version, out);
		out.writeLEInt(index);
	}
	
	public Hash getPrevious() {
		return prev.copy();
	}
	
	public int getIndex() {
		return index;
	}
	
	@Override
	public int hashCode() {
		return prev.hashCode() + index;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof OutPoint)) {
			return false;
		} else {
			OutPoint other = (OutPoint) o;
			
			if (other.index != index) {
				return false;
			}
			
			return prev.equals(other.prev);
		}
	}
	
	@Override
	public String toString() {
		return new StringCreator()
			.add("prev", prev)
			.add("index", index)
			.toString();
	}
	
}