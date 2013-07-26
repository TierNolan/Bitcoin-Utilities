package org.tiernolan.bitcoin.util.protocol;

import java.io.IOException;

import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public interface MessageType {
	
	public void write(int version, EndianDataOutputStream out) throws IOException;

}
