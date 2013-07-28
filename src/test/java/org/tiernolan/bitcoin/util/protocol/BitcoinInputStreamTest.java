package org.tiernolan.bitcoin.util.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.bitcoin.util.protocol.message.Version;

public class BitcoinInputStreamTest {
	
	@Test
	public void testReadMessage() throws IOException {
		
		Random r = new Random();
		
		long time = System.currentTimeMillis() / 1000;

		Version ver = new Version(0, time, null, 0, null, 0, r.nextLong(), 0, false);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BitcoinOutputStream cos = new BitcoinOutputStream(Message.MAGIC_MAINNET, bos);
		
		cos.setVersion(Version.VERSION);
		cos.writeMessage(ver);
		cos.flush();
		
		byte[] serialized = bos.toByteArray();
		
		ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
		BitcoinInputStream cis = new BitcoinInputStream(Message.MAGIC_MAINNET, bis);
		
		assertEquals("Unexcepted message type", Message.VERSION, cis.getCommandId());
		
		Version ver2 = cis.readVersion();
		
		assertEquals("Version message readback error", ver, ver2);
		
		for (int i = 20; i < 24; i++) {
			checkChecksum(serialized, i);
		}
		
	}
	
	private void checkChecksum(byte[] message, int p) throws IOException {
		byte[] copy = new byte[message.length];
		System.arraycopy(message, 0, copy, 0, message.length);
		
		copy[p]++;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(copy);
		BitcoinInputStream cis = new BitcoinInputStream(Message.MAGIC_MAINNET, bis);
		
		boolean thrown = false;
		try {
			cis.readVersion();
		} catch (IOException e) {
			thrown = true;
		}
		
		assertTrue("No exception throw for checksum error at position " + p, thrown);
	}

}
