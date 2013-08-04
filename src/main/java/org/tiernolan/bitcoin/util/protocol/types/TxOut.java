package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;

import org.bouncycastle.util.Arrays;
import org.tiernolan.bitcoin.util.encoding.StringCreator;
import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class TxOut implements MessageType {

	private final OutPoint outPoint;
	private final VarInt sigLength;
	private final byte[] sig;
	private final int sequence;
	
	public TxOut(OutPoint outPoint, byte[] sig, int sequence) {
		this.outPoint = outPoint;
		this.sigLength = new VarInt(sig.length);
		this.sig = new byte[sig.length];
		System.arraycopy(sig, 0, this.sig, 0, sig.length);
		this.sequence = sequence;
	}
	
	public TxOut(int version, EndianDataInputStream in) throws IOException {
		this.outPoint = new OutPoint(version, in);
		this.sigLength = new VarInt(version, in);
		if (sigLength.get() < 0 || sigLength.get() > Message.MAX_SCRIPT_LENGTH) {
			throw new IOException("Sig script to long");
		}
		this.sig = new byte[(int) sigLength.get()];
		this.sequence = in.readLEInt();
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		this.outPoint.write(version, out);
		this.sigLength.write(version, out);
		if (sigLength.get() != sig.length) {
			throw new IllegalStateException("Sig length field and array length mismatch");
		}
		out.write(this.sig);
		out.writeLEInt(sequence);
	}
	
	public OutPoint getOutPoint() {
		return outPoint;
	}
	
	public byte[] getSigScript() {
		byte[] s = new byte[sig.length];
		System.arraycopy(sig, 0, s, 0, sig.length);
		return s;
	}
	
	public int getSequence() {
		return sequence;
	}
	
	@Override
	public int hashCode() {
		return sequence + outPoint.hashCode() + Arrays.hashCode(sig);
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof TxOut)) {
			return false;
		} else {
			TxOut other = (TxOut) o;
			
			if (other.sequence != sequence) {
				return false;
			}
			
			if (!Arrays.areEqual(other.sig, sig)) {
				return false;
			}
			
			return outPoint.equals(other.outPoint);
		}
	}
	
	@Override
	public String toString() {
		return new StringCreator()
			.add("outPoint", outPoint)
			.add("scriptSig", sig)
			.add("sequence", sequence)
			.toString();
	}

}