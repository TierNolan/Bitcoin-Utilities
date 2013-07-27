package org.tiernolan.bitcoin.util.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class BitcoinServerSocket extends ServerSocket {
	
	private final int network;
	private final long services;
	private volatile int height;
	private final long nonce;
	private final boolean relay;
	private volatile InetSocketAddress localAddress;

	public BitcoinServerSocket(int port, int network, long services, int height, long nonce, boolean relay) throws IOException {
		super(port);
		this.network = network;
		this.services = services;
		this.height = height;
		this.nonce = nonce;
		this.relay = relay;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public void setLocalAddress(InetAddress addr, int port) {
		this.localAddress = new InetSocketAddress(addr, port);
	}
	
	@Override
	public BitcoinSocket accept() throws IOException {
		InetSocketAddress local = localAddress;
		InetAddress localAddress = local == null ? null : local.getAddress();
		int localPort = local == null ? 0 : local.getPort();
		BitcoinSocket s = new BitcoinSocket(network, services, height, nonce, relay, localAddress, localPort);
		s.setSoTimeout(getSoTimeout());
		implAccept(s);
		s.handshake();
		return s;
	}

}
