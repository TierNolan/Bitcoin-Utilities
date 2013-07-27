package org.tiernolan.bitcoin.util.protocol.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class Ping extends Message {
	
	private static final int BIP31_VERSION = 60000;
	
	private final long nonce;
	
	public Ping(long nonce) {
		super("ping");
		this.nonce = nonce;
	}
	
	public Ping(int version, byte[] data) throws IOException {
		this(version, new EndianDataInputStream(new ByteArrayInputStream(data)));
	}
	
	public Ping(int version, EndianDataInputStream in) throws IOException{
		super("ping");
		if (version > BIP31_VERSION) {
			this.nonce = in.readLELong();
		} else {
			this.nonce = 0L;
		}
	}
	
	public long getNonce() {
		return nonce;
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		if (version > BIP31_VERSION) {
			out.writeLELong(nonce);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Ping;
	}

}
