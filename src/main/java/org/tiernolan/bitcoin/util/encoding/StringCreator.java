package org.tiernolan.bitcoin.util.encoding;

import org.bouncycastle.util.encoders.Hex;

public class StringCreator {
	
	private final StringBuilder sb = new StringBuilder();
	private boolean spacer = false;
	
	public StringCreator() {
		sb.append("{");
	}
	
	public StringCreator(String name) {
		sb.append(name);
		sb.append(" {");
	}

	public StringCreator add(String name, Object[] value) {
		addName(name);
		return addRaw(value);
	}
	
	public StringCreator add(String name, byte[] value) {
		addName(name);
		return addRaw(value);
	}
	
	public StringCreator add(String name, Object value) {
		addName(name);
		return addRaw(value);
	}
	
	public StringCreator add(Object[] value) {
		spacer();
		return addRaw(value);
	}
	
	public StringCreator add(byte[] value) {
		spacer();
		return addRaw(value);
	}

	public StringCreator add(Object value) {
		spacer();
		return addRaw(value);
	}

	public String toString() {
		return sb.toString();
	}
	
	private StringCreator addRaw(byte[] value) {
		return addRaw(Hex.toHexString(value));
	}
	
	private StringCreator addRaw(Object[] value) {
		StringCreator sc = new StringCreator();
		for (int i = 0; i < value.length; i++) {
			Object v = value[i];
			if (v instanceof byte[]) {
				sc.add((byte[]) v);
			} else if (v instanceof Object[]) {
				sc.add((Object[]) v);
			} else {
				sc.add(value[i]);
			}
		}
		return addRaw(sc.toString());
	}
	
	private StringCreator addRaw(Object value) {
		sb.append(value);
		return this;
	}

	private void addName(String name) {
		spacer();
		sb.append(name);
		sb.append(":");
	}
	
	private void spacer() {
		if (!spacer) {
			spacer = true;
		} else {
			sb.append(", ");
		}
	}
}
