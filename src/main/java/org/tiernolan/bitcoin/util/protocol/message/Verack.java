package org.tiernolan.bitcoin.util.protocol.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class Verack extends Message {

	public Verack() {
		super("verack");
	}
	
	public Verack(int version, byte[] data) throws IOException {
		this(version, new EndianDataInputStream(new ByteArrayInputStream(data)));
	}
	
	public Verack(int version, EndianDataInputStream in) throws IOException{
		super("verack");
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
	}
	
	@Override
	public boolean equals(Object o) {
		return o instanceof Verack;
	}

}
