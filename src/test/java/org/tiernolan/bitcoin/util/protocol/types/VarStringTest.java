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

public class VarStringTest {
	
	@Test
	public void encodeTest() throws IOException {
		
		Random r = new Random();
		
		for (int i = 0; i < 64; i++) {
			
			char[] string = new char[r.nextInt(10) + 10];
			byte[] bytes = new byte[string.length];
			
			r.nextBytes(bytes);
			
			for (int j = 0; j < bytes.length; j++) {
				string[j] = (char) (bytes[j] & 0xFF);
			}
			
			String s = new String(string);
			
			VarString vs = new VarString(s);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			
			vs.write(0, eos);
			eos.flush();
			
			byte[] encoded = bos.toByteArray();
			
			EndianDataInputStream eis = new EndianDataInputStream(new ByteArrayInputStream(encoded));
			
			VarInt length = new VarInt(Version.VERSION, eis);
			
			assertEquals("Decoded string length does not match", bytes.length, (int) length.get());
			
			for (int j = 0; j < bytes.length; j++) {
				assertEquals("Character mismatch at position " + j, bytes[j], eis.readByte());
			}
		}
	}
	
	@Test
	public void decodeTest() throws IOException {
		
		Random r = new Random();

		for (int i = 0; i < 64; i++) {

			char[] string = new char[r.nextInt(10) + 10];
			byte[] bytes = new byte[string.length];

			r.nextBytes(bytes);

			for (int j = 0; j < bytes.length; j++) {
				string[j] = (char) (bytes[j] & 0xFF);
			}

			String s = new String(string);

			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			
			eos.writeByte(string.length);
			
			for (int j = 0; j < bytes.length; j++) {
				eos.writeByte(bytes[j]);
			}
			
			eos.flush();
			
			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
			EndianDataInputStream eis = new EndianDataInputStream(bis);
			
			VarString vs = new VarString(Version.VERSION, eis);
			
			assertEquals("Encoding error", s, vs.get());
			
		}
	}

}
