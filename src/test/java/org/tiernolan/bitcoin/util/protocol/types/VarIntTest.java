package org.tiernolan.bitcoin.util.protocol.types;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.message.Version;

public class VarIntTest {
	
	@Test
	public void encodeTest() throws IOException {
		
		Random r = new Random();
		
		for (int i = 0; i < 64; i++) {
			
			long value;
			
			if (r.nextBoolean()) {
				value = r.nextInt() & 0xFFL;
			} else if (r.nextBoolean()) {
				value = r.nextInt() & 0xFFFFL;
			} else if (r.nextBoolean()) {
				value = r.nextInt() & 0xFFFFFFFFL;
			} else {
				value = r.nextLong();
			}
			
			VarInt vi = new VarInt(value);
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			
			vi.write(0, eos);
			eos.flush();
			
			byte[] encoded = bos.toByteArray();
			
			EndianDataInputStream eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			if (value < 0xFD && value >= 0) {
				assertEquals("Byte encoding error", value, eis.readUnsignedByte());
			} else if (value <= 0xFFFFL && value >= 0) {
				assertEquals("Short prefix error", 0xFD, eis.readUnsignedByte());
				assertEquals("Short encoding error", value, eis.readLEShort() & 0xFFFFL);
			} else if (value <= 0xFFFFFFFFL && value >= 0) {
				assertEquals("Int prefix error", 0xFE, eis.readUnsignedByte());
				assertEquals("Int encoding error", value, eis.readLEInt() & 0xFFFFFFFFL);
			} else {
				assertEquals("Long prefix error", 0xFF, eis.readUnsignedByte());
				assertEquals("Long encoding error", value, eis.readLELong());
			}
		}
	}
	
	@Test
	public void decodeTest() throws IOException {
		
		Random r = new Random();
		
		for (int i = 0; i < 64; i++) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			long value;
			if (r.nextBoolean()) {
				value = r.nextInt(0xFD);
				eos.write((byte) value);
			} else if (r.nextBoolean()) {
				value = r.nextInt() & 0xFFFF;
				eos.write((byte) 0xFD);
				eos.writeLEShort((short) value);
			} else if (r.nextBoolean()) {
				value = r.nextInt() & 0xFFFFFFFF;
				eos.write((byte) 0xFE);
				eos.writeLEInt((int) value);
			} else {
				value = r.nextLong();
				eos.write((byte) 0xFF);
				eos.writeLELong(value);
			}
			eos.flush();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			EndianDataInputStream eis = new EndianDataInputStream(bis);
			
			VarInt v = new VarInt(Version.VERSION, eis);
			
			assertEquals("Encoding error", value, v.get());
			
		}
	}

}
