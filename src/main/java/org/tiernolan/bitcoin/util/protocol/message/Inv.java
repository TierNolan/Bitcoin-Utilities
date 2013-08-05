package org.tiernolan.bitcoin.util.protocol.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.types.InvVector;
import org.tiernolan.bitcoin.util.protocol.types.VarInt;

public class Inv extends Message {
	
	private final VarInt invCount;
	private final InvVector[] invs;

	public Inv(InvVector[] invs) {
		super("inv");
		int count = Math.min(invs.length, Message.MAX_INV_SZ);
		this.invCount = new VarInt(count);
		this.invs = new InvVector[count];
		System.arraycopy(invs, 0, this.invs, 0, count);
	}
	
	public Inv(int version, byte[] data) throws IOException {
		this(version, new EndianDataInputStream(new ByteArrayInputStream(data)));
	}
	
	public Inv(int version, EndianDataInputStream in) throws IOException{
		super("inv");
		this.invCount = new VarInt(version, in);
		if (invCount.get() < 0 || invCount.get() > Message.MAX_INV_SZ) {
			throw new IOException("Inv count out of range " + invCount.get());
		}
		this.invs = new InvVector[(int) invCount.get()];
		for (int i = 0; i < invs.length; i++) {
			this.invs[i] = new InvVector(version, in);
		}
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		if (invCount.get() != invs.length) {
			throw new IllegalStateException("Inv count and inv array length mismatch");
		}
		invCount.write(version, out);
		for (InvVector v : invs) {
			v.write(version, out);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Inv;
	}

}
