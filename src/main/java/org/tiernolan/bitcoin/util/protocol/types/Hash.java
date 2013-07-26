package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;
import java.io.InputStream;

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
	
	@Override
	public int hashCode() {
		return hash;
	}
	
	protected void computeHashCode() {
		hash = 1;
		for (int i = 0; i < data.length; i++) {
			hash = (31 * hash) + (int) data[i];
		}
	}
	
	@Override
	public boolean equals(final Object o) {
		if (o == null) {
			return false;
		} else if (o == this) {
			return true;
		} else if (!(o instanceof Hash)) {
			return false;
		} else {
			final Hash other = (Hash) o;
			if (other.data.length != data.length) {
				System.out.println("Length diff");
				return false;
			}
			if (hashCode() != other.hashCode()) {
				System.out.println("hashcode diff " + Hex.toHexString(data) + " " + Hex.toHexString(other.data));
				return false;
			}
			for (int i = 0; i < data.length; i++) {
				if (other.data[i] != data[i]) {
					System.out.println("Data diff " + i);
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

}
