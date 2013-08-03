package org.tiernolan.bitcoin.util.protocol.message;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.types.Hash;
import org.tiernolan.bitcoin.util.protocol.types.VarInt;

public class GetHeaders extends Message {
	
	private final int version;
	private final VarInt locatorCount;
	private final Hash[] locators;
	private final Hash stop;
	
	public GetHeaders(int version) {
		this(version, Message.GENESIS_MAINNET.getBlockHash());
	}
	
	public GetHeaders(int version, Hash start) {
		this(version, new Hash[] {start}, null);
	}
	
	public GetHeaders(int version, Hash[] locators, Hash stop) {
		super("getheaders");
		this.version = version;
		if (locators == null) {
			this.locators = new Hash[0];
		} else {
			this.locators = new Hash[locators.length];
			for (int i = 0; i < locators.length; i++) {
				this.locators[i] = locators[i].copy();
			}
		}
		this.locatorCount = new VarInt(this.locators.length);
		if (stop == null) {
			this.stop = new Hash(new byte[32]);
		} else {
			this.stop = stop.copy();
		}
		
	}
	
	public GetHeaders(int version, byte[] data) throws IOException {
		this(version, new EndianDataInputStream(new ByteArrayInputStream(data)));
	}
	
	public GetHeaders(int version, EndianDataInputStream in) throws IOException {
		super("getheaders");
		this.version = in.readLEInt();
		locatorCount = new VarInt(version, in);
		if (locatorCount.get() > Message.MAX_HEADERS) {
			throw new IOException("Locator hash array exceeded maximum length");
		} else if (locatorCount.get() < 0) {
			throw new IOException("Locator hash array negative length");
		}
		int length = (int) locatorCount.get();
		locators = new Hash[length];
		for (int i = 0; i < length; i++) {
			locators[i] = new Hash(in, 32);
		}
		stop = new Hash(in, 32);
	}
	
	public int getVersion() {
		return version;
	}
	
	public Hash[] getLocators() {
		Hash[] hashes = new Hash[locators.length];
		for (int i = 0; i < locators.length; i++) {
			hashes[i] = locators[i].copy();
		}
		return hashes;
	}
	
	public Hash getStop() {
		return stop.copy();
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(version);
		if (locatorCount.get() != locators.length) {
			throw new IllegalStateException("Locator count and locator count array length mismatch");
		}
		locatorCount.write(version, out);
		for (int i = 0; i < locators.length; i++) {
			locators[i].write(version, out);
		}
		stop.write(version, out);
		
	}
	
	

}
