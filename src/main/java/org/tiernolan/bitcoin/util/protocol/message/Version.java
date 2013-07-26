package org.tiernolan.bitcoin.util.protocol.message;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.endian.Endian;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.types.NetAddress;
import org.tiernolan.bitcoin.util.protocol.types.VarString;

public class Version extends Message {
	
	public static final int VERSION = 70001;
	public static final String AGENT = "/BitcoinJavaUtilities:0.1/";
	
	private final int version;
	private final long services;
	private final long timestamp;
	private final NetAddress peerAddress;
	private final NetAddress localAddress;
	private final long nonce;
	private final VarString agent;
	private final int height;
	private boolean relay;
	
	public Version(long services, long timestamp, InetAddress peerAddress, int peerPort, InetAddress localAddress, int localPort, long nonce, int height, boolean relay) {
		super("version");
		this.version = VERSION;
		this.services = services;
		this.timestamp = timestamp;
		this.peerAddress = new NetAddress(peerAddress, peerPort, 0);
		this.localAddress = new NetAddress(localAddress, localPort, services);
		this.nonce = nonce;
		this.agent = new VarString(AGENT);
		this.height = height;
		this.relay = relay;
	}
	
	public Version(int version, byte[] data) throws IOException {
		this(version, new EndianDataInputStream(new ByteArrayInputStream(data)));
	}
	
	public Version(int version, EndianDataInputStream in) throws IOException{
		super("version");
		if (version != 0) {
			throw new IOException("Only one version packet can be received");
		}
		this.version = Endian.swap(in.readInt());
		if (this.version < 60000) {
			throw new IOException("Unsupported version");
		}
		services = in.readLELong();
		timestamp = in.readLELong();
		peerAddress = new NetAddress(version, in, false);
		localAddress = new NetAddress(version, in, false);
		nonce = in.readLELong();
		agent = new VarString(version, in);
		this.height = in.readLEInt();
		try {
			relay = in.readBoolean();	
		} catch (EOFException e) {
			relay = true;
		}
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(this.version);
		out.writeLELong(services);
		out.writeLELong(timestamp);
		localAddress.write(version, out);
		peerAddress.write(version, out);
		out.writeLELong(nonce);
		agent.write(version, out);
		out.writeLEInt(height);
		if (version >= 70001) {
			out.writeBoolean(relay);
		} else if (!relay) {
			throw new IOException("Unable to encode false relay before version 70001");
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof Version)) {
			System.out.println("Failing as not version type");
			return false;
		} else {
			Version other = (Version) o;
			
			return 
					other.version == version &&
					other.services == services &&
					other.timestamp == timestamp &&
					other.nonce == nonce &&
					other.height == height &&
					other.relay == relay &&
					other.peerAddress.equals(peerAddress) &&
					other.localAddress.equals(localAddress) &&
					other.agent.equals(agent);
		}
	}

}
