package org.tiernolan.bitcoin.util.chain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.Random;

import org.junit.Test;
import org.tiernolan.bitcoin.util.protocol.types.BlockHeader;
import org.tiernolan.bitcoin.util.protocol.types.Hash;

public class BlockTreeTest {
	
	@Test
	public void testSimpleChain() throws MisbehaveException {
		
		Random r = new Random();
		
		BlockHeader genesis = getHeader(r);
		
		BlockTree tree = new BlockTree(genesis, null, false);
		
		BlockHeader first = getHeader(genesis.getBlockHash(), r);
		
		BlockHeader second = getHeader(first.getBlockHash(), r);
		
		assertTrue("Unable to add first link", tree.add(first));
		
		assertTrue("Unable to add second link", tree.add(second));
		
		assertEquals("Unable to get first link", first, tree.getHeader(1));
		
		assertEquals("Unable to get second link", second, tree.getHeader(2));
		
	}
	
	@Test
	public void testReverseChain() throws MisbehaveException {
		
		Random r = new Random();
		
		BlockHeader genesis = getHeader(r);
		
		BlockTree tree = new BlockTree(genesis, null, false);
		
		BlockHeader first = getHeader(genesis.getBlockHash(), r);
		
		BlockHeader second = getHeader(first.getBlockHash(), r);

		assertTrue("Unable to add second link", tree.add(second));
		
		assertTrue("Unable to add first link", tree.add(first));
		
		assertEquals("Unable to get first link", first, tree.getHeader(1));
		
		assertEquals("Unable to get second link", second, tree.getHeader(2));
		
	}
	
	@Test
	public void testRandomAdd() throws MisbehaveException {
		Random r = new Random();
		
		BlockHeader genesis = getHeader(r);
		
		BlockTree tree = new BlockTree(genesis, null, false);
		
		BlockHeader[] ordered = new BlockHeader[50];
		
		BlockHeader prev = genesis;
		
		for (int i = 0; i < ordered.length; i++) {
			ordered[i] = getHeader(prev.getBlockHash(), r);
			prev = ordered[i];
		}
		
		BlockHeader[] shuffled = new BlockHeader[50];
		
		System.arraycopy(ordered, 0, shuffled, 0, ordered.length);
		
		for (int i = 0; i < shuffled.length; i++) {
			int t = i + r.nextInt(shuffled.length - i);
			BlockHeader h = shuffled[i];
			shuffled[i] = shuffled[t];
			shuffled[t] = h;
		}
		
		for (int i = 0; i < shuffled.length; i++) {
			assertTrue("Unable to add link", tree.add(shuffled[i]));
		}
		
		for (int i = 0; i < ordered.length; i++) {
			assertEquals("Headers not in expected order", ordered[i], tree.getHeader(i + 1));
		}
	}
	
	@Test 
	public void testOrphans() throws MisbehaveException {
		
		Random r = new Random();
		
		BlockHeader genesis = getHeader(r);
		
		BlockTree tree = new BlockTree(genesis, null, false);
		
		BlockHeader[] main = new BlockHeader[20];
		BlockHeader[] orph = new BlockHeader[20];
		
		main[0] = getHeader(genesis.getBlockHash(), r);
		orph[0] = getHeader(genesis.getBlockHash(), r);
		
		for (int i = 1; i < main.length; i++) {
			if (r.nextBoolean()) {
				main[i] = getHeader(main[i - 1].getBlockHash(), r);
				orph[i] = getHeader(main[i - 1].getBlockHash(), r);
			} else {
				orph[i] = getHeader(main[i - 1].getBlockHash(), r);
				main[i] = getHeader(main[i - 1].getBlockHash(), r);
			}
		}
		
		for (int i = 0; i < main.length; i++) {
			assertTrue("Unable to add main header", tree.add(main[i]));
			assertTrue("Unable to add orphan header", tree.add(orph[i]));
		}
		
		for (int i = 0; i < main.length; i++) {
			assertEquals("Unexpected main chain header", main[i], tree.getHeader(i + 1));
		}
		
		for (int i = 0; i < main.length; i++) {
			assertFalse("Orphan part of main chain", tree.isOnMain(orph[i]));
		}
		
	}
	
	@Test 
	public void testFork() throws MisbehaveException {
		
		Random r = new Random();
		
		BlockHeader genesis = getHeader(r);
		
		BlockTree tree = new BlockTree(genesis, null, false);
		
		BlockHeader[] main = new BlockHeader[20];
		BlockHeader[] fork = new BlockHeader[20];
		
		main[0] = getHeader(genesis.getBlockHash(), r);
		
		for (int i = 1; i < main.length; i++) {
			main[i] = getHeader(main[i - 1].getBlockHash(), r);
		}
		
		fork[0] = getHeader(main[10].getBlockHash(), r);
		
		for (int i = 1; i < fork.length; i++) {
			fork[i] = getHeader(fork[i - 1].getBlockHash(), r);
		}
		
		for (int i = 0; i < main.length; i++) {
			assertTrue("Unable to add main header", tree.add(main[i]));
		}
		
		for (int i = 0; i < main.length; i++) {
			assertEquals("Unexpected main chain header", main[i], tree.getHeader(i + 1));
		}
		
		for (int i = 0; i < 9; i++) {
			assertTrue("Unable to add fork header", tree.add(fork[i]));
		}
		
		for (int i = 0; i < main.length; i++) {
			assertEquals("Main chain replaced by fork even when POW is equal", main[i], tree.getHeader(i + 1));
		}
		
		assertTrue("Unable to add main header", tree.add(fork[9]));
		
		assertFalse("Main leaf not displaced when fork had more POW", tree.isOnMain(main[main.length - 1]));
		
		for (int i = 10; i < fork.length; i++) {
			assertTrue("Unable to add fork header", tree.add(fork[i]));
		}
		
		for (int i = 0; i < 10; i++) {
			assertEquals("Unexpected main chain header", main[i], tree.getHeader(i + 1));
		}
		
		for (int i = 11; i < 11 + fork.length; i++) {
			assertEquals("Unexpected main chain header (after fork replacement)", fork[i - 11], tree.getHeader(i + 1));
		}
	}
	
	private static BlockHeader getHeader(Random r) {
		return getHeader(getRandomHash(r), r);
	}
	
	private static BlockHeader getHeader(Hash previous, Random r) {
		return new BlockHeader(1, previous, getRandomHash(r), 0, BigInteger.ONE, 0, 0);
	}
	
	private static Hash getRandomHash(Random r) {
		
		byte[] data = new byte[32];
		r.nextBytes(data);
		return new Hash(data);
	}

}
