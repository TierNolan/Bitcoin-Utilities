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
	
	public static int compare(byte[] a, byte[] b, int length) {
		return compare(rightJustify(a, length), rightJustify(b, length));
	}

	public static int compare(byte[] a, byte[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("Arrays must be the same length to compare");
		}
		for (int i = 0; i < a.length; i++) {
			int d = (a[i] & 0xFF) - (b[i] & 0xFF);
			if (d != 0) {
				return d;
			}
		}
		return 0;
	}
	
	public static byte[] reverse(byte[] a) {
		int j = a.length - 1;
		for (int i = 0; i < a.length / 2; i++) {
			byte t = a[i];
			a[i] = a[j];
			a[j] = t;
			j--;
		}
		return a;
	}

}
