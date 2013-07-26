package org.tiernolan.bitcoin.util.protocol.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.message.Version;

public class NetAddressTest {
	
	@Test
	public void IP4Test() throws IOException {
		byte[] ip = new byte[4];
		
		Random r = new Random();
		
		for (int i = 0; i < 20; i++) {
			r.nextBytes(ip);
			int port = r.nextInt() & 0xFFFF;
			long services = r.nextLong();
			
			NetAddress a = new NetAddress(InetAddress.getByAddress(ip), port, services);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			
			a.write(Version.VERSION, eos);
			eos.flush();
			
			byte[] encoded = bos.toByteArray();
			
			EndianDataInputStream eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			assertEquals("Services mismatch", services, eis.readLELong());
			assertEquals("Address mismatch", 0L, eis.readLELong());
			assertEquals("Address mismatch", 0, eis.readLEShort());
			assertEquals("Address mismatch", (short) -1, eis.readLEShort());

			byte[] serIp = new byte[4];
			eis.read(serIp);
			
			assertTrue("IP mismatch", Arrays.equals(ip, serIp));
			
			assertEquals("Port mismatch", eis.readUnsignedShort(), port);

			eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			NetAddress decoded = new NetAddress(Version.VERSION, eis, false);
			
			assertEquals("Decoded error", a, decoded);
		}

	}
	
	@Test
	public void IP6Test() throws IOException {
		byte[] ip = new byte[16];
		
		Random r = new Random();
		
		for (int i = 0; i < 20; i++) {
			r.nextBytes(ip);
			int port = r.nextInt() & 0xFFFF;
			long services = r.nextLong();
			
			NetAddress a = new NetAddress(InetAddress.getByAddress(ip), port, services);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			
			a.write(Version.VERSION, eos);
			eos.flush();
			
			byte[] encoded = bos.toByteArray();
			
			EndianDataInputStream eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			assertEquals("Services mismatch", services, eis.readLELong());

			byte[] serIp = new byte[16];
			eis.read(serIp);
			
			assertTrue("IP mismatch", Arrays.equals(ip, serIp));
			
			assertEquals("Port mismatch", eis.readUnsignedShort(), port);
			
			eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			NetAddress decoded = new NetAddress(Version.VERSION, eis, false);
			
			assertEquals("Decoded error", a, decoded);
		}
	}
	
	@Test
	public void TimestampTest() throws IOException {
		byte[] ip = new byte[4];
		
		Random r = new Random();
		
		for (int i = 0; i < 20; i++) {
			r.nextBytes(ip);
			int port = r.nextInt() & 0xFFFF;
			long services = r.nextLong();
			int timestamp = r.nextInt() & 0x7FFFFFFF;
			
			NetAddress a = new NetAddress(InetAddress.getByAddress(ip), port, services, timestamp);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			
			a.write(Version.VERSION, eos);
			eos.flush();
			
			byte[] encoded = bos.toByteArray();
			
			EndianDataInputStream eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			assertEquals("Timestamp mismatch", timestamp, eis.readLEInt());
			assertEquals("Services mismatch", services, eis.readLELong());
			assertEquals("Address mismatch", 0L, eis.readLELong());
			assertEquals("Address mismatch", 0, eis.readLEShort());
			assertEquals("Address mismatch", (short) -1, eis.readLEShort());

			byte[] serIp = new byte[4];
			eis.read(serIp);
			
			assertTrue("IP mismatch", Arrays.equals(ip, serIp));
			
			assertEquals("Port mismatch", eis.readUnsignedShort(), port);
			
			eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			NetAddress decoded = new NetAddress(Version.VERSION, eis, true);
			
			assertEquals("Decoded error", a, decoded);
		}

	}

}
