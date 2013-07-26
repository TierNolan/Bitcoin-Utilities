package org.tiernolan.bitcoin.util.protocol.types;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class MutableHashTest {
	
	@Test
	public void encodeTest() throws IOException {
		
		Random r = new Random();
		
		MutableHash h = new MutableHash(15);
		
		for (int i = 0; i < 20; i++) {
			byte[] input = new byte[15];
			
			r.nextBytes(input);
			
			for (int j = 0; j < 15; j++) {
				h.set(j, input[j]);
			}
			
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			EndianDataOutputStream eos = new EndianDataOutputStream(bos);
			
			h.write(0, eos);
			eos.flush();
			
			byte[] encoded = bos.toByteArray();
			
			assertTrue("Encoding error", Arrays.equals(input, encoded));
			
		}
	}
	
	@Test
	public void decodeTest() throws IOException {
		
		Random r = new Random();
		
		MutableHash h = new MutableHash(15);
		
		for (int i = 0; i < 20; i++) {
			byte[] input = new byte[15];
			
			r.nextBytes(input);
			
			ByteArrayInputStream bis = new ByteArrayInputStream(input);
			EndianDataInputStream eis = new EndianDataInputStream(bis);
			
			h.read(eis);
			
			assertTrue("Encoding error", Arrays.equals(h.getData(), input));
			
		}
	}

}
