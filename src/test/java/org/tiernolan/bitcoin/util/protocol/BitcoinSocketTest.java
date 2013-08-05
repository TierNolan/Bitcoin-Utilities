package org.tiernolan.bitcoin.util.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;
import org.tiernolan.bitcoin.util.protocol.message.Verack;
import org.tiernolan.bitcoin.util.protocol.message.Version;

public class BitcoinSocketTest {

	private int port = 12345;

	@Test
	public void test() throws IOException, InterruptedException {

		ServerSocket server = new ServerSocket(port);
		server.setSoTimeout(500);

		try {
			final Random r = new Random();

			final long nonce = r.nextLong();

			final AtomicBoolean exceptionThrown = new AtomicBoolean(false);

			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						new BitcoinSocket("localhost", port, Message.MAGIC_MAINNET, 0L, 0, nonce);
					} catch (Throwable t) {
						exceptionThrown.set(true);
					}
				}
			});

			t.start();

			Socket s = server.accept();
			s.setSoTimeout(500);

			BitcoinOutputStream cos = new BitcoinOutputStream(Message.MAGIC_MAINNET, s.getOutputStream());
			BitcoinInputStream cis = new BitcoinInputStream(Message.MAGIC_MAINNET, s.getInputStream());

			assertEquals("Message id mismatch, version message expected", Message.VERSION, cis.getCommandId());

			Version ver = cis.readVersion();

			assertEquals("Unexcepted version", Version.VERSION, ver.getVersion());

			Version reply = new Version(0L, System.currentTimeMillis() / 1000, null, 0, null, 0, nonce + 1, 0, false);

			cos.writeMessage(reply);

			assertEquals("Message id mismatch, verack message expected", Message.VERACK, cis.getCommandId());

			cos.writeMessage(new Verack());

			t.join(500);

			assertTrue("Exception was thrown by socket thread", !exceptionThrown.get());

		} finally {
			server.close();
		}

	}

	@Test
	public void selfConnect() throws IOException {
		ServerSocket server = new ServerSocket(port);
		server.setSoTimeout(500);

		try {
			final Random r = new Random();

			final long nonce = r.nextLong();

			final AtomicBoolean exceptionThrown = new AtomicBoolean(false);

			Thread t = new Thread(new Runnable() {
				public void run() {
					try {
						new BitcoinSocket("localhost", port, Message.MAGIC_MAINNET, 0L, 0, nonce);
					} catch (Throwable t) {
						exceptionThrown.set(true);
					}
				}
			});		

			t.start();
			
			Socket s = server.accept();
			s.setSoTimeout(500);

			BitcoinOutputStream cos = new BitcoinOutputStream(Message.MAGIC_MAINNET, s.getOutputStream());
			BitcoinInputStream cis = new BitcoinInputStream(Message.MAGIC_MAINNET, s.getInputStream());

			assertEquals("Message id mismatch, version message expected", Message.VERSION, cis.getCommandId());

			cis.readVersion();

			Version reply = new Version(0L, System.currentTimeMillis() / 1000, null, 0, null, 0, nonce, 0, false);

			cos.writeMessage(reply);

			boolean eof = false;
			try {
				cis.getCommandId();
			} catch (EOFException e) {
				eof = true;
			}
			assertTrue("Self connect did not cause connection termination", eof);
		} finally {
			server.close();
		}

	}

}
