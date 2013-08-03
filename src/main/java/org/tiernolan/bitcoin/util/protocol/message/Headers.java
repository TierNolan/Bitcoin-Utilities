package org.tiernolan.bitcoin.util.protocol.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.types.BlockHeader;
import org.tiernolan.bitcoin.util.protocol.types.Hash;
import org.tiernolan.bitcoin.util.protocol.types.VarInt;

public class Headers extends Message {
	
	private final VarInt headerCount;
	private final BlockHeader[] headers;
	
	public Headers(int version) {
		this(version, Message.GENESIS_MAINNET.getBlockHash());
	}
	
	public Headers(int version, Hash start) {
		this(version, new Hash[] {start}, null);
	}
	
	public Headers(int version, Hash[] locators, Hash stop) {
		super("headers");
		throw new UnsupportedOperationException(""); // TODO	
	}
	
	public Headers(int version, byte[] data) throws IOException {
		this(version, new EndianDataInputStream(new ByteArrayInputStream(data)));
	}
	
	public Headers(int version, EndianDataInputStream in) throws IOException {
		super("headers");
		headerCount = new VarInt(version, in);
		if (headerCount.get() < 0) {
			throw new IOException("Negative header count");
		} else if (headerCount.get() > Message.MAX_HEADERS) {
			throw new IOException("Number of headers exceeds maximum allowed");
		}
		int count = (int) headerCount.get();
		this.headers = new BlockHeader[count];
		for (int i = 0; i < count; i++) {
			this.headers[i] = new BlockHeader(version, in);
		}
	}
	
	public BlockHeader[] getBlockHeaders() {
		BlockHeader[] h = new BlockHeader[headers.length];
		for (int i = 0; i < headers.length; i++) {
			h[i] = headers[i];
		}
		return h;
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(version);
		if (headerCount.get() != headers.length) {
			throw new IllegalStateException("Locator count and locator count array length mismatch");
		}
		headerCount.write(version, out);
		for (int i = 0; i < headers.length; i++) {
			headers[i].write(version, out);
		}
	}
	
	

}
