package org.tiernolan.bitcoin.util.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.tiernolan.bitcoin.util.protocol.message.Verack;
import org.tiernolan.bitcoin.util.protocol.message.Version;
import org.tiernolan.bitcoin.util.protocol.types.BlockHeader;

public class BitcoinSocket extends Socket {
	
	private final int network;
	private final long services;
	private final int height;
	private final long nonce;
	private final boolean relay;
	private final InetAddress localAddress;
	private final int localPort;
	
	private BitcoinInputStream cis;
	private BitcoinOutputStream cos;
	
	public BitcoinSocket(String hostname, int port, int network, long services, int height, long nonce) throws IOException {
		this(hostname, port, network, services, height, nonce, false);
	}
	
	public BitcoinSocket(String hostname, int port, int network, long services, int height, long nonce, boolean relay) throws IOException {
		this(hostname, port, network, services, height, nonce, relay, System.currentTimeMillis() / 1000L);
	}
	
	public BitcoinSocket(String hostname, int port, int network, long services, int height, long nonce, boolean relay, long timestamp) throws IOException {
		this(hostname, port, network, services, height, nonce, relay, timestamp, null, 0);
	}
	
	public BitcoinSocket(String hostname, int port, int network, long services, int height, long nonce, boolean relay, long timestamp, InetAddress localAddress, int localPort) throws IOException {
		super(hostname, port);
		this.network = network;
		this.services = services;
		this.height = height;
		this.nonce = nonce;
		this.relay = relay;
		this.localAddress = localAddress;
		this.localPort = localPort;
		connectRemote();
	}
	
	protected BitcoinSocket(int network, long services, int height, long nonce, boolean relay, InetAddress localAddress, int localPort) {
		this.network = network;
		this.services = services;
		this.height = height;
		this.nonce = nonce;
		this.relay = relay;
		this.localAddress = localAddress;
		this.localPort = localPort;
	}
	
	protected void handshake() throws IOException {
		connectClient();
	}
	
	@Override
	public BitcoinOutputStream getOutputStream() {
		return cos;
	}
	
	@Override
	public BitcoinInputStream getInputStream() {
		return cis;
	}
	
	private void connectClient() throws IOException {
		this.cis = new BitcoinInputStream(network, super.getInputStream());
		this.cos = new BitcoinOutputStream(network, super.getOutputStream());
		
		InetSocketAddress remote = (InetSocketAddress) getLocalSocketAddress();
		
		long timestamp = System.currentTimeMillis() / 1000;
		
		int id = this.cis.getCommandId();
		
		if (id != Message.VERSION) {
			throw new IOException("Expecting version message response");
		}
	
		Version init = cis.readVersion();
		
		if (init.getNonce() == nonce) {
			this.shutdownOutput();
			this.close();
			throw new IOException("Connection to self");
		}
		
		int version = Math.min(init.getVersion(), Version.VERSION);
		
		Version reply = new Version(services, timestamp, remote.getAddress(), remote.getPort(), localAddress, localPort, nonce, height, relay);

		cos.writeMessage(reply);
		
		cis.setVersion(version);
		cos.setVersion(version);
		
		id = this.cis.getCommandId();
		
		if (id != Message.VERACK) {
			throw new IOException("Expecting verack message to complete handshake");
		}
		
		cis.readVerack();
		
		cos.writeMessage(new Verack());

	}
	
	private void connectRemote() throws IOException {
		this.cis = new BitcoinInputStream(network, super.getInputStream());
		this.cos = new BitcoinOutputStream(network, super.getOutputStream());
		
		InetSocketAddress remote = (InetSocketAddress) getRemoteSocketAddress();
		
		long timestamp = System.currentTimeMillis() / 1000;
		
		Version ver = new Version(services, timestamp, remote.getAddress(), remote.getPort(), localAddress, localPort, nonce, height, relay);
		
		this.cos.writeMessage(ver);
		
		int id = this.cis.getCommandId();
		
		if (id != Message.VERSION) {
			throw new IOException("Expecting version message response");
		}
		
		Version reply = cis.readVersion();
		
		if (reply.getNonce() == nonce) {
			this.shutdownOutput();
			this.close();
			throw new IOException("Connection to self");
		}

		int version = Math.min(reply.getVersion(), Version.VERSION);
		
		cis.setVersion(version);
		cos.setVersion(version);

		this.cos.writeMessage(new Verack());
		
		id = this.cis.getCommandId();
		
		if (id != Message.VERACK) {
			throw new IOException("Expecting verack message to complete handshake");
		}
		
		cis.readVerack();
		
	}

}
