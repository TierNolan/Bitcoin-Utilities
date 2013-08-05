package org.tiernolan.bitcoin.util.chain;

import org.tiernolan.bitcoin.util.protocol.types.BlockHeader;

public interface TreeMonitor {

	/**
	 * Handles an BlockHeader event
	 * 
	 * @param header
	 * @param replaced true if the header was part of a fork that was replaced
	 */
	public void handle(BlockHeader header, boolean replaced);
	
}
