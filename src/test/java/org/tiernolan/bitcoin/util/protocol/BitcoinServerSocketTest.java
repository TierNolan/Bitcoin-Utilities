package org.tiernolan.bitcoin.util.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import org.tiernolan.bitcoin.util.protocol.message.Ping;
import org.tiernolan.bitcoin.util.protocol.message.Pong;
import org.tiernolan.bitcoin.util.protocol.message.Version;

public class BitcoinServerSocketTest {

	private int port = 12345;

	@Test
	public void test() throws IOException, InterruptedException {
		
		final Random r = new Random();

		final long nonce = r.nextLong();

		BitcoinServerSocket server = new BitcoinServerSocket(port, Message.MAGIC_MAINNET, 0L, 0, nonce, true);
		server.setSoTimeout(500);

		final long pingNonce = r.nextLong();
		
		try {
			
			final AtomicReference<Throwable> exceptionThrown = new AtomicReference<Throwable>(null);

			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						BitcoinSocket s = new BitcoinSocket("localhost", port, Message.MAGIC_MAINNET, 0L, 0, nonce + 1);
						s.getOutputStream().writeMessage(new Ping(pingNonce));
						
						Pong pong = s.getInputStream().readPong();
						
						assertEquals("Pong nonce mismatch", pong.getNonce(), pingNonce);
						
					} catch (Throwable t) {
						exceptionThrown.set(t);
					}
				}
			});

			t.start();

			BitcoinSocket s = server.accept();
			s.setSoTimeout(500);

			BitcoinOutputStream cos = s.getOutputStream();
			BitcoinInputStream cis = s.getInputStream();

			assertEquals("Message id mismatch, version message expected", Message.PING, cis.getCommandId());
			
			Ping ping = cis.readPing();
			
			assertEquals("Ping nonce mismatch", pingNonce, ping.getNonce());
			
			cos.writeMessage(new Pong(ping.getNonce()));
			
			t.join(500);
			
			Throwable thrown = exceptionThrown.get();
			if (thrown != null) {
				throw new RuntimeException(thrown);
			}

		} finally {
			server.close();
		}

	}

	@Test
	public void selfConnect() throws IOException, InterruptedException {
		final Random r = new Random();

		final long nonce = r.nextLong();

		BitcoinServerSocket server = new BitcoinServerSocket(port, Message.MAGIC_MAINNET, 0L, 0, nonce, true);
		server.setSoTimeout(500);

		try {
			
			final AtomicReference<Throwable> exceptionThrown = new AtomicReference<Throwable>(null);

			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						BitcoinSocket s = new BitcoinSocket("localhost", port, Message.MAGIC_MAINNET, 0L, 0, nonce);
					} catch (Throwable t) {
						exceptionThrown.set(t);
					}
				}
			});

			t.start();

			boolean thrown = false;
			try {
				Socket s = server.accept();
			} catch (IOException e) {
				thrown = true;
			}
			
			assertTrue("Self connection was not detected by server", thrown);

			t.join(500);
			
			assertTrue("Self connection was not detected by client", exceptionThrown.get() != null);

		} finally {
			server.close();
		}


	}

}
