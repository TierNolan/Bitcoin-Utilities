package org.tiernolan.bitcoin.util.crypt;

import static org.junit.Assert.assertTrue;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.bitcoin.util.crypt.Crypt;
import org.tiernolan.bitcoin.util.crypt.KeyPair;

public class CryptTest {
	
	@Test
	public void encryptDecryptSingleByteTest() throws IOException {
		Random r = KeyPair.getSeededSecureRandom();
		
		byte[] iv = new byte[16];
		byte[] key = new byte[16];
		

		
		for (int i = 0; i < 5; i++) {
			r.nextBytes(iv);
			r.nextBytes(key);

			PipedInputStream pis = new PipedInputStream();
			PipedOutputStream pos = new PipedOutputStream(pis);

			OutputStream cos = Crypt.encrypt(key, iv, pos);

			byte[] data = new byte[r.nextInt(2048)];
			
			r.nextBytes(data);
			
			
			InputStream cis = Crypt.decrypt(key, iv, pis);
			
			for (int j = 0; j < data.length; j++) {
				
				int in = data[j] & 0xFF;
				cos.write((byte) in);
				cos.flush();
				
				int out = cis.read();
				
				assertTrue("Unexpected EOF", out >= 0);

				assertTrue("Unexpected data", out == in);
				
			}
			
			cos.close();
			int out = cis.read();
			assertTrue("EOF missing", out == -1);
		}
		
		
	}
	
	@Test
	public void encryptDecryptBurstTest() throws IOException, InterruptedException {
		Random r = KeyPair.getSeededSecureRandom();
		
		byte[] iv = new byte[16];
		byte[] key = new byte[16];
		

		
		for (int i = 0; i < 5; i++) {
			r.nextBytes(iv);
			r.nextBytes(key);

			PipedInputStream pis = new PipedInputStream();
			PipedOutputStream pos = new PipedOutputStream(pis);

			final OutputStream cos = Crypt.encrypt(key, iv, pos);

			final byte[] data = new byte[r.nextInt(2048)];
			
			r.nextBytes(data);
			
			InputStream cis = Crypt.decrypt(key, iv, pis);

			Thread t = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						cos.write(data);
					} catch (IOException e) {
					} finally {
						try {
							cos.close();
						} catch (IOException e) {
						}
					}
				}
				
			});
			
			t.start();
			
			byte[] processedData = new byte[data.length];
			
			int read = 0;
			while (read < processedData.length) {
				int newData = cis.read(processedData, read, processedData.length - read);
				if (newData < 0) {
					throw new EOFException("Unexpected EOF");
				}
				read += newData;
			}
			
			assertTrue("Processed data not equal to input data", Arrays.equals(processedData, data));

			t.join();
			int out = cis.read();
			assertTrue("EOF missing", out == -1);
		}
		
		
	}

}
