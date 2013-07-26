package org.tiernolan.bitcoin.util.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONReader extends BufferedReader {

	private final int openCurly = '{' & 0xFF;
	private final int closeCurly = '}' & 0xFF;
	
	private final int openSquare = '[' & 0xFF;
	private final int closeSquare = ']' & 0xFF;
	
	private final static int MAX_LENGTH = 8192;
	
	public JSONReader(InputStream in) {
		super(new InputStreamReader(in, StandardCharsets.UTF_8));
	}
	
	public boolean isJSONObject() throws IOException {
		return isJSONObject(MAX_LENGTH);
	}
	
	public boolean isJSONObject(int maxLength) throws IOException {
		skip(maxLength);
		mark(2);
		int b = read();
		reset();
		return b == openCurly;
	}
	
	public JSONObject getJSONObject() throws IOException {
		return getJSONObject(MAX_LENGTH);
	}
	
	public JSONObject getJSONObject(int maxLength) throws IOException {
		String string = getJSONString(maxLength, openCurly);
		if (string == null) {
			return null;
		}
		return new JSONObject(string);
	}
	
	public JSONArray getJSONArray() throws IOException {
		return getJSONArray(MAX_LENGTH);
	}
	
	public JSONArray getJSONArray(int maxLength) throws IOException {
		String string = getJSONString(maxLength, openSquare);
		if (string == null) {
			return null;
		}
		return new JSONArray(string);
	}
	
	private String getJSONString(int maxLength, int first) throws IOException {
		skip(maxLength);
		int curly = 0;
		int square = 0;
		int length = 0;
		
		mark(maxLength);

		int b = read();
		length++;
		
		if (b != first) {
			throw new IOException("JSON parser error, expected first character " + ((char) (first)));
		}
		
		if (b == openCurly) {
			curly++;
		} else if (b == openSquare) {
			square++;
		}
		
		while ((b = read()) != -1) {
			length++;
			
			if (b == openCurly) {
				curly++;
			} else if (b == openSquare) {
				square++;
			} else if (b == closeCurly) {
				curly--;
				if (curly < 0 || (curly == 0 && square == 0)) {
					break;
				}
			} else if (b == closeSquare) {
				square--;
				if (square < 0 || (square == 0 && curly == 0)) {
					break;
				}
			}
			if (length > maxLength - 1) {
				throw new IOException("JSON Object exceeds maximum length");
			}
		}
		
		if (curly != 0 || square != 0) {
			throw new IOException("Stream terminated before JSON Object was completed");
		}
		
		char[] out = new char[length];
		
		reset();
		
		int read = read(out);
		
		if (read != length) {
			throw new IOException("Object length calculation error");
		}
		
		return new String(out);
		
	}
	
	private void skip(int maxLength) throws IOException {
		for (int i = 0; i < maxLength; i++) {
			mark(2);
			int b = read();
			if (b == -1) {
				return;
			}
			if (b == openCurly || b == openSquare) {
				reset();
				return;
			}
		}
		throw new IOException("Whitespace threshold exceeded");
	}

}
