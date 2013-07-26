package org.tiernolan.bitcoin.util.encoding;

public class ByteArray {
	
	public static byte[] rightJustify(byte[] bytes, int length) {
		
		if (bytes.length == length) {
			return bytes;
		}
		
		byte[] temp = new byte[length];
		
		int j = length - 1;
		for (int i = bytes.length - 1; i >= 0 && j >= 0;) {
			temp[j--] = bytes[i--];
		}
		
		return temp;
		
	}

}
