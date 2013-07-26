package org.tiernolan.bitcoin.util.json;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONWriter extends OutputStreamWriter {
	
	public JSONWriter (OutputStream out) {
		super(out, StandardCharsets.UTF_8);
	}
	
	public void writeJSONObject(JSONObject o) throws IOException {
		o.write(this);
	}
	
	public void getJSONArray(JSONArray a) throws IOException {
		a.write(this);
	}
}
