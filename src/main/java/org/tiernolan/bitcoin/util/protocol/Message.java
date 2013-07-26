package org.tiernolan.bitcoin.util.protocol;

import java.util.HashMap;
import java.util.Map;

import org.bouncycastle.util.encoders.Hex;
import org.tiernolan.bitcoin.util.protocol.types.Hash;


public abstract class Message implements MessageType {
	
	private static HashMap<Integer, Hash> map = new HashMap<Integer, Hash>();
	
	public static final int MAGIC_MAINNET = 0xD9B4BEF9;
	public static final int MAGIC_TESTNET = 0xD9B4BEF9;
	
	public static final int EOF = -1;
	
	public static final int VERSION = add("version");
	public static final int VERACK = add("verack");
	public static final int ADDR = add("addr");
	public static final int INV = add("inv");
	public static final int GETDATA = add("getdata");
	public static final int NOTFOUND = add("notfound");
	public static final int GETBLOCKS = add("getblocks");
	public static final int GETHEADERS = add("getheaders");
	public static final int TX = add("tx");
	public static final int BLOCK = add("block");
	public static final int HEADERS = add("headers");
	public static final int GETADDR = add("getaddr");
	public static final int CHECKORDER = add("checkorder");
	public static final int SUBMITORDER = add("submitorder");
	public static final int REPLY = add("reply");
	public static final int PING = add("ping");
	public static final int PONG = add("pong");
	public static final int FILTERLOAD = add("filterload");
	public static final int FILTERADD = add("filteradd");
	public static final int FILTERCLEAR = add("filterclear");
	public static final int MERKLEBLOCK = add("merkleblock");
	public static final int ALERT = add("alert");
	
	public static final int UNKNOWN = add("");

	private final static int mask;
	private final static Hash[] commandArray;
	
	static {
		commandArray = findMask();
		mask = commandArray.length - 1;
		map = null;
	}
	
	public static int getId(Hash h) {
		Hash c = commandArray[h.hashCode() & mask];

		if (h.equals(c)) {
			return h.hashCode();
		}
		return UNKNOWN;
	}
	
	private static Hash[] findMask() {
		int mask = 1;
		loop1:
		while (true) {
			Hash[] commands = new Hash[mask + 1];
			for (Map.Entry<Integer, Hash> e : map.entrySet()) {
				if (commands[e.getKey() & mask] == null && ((int) e.getKey()) != -1) {
					commands[e.getKey() & mask] = e.getValue();
				} else if (mask < 0xFFFF) {
					mask = (mask << 1) + 1;
					continue loop1;
				} else {
					throw new IllegalStateException("Unable to add all keys into array of size" + commands.length);
				}
			}
			return commands;
		}
	}
	
	private static int add(String command) {
		Hash h = getCommandHash(command);
		map.put(h.hashCode(), h);
		return h.hashCode();
	}
	
	private static Hash getCommandHash(String command) {
		if (command.length() > 12) {
			throw new IllegalArgumentException("Name exceeds 12 characters");
		}
		char[] chars = command.toCharArray();
		byte[] data = new byte[12];
		for (int i = 0; i < chars.length; i++) {
			if ((chars[i] & 0xFF80) != 0) {
				throw new IllegalArgumentException("Only ASCII characters are permitted");
			}
			data[i] = (byte) chars[i];
		}
		return new Hash(data);
	}
	
	private final String command;
	
	protected Message(String command) {
		this.command = command;
	}
	
	public String getCommand() {
		return command;
	}

}
