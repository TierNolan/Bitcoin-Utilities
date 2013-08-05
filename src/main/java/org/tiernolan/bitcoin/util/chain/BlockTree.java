package org.tiernolan.bitcoin.util.chain;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.types.BlockHeader;
import org.tiernolan.bitcoin.util.protocol.types.Hash;
import org.tiernolan.bitcoin.util.protocol.types.TargetBits;

public class BlockTree {
	
	private BigInteger bestPOW = BigInteger.ZERO;
	
	private final HashMap<Hash, BlockHeader> orphanHeaders = new HashMap<Hash, BlockHeader>();
	
	private final HashMap<Hash, BlockTreeLink> tree = new HashMap<Hash, BlockTreeLink>();
	
	private final ArrayList<BlockTreeLink> mainChain = new ArrayList<BlockTreeLink>();

	private final BigInteger maxPOW;
	
	private final boolean checkPOW;
	
	private ArrayList<TreeMonitor> treeMonitors = new ArrayList<TreeMonitor>(1);
	
	private BlockTreeLink mainLeaf;
	
	public BlockTree(BlockHeader genesis, BigInteger minPOW) {
		this(genesis, minPOW, true);
	}
	
	public BlockTree(BlockHeader genesis, BigInteger maxPOW, boolean checkPOW) {
		this.mainLeaf = new BlockTreeLink(null, genesis, BigInteger.ZERO, 0);
		this.tree.put(genesis.getBlockHash(), mainLeaf);
		this.mainChain.add(mainLeaf);
		this.checkPOW = checkPOW;
		this.maxPOW = maxPOW;
	}
	
	/**
	 * Adds the monitor to the list of tree monitors
	 * 
	 * @param monitor
	 * @return
	 */
	public synchronized boolean addTreeMonitor(TreeMonitor monitor) {
		for (TreeMonitor m : treeMonitors) {
			if (m == monitor) {
				return false;
			}
		}
		return treeMonitors.add(monitor);
	}
	
	/**
	 * Removes the monitor to the list of tree monitors
	 * 
	 * @param monitor
	 * @return
	 */
	public synchronized boolean removeTreeMonitor(TreeMonitor monitor) {
		return treeMonitors.remove(monitor);
	}
	
	/**
	 * Gets the BlockHeader on the main chain with height index
	 * 
	 * @param index the height, the genesis block is zero
	 * @return
	 */
	public synchronized BlockHeader getHeader(int index) {
		if (index < 0) {
			throw new IllegalArgumentException("Negative block indexes are invalid");
		} else if (index >= mainChain.size()) {
			return null;
		}
		return mainChain.get(index).getHeader();
	}
	
	/**
	 * Gets the previous BlockHeader from a BlockHeader.
	 * 
	 * @param header
	 * @return
	 */
	public synchronized BlockHeader getParent(BlockHeader header) {
		BlockTreeLink link = tree.get(header.getBlockHash());
		if (link == null) {
			return null;
		}
		BlockTreeLink parent = link.getPrevious();
		if (parent == null) {
			throw new IllegalStateException("Non-genesis block header has no parent");
		}
		return parent.getHeader();
	}
	
	/**
	 * Gets the next main chain BlockHeader from a BlockHeader.<br>
	 * 
	 * @param header
	 * @return
	 */
	public synchronized BlockHeader getNext(BlockHeader header) {
		BlockTreeLink link = tree.get(header.getBlockHash());
		if (link == null) {
			return null;
		}
		BlockTreeLink next = link.getMainChild();
		if (next == null) {
			return null;
		}
		return next.getHeader();
	}
	
	/**
	 * Gets if the BlockHeader is on the main chain
	 * 
	 * @param header
	 * @return
	 */
	public synchronized boolean isOnMain(BlockHeader header) {
		BlockTreeLink link = tree.get(header.getBlockHash());
		if (link == null) {
			return false;
		}
		return link.isOnMain();
	}
	
	/**
	 * Gets all child BlockHeaders from a BlockHeader
	 * 
	 * @param header
	 * @return
	 */
	public synchronized Collection<BlockHeader> getAllNext(BlockHeader header) {
		BlockTreeLink link = tree.get(header.getBlockHash());
		if (link == null) {
			return null;
		}
		Collection<BlockTreeLink> childLinks = new ArrayList<BlockTreeLink>(link.getChildren());
		Collection<BlockHeader> children = new ArrayList<BlockHeader>(childLinks.size());
		for (BlockTreeLink l : childLinks) {
			children.add(l.getHeader());
		}
		if (children.size() == 0) {
			return null;
		}
		return children;
	}
	
	/**
	 * Gets the distance from the genesis block to a BlockHeader
	 * 
	 * @param header
	 * @return
	 */
	public synchronized int getHeight(BlockHeader header) {
		BlockTreeLink link = tree.get(header.getBlockHash());
		if (link == null) {
			return -1;
		}
		return link.getHeight();
	}
	
	/**
	 * Gets the number of confirms received by the header
	 * 
	 * @param header
	 * @return
	 */
	public synchronized int getConfirms(BlockHeader header) {
		BlockTreeLink link = tree.get(header.getBlockHash());
		if (link == null) {
			return -1;
		}
		if (!link.isOnMain()) {
			return -1;
		}
		return getHeight() - link.getHeight();
	}
	
	/**
	 * Adds a BlockHeader to the tree.  
	 * 
	 * @param header
	 * @return true if the BlockHeader was added to the tree or the orphan store
	 * @throws MisbehaveException
	 */
	public synchronized boolean add(BlockHeader header) throws MisbehaveException {
		
		if (header == null) {
			throw new IllegalArgumentException("Cannot add null header");
		}
		
		while (header != null) {

			if (checkPOW && !header.checkPOW()) {
				throw new MisbehaveException(MisbehaveException.CRITICAL, "Insufficient proof of work");
			}

			Hash prevHash = header.getPrevious();
			BlockTreeLink prevLink = tree.get(prevHash);
			if (prevLink == null) {
				return orphanHeaders.put(prevHash, header) == null;
			}

			if (checkPOW) {
				TargetBits expectedTarget = getRetarget(prevLink);
				if (!expectedTarget.equals(header.getTarget())) {
					throw new MisbehaveException(MisbehaveException.CRITICAL, "Incorrect difficulty value");
				}
			}
			
			Hash blockHash = header.getBlockHash();
			
			if (tree.containsKey(blockHash)) {
				return false;
			}

			BigInteger blockPOW = header.getBlockWork();
			
			BigInteger parentPOW = prevLink.getPOW();
			
			BigInteger newPOW = blockPOW.add(parentPOW);
			
			BlockTreeLink link = new BlockTreeLink(prevLink, header, newPOW, prevLink.getHeight() + 1);

			prevLink.addChild(link);
			
			if (tree.put(blockHash, link) != null) {
				throw new IllegalStateException("Header added to chain twice");
			}

			if (newPOW.compareTo(bestPOW) > 0) {
				bestPOW = newPOW;
				BlockTreeLink oldPath = mainLeaf;
				BlockTreeLink newPath = link;

				while (mainChain.size() < newPath.getHeight() + 1) {
					mainChain.add(null);
				}

				while (mainChain.size() > newPath.getHeight() + 1) {
					mainChain.remove(mainChain.size() - 1);
				}

				boolean done = false;
				
				LinkedList<BlockHeader> reorgReplaced = new LinkedList<BlockHeader>();
				LinkedList<BlockHeader> reorgNew = new LinkedList<BlockHeader>();
				
				while (!done) {
					while (oldPath.getHeight() > newPath.getHeight()) {
						reorgReplaced.add(oldPath.getHeader());
						oldPath.setOnFork();
						oldPath = oldPath.getPrevious();
					}
					
					done = oldPath == newPath;
					
					if (!done) {
						mainChain.set(newPath.getHeight(), newPath);
						reorgNew.addFirst(newPath.getHeader());
						
						if (!newPath.getPrevious().setMainChild(newPath)) {
							throw new IllegalStateException("Link not correctly recorded on parent link");
						}
						
						newPath = newPath.getPrevious();
					}
				}
				for (TreeMonitor m : treeMonitors) {
					for (BlockHeader h : reorgReplaced) {
						m.handle(h, true);
					}
					for (BlockHeader h : reorgNew) {
						m.handle(h, false);
					}
				}
				mainLeaf = link;
			} else {
				link.setOnFork();
			}

			header = orphanHeaders.remove(header.getBlockHash());

		}
		
		return true;
		
	}
	
	/**
	 * Gets a set of Hashes for the block locator field.<br>
	 * <br>
	 * The last 10 hashes on the main chain are included, and then the step size doubles.
	 * 
	 * @return
	 */
	public synchronized Hash[] getBlockLocator() {
		List<Hash> locators = new ArrayList<Hash>(20);
		int h;
		for (h = getHeight() - 1; h >= 0 && h > getHeight() - 10; h--) {
			locators.add(this.getHeader(h).getBlockHash());
		}
		int step = 1;
		for (; h > 0; h -= step) {
			locators.add(this.getHeader(h).getBlockHash());
			step = step << 1;
		}
		locators.add(getHeader(0).getBlockHash());
		return locators.toArray(new Hash[0]);
	}

	/**
	 * Gets the length of the main chain
	 * 
	 * @return
	 */
	public synchronized int getHeight() {
		return mainChain.size();
	}
	
	private synchronized TargetBits getRetarget(BlockTreeLink prevLink) {
		
		if (((prevLink.getHeight() + 1) % Message.RETARGET_INTERVAL) != 0) {
			return prevLink.getHeader().getTarget();
		}
		
		BlockTreeLink firstLink = prevLink;
		for (int i = 0; i < Message.RETARGET_INTERVAL - 1; i++) {
			if (firstLink == null) {
				throw new IllegalStateException("Scanned backwards past the genesis block for difficulty target");
			}
			firstLink = firstLink.getPrevious();
		}
		int end = prevLink.getHeader().getTimestamp();
		int start = firstLink.getHeader().getTimestamp();
		int timespan = end - start;
		if (timespan < (Message.RETARGET_TIMESPAN / 4)) {
			timespan = Message.RETARGET_TIMESPAN / 4;
		}
		if (timespan > (Message.RETARGET_TIMESPAN * 4)) {
			timespan = Message.RETARGET_TIMESPAN * 4;
		}
		BigInteger newTarget = prevLink.getHeader().getTarget().getTarget();
		newTarget = newTarget.multiply(BigInteger.valueOf(timespan));
		newTarget = newTarget.divide(BigInteger.valueOf(Message.RETARGET_TIMESPAN));
		
		if (newTarget.compareTo(maxPOW) > 0) {
			newTarget = maxPOW;
		}
		return new TargetBits(newTarget);
	}

}
