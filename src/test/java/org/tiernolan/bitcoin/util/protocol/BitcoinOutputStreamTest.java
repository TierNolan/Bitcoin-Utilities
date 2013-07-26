package org.tiernolan.bitcoin.util.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.bitcoin.util.crypt.Digest;
import org.tiernolan.bitcoin.util.encoding.ByteArray;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.message.Version;

public class BitcoinOutputStreamTest {

	@Test
	public void testWriteMessage() throws IOException {

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		BitcoinOutputStream cos = new BitcoinOutputStream(Message.MAGIC_MAINNET, bos);

		Random r = new Random();

		long time = System.currentTimeMillis() / 1000;

		Version ver = new Version(0, time, null, 0, null, 0, r.nextLong(), 0, false);

		cos.setVersion(Version.VERSION);
		cos.writeMessage(ver);
		cos.flush();
		
		byte[] message = bos.toByteArray();

		EndianDataInputStream eis = new EndianDataInputStream(new ByteArrayInputStream(message));
		
		assertEquals("Network magic value did not match", Message.MAGIC_MAINNET, eis.readLEInt());
		
		byte[] command = new byte[12];
		eis.read(command);
		assertTrue("Command value did not match", Arrays.equals("version\0\0\0\0\0".getBytes(StandardCharsets.UTF_8), command));
		
		assertEquals("Incorrect message length", message.length - 24, eis.readLEInt());

		int checksum = eis.readLEInt();
	
		byte[] payload = ByteArray.rightJustify(message, message.length - 24);
		
		byte[] digest = Digest.doubleSHA256(payload);
		
		int expectedChecksum = new EndianDataInputStream(new ByteArrayInputStream(digest)).readLEInt();
		
		assertEquals("Unexpected checksum", expectedChecksum, checksum);
		
		assertEquals("Unexpected version", Version.VERSION, eis.readLEInt());
		
		assertEquals("Unexpected services", 0, eis.readLELong());
		
		assertEquals("Unexpected timestamp", time, eis.readLELong());
		
	}

}
