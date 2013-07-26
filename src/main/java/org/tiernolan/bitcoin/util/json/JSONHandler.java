package org.tiernolan.bitcoin.util.json;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import org.json.JSONException;
import org.json.JSONObject;


public class JSONHandler {

	private final static AtomicBoolean authSet = new AtomicBoolean(false);

	public static void setLoginDetails(final String username, final String password) {
		if (authSet.compareAndSet(false, true)) {
			Authenticator.setDefault(new Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication (username, password.toCharArray());
				}
			});
		}
	}

	private final String hostname;
	private final int port;
	
	public JSONHandler(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}
	
	public JSONObject query(JSONObject o) {
		try {
			return queryJSON(hostname, port, o);
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}
	
	public String queryHex(JSONObject o) {
		try {
			return queryHexString(hostname, port, o);
		} catch (IOException e) {
			throw new JSONException(e);
		}
	}
	
	private static HttpURLConnection sendQuery(String hostname, int port, JSONObject o) throws IOException {
		final Writer writer;
		
		HttpURLConnection connection = null;;
		OutputStream out = null;

		URL url = new URL("http", hostname, port, "/");
		connection = (HttpURLConnection) url.openConnection();

		connection.setDoOutput(true);

		out = connection.getOutputStream();

		writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));

		o.write(writer);
		writer.flush();
		writer.close();

		return connection;
	}
	
	private static JSONObject queryJSON(String hostname, int port, JSONObject o) throws IOException {

		HttpURLConnection connection = null;
		InputStream in = null;
		Reader reader;

		try {
			connection = sendQuery(hostname, port, o);

			in = connection.getInputStream();
			reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));

			return new JSONObject(readJSONString(reader));

		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}

	}
	
	private static String queryHexString(String hostname, int port, JSONObject o) throws IOException {

		HttpURLConnection connection = null;
		InputStream in = null;

		try {
			connection = sendQuery(hostname, port, o);

			in = connection.getInputStream();
			
			StringBuilder sb = new StringBuilder();
			
			byte[] buf = new byte[8192];
			char[] chars = new char[8192];
			
			int read = 0;
			while (read >= 0) {
				read = in.read(buf);
				if (read > 0) {
					for (int i = 0; i < read; i++) {
						chars[i] = (char) (buf[i] & 0xFF);
					}
					sb.append(chars, 0, read);
				}
			}
			
			return sb.toString();

		} catch (IOException e) {
			throw e;
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
				}
			}
			if (connection != null) {
				connection.disconnect();
			}
		}

	}

	
	private static String readJSONString(Reader reader) {
		char[] buf = new char[256];
		StringBuilder sb = new StringBuilder();
		
		int read = 0;
		
		while (read != -1) {
			try {
				read = reader.read(buf);
			} catch (IOException e) {
				throw new JSONException(e);
			}
			if (read > 0) {
				sb.append(buf, 0, read);
			}
		}
		return sb.toString();
	}
}
