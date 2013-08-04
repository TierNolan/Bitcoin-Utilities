package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;

import org.bouncycastle.util.Arrays;
import org.tiernolan.bitcoin.util.encoding.StringCreator;
import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class TxIn implements MessageType {

	private final long value;
	private final VarInt pkLength;
	private final byte[] pk;
	
	public TxIn(long value, byte[] pk) {
		this.pkLength = new VarInt(pk.length);
		this.pk = new byte[pk.length];
		System.arraycopy(pk, 0, this.pk, 0, pk.length);
		this.value = value;
	}
	
	public TxIn(int version, EndianDataInputStream in) throws IOException {
		this.value = in.readLELong();
		this.pkLength = new VarInt(version, in);
		if (pkLength.get() < 0 || pkLength.get() > Message.MAX_SCRIPT_LENGTH) {
			throw new IOException("Sig script to long");
		}
		this.pk = new byte[(int) pkLength.get()];
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLELong(value);
		this.pkLength.write(version, out);
		if (pkLength.get() != pk.length) {
			throw new IllegalStateException("Sig length field and array length mismatch");
		}
		out.write(this.pk);
	}
	
	public byte[] getPubKeyScript() {
		byte[] s = new byte[pk.length];
		System.arraycopy(pk, 0, s, 0, pk.length);
		return s;
	}
	
	public long getValue() {
		return value;
	}
	
	@Override
	public int hashCode() {
		return ((int) (value + (value >> 32))) + Arrays.hashCode(pk);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof TxIn)) {
			return false;
		} else {
			TxIn other = (TxIn) o;
			
			if (other.value != value) {
				return false;
			}
			
			return Arrays.areEqual(other.pk, pk);
			
		}
	}

	@Override
	public String toString() {
		return new StringCreator()
			.add("scriptPubKey", pk)
			.add("value", value)
			.toString();
	}
	
}