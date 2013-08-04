package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;
import java.io.InputStream;

import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.Hex;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class Hash implements MessageType {
	
	protected final byte[] data;
	protected int hash;
	
	public Hash(InputStream in, int length) throws IOException {
		this.data = new byte[length];
		in.read(this.data);
		computeHashCode();
	}
	
	public Hash(byte[] data) {
		this.data = new byte[data.length];
		System.arraycopy(data, 0, this.data, 0, data.length);
		computeHashCode();
	}
	
	protected Hash(int length) {
		this.data = new byte[length];
		computeHashCode();
	}
	
	public byte[] getData() {
		byte[] d = new byte[data.length];
		System.arraycopy(data, 0, d, 0, data.length);
		return d;
	}
	
	public byte[] getReverseData() {
		byte[] d = new byte[data.length];
		int j = data.length - 1;
		for (int i = 0; i < d.length; i++) {
			d[i] = data[j--];
		}
		return d;
	}
	
	public int getLength() {
		return data.length;
	}
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	protected void computeHashCode() {
		hash = Arrays.hashCode(data);
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		} else if (!(o instanceof Hash)) {
			return false;
		} else {
			final Hash other = (Hash) o;
			if (other.data.length != data.length) {
				return false;
			}
			if (hashCode() != other.hashCode()) {
				return false;
			}
			for (int i = 0; i < data.length; i++) {
				if (other.data[i] != data[i]) {
					return false;
				}
			}
			return true;
		}
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.write(data);
	}
	
	@Override
	public String toString() {
		return Hex.toHexString(data);
	}
	
	public String toASCIIString() {
		char[] chars = new char[data.length];
		int length = 0;
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) (data[i] & 0xFF);
			if (chars[i] != 0) {
				length = i + 1;
			}
		}
		return new String(chars, 0, length);
	}
	
	public Hash copy() {
		if (!getClass().equals(Hash.class)) {
			throw new IllegalStateException("The copy method has no override by the sub-class");
		}
		return this;
	}

}
