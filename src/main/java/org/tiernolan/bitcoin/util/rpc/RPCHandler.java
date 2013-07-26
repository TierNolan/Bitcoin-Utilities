package org.tiernolan.bitcoin.util.rpc;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tiernolan.bitcoin.util.json.JSONHandler;

public class RPCHandler extends JSONHandler {
	
	public RPCHandler(String hostname, int port) {
		super(hostname, port);
	}
	
	public int getChainLength() {
		try {
			JSONObject o = query(build("id", "getblockcount"));
			return o.getInt("result");
		} catch (JSONException e) {
			return -1;
		}
	}
	
	public String getBlockHash(int index) {
		try {
			JSONObject o = query(build("id", "getblockhash", index));
			return o.getString("result");
		} catch (JSONException e) {
			return null;
		}
	}
	
	public JSONObject getBlock(String hash) {
		try {
			return query(build("id", "getblock", hash));
		} catch (JSONException e) {
			return null;
		}
	}
	
	public String getRawTransactionHex(String hash) {
		try {
			return query(build("id", "getrawtransaction", hash)).getString("result");
		} catch (JSONException e) {
			return null;
		}
	}
	
	public JSONObject getRawTransactionJSON(String hash) {
		try {
			return query(build("id", "getrawtransaction", hash, 1));
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	
	private final JSONObject build(String id, String method, Object...params) {
		JSONArray p = new JSONArray();
		
		for (int i = 0; i < params.length; i++) {
			if (Integer.class.equals(params[i].getClass())) {
				p.put((Integer) params[i]);
			} else if (String.class.equals(params[i].getClass())) {
				p.put((String) params[i]);
			} else if (Boolean.class.equals(params[i].getClass())) {
				p.put((Boolean) params[i]);
			} else {
				throw new IllegalStateException("Unknown parameter class " + params[i].getClass());
			}
		}
		
		return new JSONObject()
			.put("id", id)
			.put("method", method)
			.put("params", p);
	}

}
