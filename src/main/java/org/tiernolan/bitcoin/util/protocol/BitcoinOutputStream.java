package org.tiernolan.bitcoin.util.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

import org.tiernolan.bitcoin.util.crypt.Digest;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.message.Version;
import org.tiernolan.bitcoin.util.protocol.types.MutableHash;

public class BitcoinOutputStream extends EndianDataOutputStream {

	private final MessageDigest d;
	
	protected boolean headerRead = false;
	
	protected final int network;
	protected final MutableHash command;
	protected int version = 0;
	protected byte[] checksum = new byte[4];
	protected byte[] data;
	
	public BitcoinOutputStream(int network, OutputStream out) throws IOException {
		super(out);
		this.d = Digest.getDigest(Digest.SHA256);
		this.network = network;
		this.command = new MutableHash(12);
		if (this.d == null) {
			throw new IOException("Unable to create SHA-256 digest");
		}
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public void writeMessage(Message message) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		EndianDataOutputStream eos = new EndianDataOutputStream(bos);
		message.write(Version.VERSION, eos);
		eos.flush();
		eos.close();
		byte[] data = bos.toByteArray();
		byte[] check = Digest.doubleSHA256(data);
		writeLEInt(network);
		command.setASCII(message.getCommand());
		command.write(version, this);
		writeLEInt(data.length);
		write(check, 0, 4);
		write(data);
		flush();
	}

}
