package org.tiernolan.bitcoin.util.chain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.tiernolan.bitcoin.util.protocol.types.BlockHeader;

public class BlockTreeLink {
	private final BlockTreeLink previous;
	private final BlockHeader header;
	private final List<BlockTreeLink> children;
	private final BigInteger pow; // counts from root of chain
	private final int height;
	private int mainIndex;

	public BlockTreeLink(BlockTreeLink previous, BlockHeader header, BigInteger pow, int height) {
		this.previous = previous;
		this.header = header;
		this.pow = pow;
		this.children = new ArrayList<BlockTreeLink>();
		this.height = height;
		this.mainIndex = -1;
	}

	public BlockTreeLink getPrevious() {
		return previous;
	}

	public BlockHeader getHeader() {
		return header;
	}

	public void addChild(BlockTreeLink child) {
		children.add(child);
	}

	public Collection<BlockTreeLink> getChildren() {
		return new ArrayList<BlockTreeLink>(children);
	}

	public void setOnFork() {
		this.mainIndex = -1;
	}
	
	public boolean isOnMain() {
		return this.mainIndex >= 0;
	}

	public boolean setMainChild(BlockTreeLink child) {
		for (int i = 0; i < children.size(); i++) {
			if (children.get(i) == child) {
				mainIndex = i;
				return true;
			}
		}
		return false;
	}

	public BlockTreeLink getMainChild() {
		if (children.size() == 0) {
			return null;
		} else if (mainIndex == -1) {
			return null;
		} else if (mainIndex >= children.size() || mainIndex < 0) {
			throw new IllegalStateException("Main index of " + mainIndex + " is out of bounds for the child array");
		} else {
			return children.get(mainIndex);
		}
	}

	public int getHeight() {
		return height;
	}

	public BigInteger getPOW() {
		return pow;
	}

}
