package org.tiernolan.bitcoin.util.protocol;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

import org.bouncycastle.util.encoders.Hex;
import org.tiernolan.bitcoin.util.crypt.Digest;
import org.tiernolan.bitcoin.util.protocol.endian.Endian;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.message.GetHeaders;
import org.tiernolan.bitcoin.util.protocol.message.Headers;
import org.tiernolan.bitcoin.util.protocol.message.Ping;
import org.tiernolan.bitcoin.util.protocol.message.Pong;
import org.tiernolan.bitcoin.util.protocol.message.Verack;
import org.tiernolan.bitcoin.util.protocol.message.Version;
import org.tiernolan.bitcoin.util.protocol.types.MutableHash;

public class BitcoinInputStream extends EndianDataInputStream {

	private static int MAX_LENGTH = 32 * 1024 * 1024;
	
	private final MessageDigest d;
	
	protected boolean headerRead = false;
	protected boolean dataRead = false;
	
	protected final int network;
	protected final MutableHash command;
	protected final int safety;
	protected int version;
	protected int messageId;
	protected int length;
	protected byte[] checksum = new byte[4];
	protected byte[] data;
	
	private int totalRead = 0;
	
	public BitcoinInputStream(int network, InputStream in) throws IOException {
		this(network, in, 1);
	}
	
	public BitcoinInputStream(int network, InputStream in, int safety) throws IOException {
		super(in);
		this.d = Digest.getDigest(Digest.SHA256);
		this.network = network;
		this.safety = safety;
		this.command = new MutableHash(12);
		if (this.d == null) {
			throw new IOException("Unable to create SHA-256 digest");
		}
	}
	
	public int getCommandId() throws IOException {
		if (!headerRead) {
			int m;
			if ((m = Endian.swap(readInt())) != network) {
				do {
					int b = read();
					if (b == -1) {
						throw new EOFException("End of stream reached while seeking for magic number");
					}
					m = (m >> 8) | (b << 24);
				} while (m != network);

			}
			command.read(this);
			messageId = Message.getId(command);
			length = Endian.swap(readInt());
			read(checksum);
			headerRead = true;
		}
		return messageId;
	}
	
	public String getCommand() throws IOException {
		getCommandId();
		return command.toASCIIString();
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public void skipMessage() throws IOException {
		readData();
		dataRead = false;
		headerRead = false;
	}
	
	public Version readVersion() throws IOException {
		readData();
		try {
			return new Version(version, data);
		} finally {
			dataRead = false;
			headerRead = false;
		}
	}
	
	public Verack readVerack() throws IOException {
		readData();
		try {
			return new Verack(version, data);
		} finally {
			dataRead = false;
			headerRead = false;
		}
	}
	
	public Ping readPing() throws IOException {
		readData();
		try {
			return new Ping(version, data);
		} finally {
			dataRead = false;
			headerRead = false;
		}
	}
	
	public Pong readPong() throws IOException {
		readData();
		try {
			return new Pong(version, data);
		} finally {
			dataRead = false;
			headerRead = false;
		}
	}

	public GetHeaders readGetHeaders() throws IOException {
		readData();
		try {
			return new GetHeaders(version, data);
		} finally {
			dataRead = false;
			headerRead = false;
		}
	}
	
	public Headers readHeaders() throws IOException {
		readData();
		try {
			return new Headers(version, data);
		} finally {
			dataRead = false;
			headerRead = false;
		}
	}
	
	protected void readData() throws IOException {
		if (!headerRead) {
			getCommandId();
		}
		if (dataRead) {
			return;
		}
		if (length < 0) {
			throw new IOException("Negative data lengths are not allowed");
		} else if (length > MAX_LENGTH) {
			throw new IOException("Mesage sizes above 32MB are not allowed");
		}

		int size = Math.max(128, totalRead / safety);
		size = Math.min(length, size);
		
		byte[] data = new byte[size];
		
		int read = 0;
		
		while (read < length) {
			read += read(data, read, data.length - read);
			if (read == data.length) {
				data = expand(data);
			}
		}
		
		this.data = new byte[length];
		System.arraycopy(data, 0, this.data, 0, length);
		this.totalRead += length;
		byte[] digest = Digest.doubleSHA256(this.data);
		for (int i = 0; i < 4; i++) {
			if (digest[i] != checksum[i]) {
				throw new IOException("Checksum error at position " + i + " " + Hex.toHexString(digest, 0, 4) + " " + Hex.toHexString(checksum));
			}
		}
		dataRead = true;
	}
	
	private static byte[] expand(byte[] arr) {
		byte[] newArr = new byte[(arr.length * 3) / 2];
		System.arraycopy(arr, 0, newArr, 0, arr.length);
		return newArr;
	}

}
