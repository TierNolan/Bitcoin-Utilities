package org.tiernolan.bitcoin.util.protocol.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class Pong extends Message {
	
	private final long nonce;
	
	public Pong(long nonce) {
		super("pong");
		this.nonce = nonce;
	}
	
	public Pong(int version, byte[] data) throws IOException {
		this(version, new EndianDataInputStream(new ByteArrayInputStream(data)));
	}
	
	public Pong(int version, EndianDataInputStream in) throws IOException{
		super("pong");
		this.nonce = in.readLELong();
	}
	
	public long getNonce() {
		return nonce;
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLELong(nonce);
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Pong;
	}

}
