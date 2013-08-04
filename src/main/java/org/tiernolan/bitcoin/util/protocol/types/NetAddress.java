package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.tiernolan.bitcoin.util.encoding.ByteArray;
import org.tiernolan.bitcoin.util.encoding.StringCreator;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class NetAddress implements MessageType {
	
	private static final byte[] nullAddress = new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, (byte) 0xFF, (byte) 0xFF, 0, 0, 0, 0};
	
	private final boolean hasTimestamp;
	private final int timestamp;
	private final long services;
	private final Hash ip;
	private final InetAddress addr;
	private final int port;
	
	public NetAddress(InetAddress addr, int port, long services, int timestamp) {
		this(addr, port, services, timestamp, true);
	}
	
	public NetAddress(InetAddress addr, int port, long services) {
		this(addr, port, services, 0, false);
	}
	
	private NetAddress(InetAddress addr, int port, long services, int timestamp, boolean hasTimestamp) {
		if (addr == null) {
			this.ip = new Hash(nullAddress);
		} else {
			byte[] bytes = addr.getAddress();
			if (bytes.length == 4) {
				bytes = ByteArray.rightJustify(bytes, 16);
				bytes[10] = -1;
				bytes[11] = -1;
			} else if (bytes.length != 16) {
				throw new IllegalArgumentException("Network address must be 4 or 16 bytes long");
			}
			this.ip = new Hash(bytes);
		}
		try {
			this.addr = getInetAddress(ip);
		} catch (IOException e) {
			throw new IllegalStateException("InetAddress.getByAddress should not throw an exception", e);
		}
		this.hasTimestamp = hasTimestamp;
		this.timestamp = timestamp;
		this.port = port;
		this.services = services;
	}
	
	public NetAddress(int version, EndianDataInputStream in, boolean hasTimestamp) throws IOException {
		this.hasTimestamp = hasTimestamp;
		this.timestamp = hasTimestamp ? in.readLEInt() : 0;
		this.services = in.readLELong();
		this.ip = new Hash(in, 16);
		this.addr = getInetAddress(ip);
		this.port = in.readBEShort() & 0xFFFF;
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		if (hasTimestamp) {
			out.writeLEInt(timestamp);
		}
		out.writeLELong(services);
		ip.write(version, out);
		out.writeBEShort((short) port);
	}
	
	public Integer getTimestamp() {
		if (!hasTimestamp) {
			return null;
		}
		return timestamp;
	}
	
	public long getServices() {
		return services;
	}
	
	public InetAddress getAddress() {
		return addr;
	}
	
	public int getPort() {
		return port;
	}
	
	private static InetAddress getInetAddress(Hash ip) throws IOException {
		byte[] addr = ip.getData();
		try {
			return InetAddress.getByAddress(addr);
		} catch (UnknownHostException e) {
			throw new IOException(e);
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof NetAddress)) {
			return false;
		} else {
			NetAddress other = (NetAddress) o;
			
			if (other.port != port) {
				return false;
			}
			
			if (hasTimestamp != other.hasTimestamp) {
				return false;
			}
			
			if (hasTimestamp && (timestamp != other.timestamp)) {
				return false;
			}

			if (other.services != services) {
				return false;
			}
			
			return ip.equals(other.ip);
		}
	}
	
	@Override
	public int hashCode() {
		return (int) (port + (hasTimestamp ? timestamp : 0) + services + (services >> 32) + ip.hashCode());
	}
	
	@Override
	public String toString() {
		StringCreator sc = new StringCreator();
		if (hasTimestamp) {
			sc = sc.add("timestamp", timestamp);
		}
		return sc
			.add("services", services)
			.add("ip", ip)
			.add("port", port)
			.toString();
	}

}
