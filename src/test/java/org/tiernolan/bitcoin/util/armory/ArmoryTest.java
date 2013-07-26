package org.tiernolan.bitcoin.util.armory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.util.LinkedList;

import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils;
import org.junit.Test;
import org.tiernolan.bitcoin.util.armory.Armory;
import org.tiernolan.bitcoin.util.crypt.Crypt;
import org.tiernolan.bitcoin.util.crypt.KeyPair;
import org.tiernolan.bitcoin.util.crypt.NetPrefix;
import org.tiernolan.bitcoin.util.crypt.Secp256k1;


public class ArmoryTest {
	
	private final static int PUB_KEY_NUMBER = 25;

	private final static String[][] paperTest = new String[][] {
			new String[] {
					"dwwj unhu khis funw ssjo drka eegi ggkj gkfn",
					"fwjh egru gawe oiwh ania arwo fkhi nsoo nhnj",
					"gstw kffj sohj aued hnen giof nihi nasg idhg",
					"anus grag doiu okhi ofit ieuo foww iohg nrfn"
			},
			new String[] {
					"oegn ddie fwrj rogg gooe jror auut wnus siwa",
					"tjug duwg ighf gweu taon sfen oerj hwse jffn",
					"skjk hsoe atha frrd hsgt wuie dsgw tuwu aght",
					"jhsr uusa aafa shju roko eith duus jgon juew"
			}
	};
	
	private final static String[] walletIds = new String[] {
			"LC1iqeBy",
			"2Jg5gsaZV"
	};
	
	private final static String[][] publicKeys = new String[][] {
			new String[] {
					"1LLctaoL53DHJ2AE75eNkAN6vccU1rN8c4",
					"1Pc4owwpJFVvFeFXjQG3GtGkmk2VMe9bDB",
					"1FkP8LTmanFYxxjC1D5raH9D7AjNo7w7sW",
					"1GaRZxPgnWVap83GBvNbCHrDAUydMcX2FB",
					"1Pbk1fJswAzJVowWpyoah7KyR3zHG4tDqo"
			},
			new String[] {
					"1EBHsJLnNKKciuCtiHDp5fCVttRh18fTux",
					"1GxpmR5YijFdi2uyaJx7gt1EgAGb1icw4g",
					"1FPcvqf6WJ99cwuEX388Ur6qDJYoTHiB4U",
					"1C9GyjRScoaqiWtYch3NwH4KAxb328MJH5",
					"1BWdAj1CbVdxPEqn5w7sp62T1ak7768aj7",
					"16poHQy6LbFqZ3zupfxKMLya1UZU52gZsj"
			}
			
	};
	
	@Test
	public void testPaper() {
		Crypt.init();
		
		for (int i = 0; i < paperTest.length; i++) {
			byte[] priv1 = Armory.decodeLine(paperTest[i][0].replace(" ", ""), 0);
			byte[] priv2 = Armory.decodeLine(paperTest[i][1].replace(" ", ""), 0);			
			byte[] chain1 = Armory.decodeLine(paperTest[i][2].replace(" ", ""), 0);
			byte[] chain2 = Armory.decodeLine(paperTest[i][3].replace(" ", ""), 0);
			
			byte[] key = ByteUtils.concatenate(new byte[] {0}, ByteUtils.concatenate(priv1, priv2));
			assertTrue("Private key is wrong length " + key.length, key.length == 33);
			
			byte[] chaincode = ByteUtils.concatenate(chain1, chain2);

			BigInteger privateKey = new BigInteger(key);
			KeyPair keyPair = new KeyPair(NetPrefix.MAIN_NET, privateKey);
			keyPair.attach("chaincode", chaincode);
			
			KeyPair root = keyPair;

			assertEquals("Wallet id mismatch", walletIds[i], Armory.getWalletId(root));
			
			String[] expectedKeys = publicKeys[i];

			keyPair = root.getPublicKeyPair();
			
			for (int j = 0; j < expectedKeys.length; j++) {
				KeyPair nextKeyPair = Armory.getNext(keyPair);
				assertEquals("Address does not match expected when generating from public key", expectedKeys[j], nextKeyPair.getAddress(false));
				assertTrue("Private key should not be present in KeyPair", keyPair.getPrivateKey() == null);
				keyPair = nextKeyPair;
			}
			
			keyPair = root;
			
			for (int j = 0; j < expectedKeys.length; j++) {
				KeyPair nextKeyPair = Armory.getNext(keyPair);
				assertEquals("Address does not match expected when generating from private key", expectedKeys[j], nextKeyPair.getAddress(false));
				assertTrue("Private key should be present in KeyPair", keyPair.getPrivateKey() != null);
				keyPair = nextKeyPair;
			}
		}
	}
	
	@Test
	public void testSequence() {
		Crypt.init();
		
		byte[] priv1 = Armory.decodeLine(paperTest[0][0].replace(" ", ""), 0);
		byte[] priv2 = Armory.decodeLine(paperTest[0][1].replace(" ", ""), 0);			
		byte[] chain1 = Armory.decodeLine(paperTest[0][2].replace(" ", ""), 0);
		byte[] chain2 = Armory.decodeLine(paperTest[0][3].replace(" ", ""), 0);
		
		byte[] key = ByteUtils.concatenate(new byte[] {0}, ByteUtils.concatenate(priv1, priv2));
		assertTrue("Private key is wrong length " + key.length, key.length == 33);
		
		byte[] chaincode = ByteUtils.concatenate(chain1, chain2);

		BigInteger privateKey = new BigInteger(key);
		KeyPair keyPair = new KeyPair(NetPrefix.MAIN_NET, privateKey);
		keyPair.attach("chaincode", chaincode);
		
		KeyPair pubKey = keyPair.getPublicKeyPair();
		
		LinkedList<String> publicAddresses = new LinkedList<String>();
		
		for (int i = 0; i < PUB_KEY_NUMBER; i++) {
			assertTrue("Private key present in public key pair", pubKey.getPrivateKey() == null);
			publicAddresses.add(pubKey.getAddress(false));
			publicAddresses.add(pubKey.getAddress(true));
			pubKey = Armory.getNext(pubKey);
		}
		
		KeyPair privKey = keyPair;
		
		for (int i = 0; i < PUB_KEY_NUMBER; i++) {
			assertTrue("No private key present in private key pair", privKey.getPrivateKey() != null);
			assertEquals("Uncompressed address mismatch", publicAddresses.removeFirst(), privKey.getAddress(false));
			assertEquals("Compressed address mismatch", publicAddresses.removeFirst(), privKey.getAddress(true));
			BigInteger pri = privKey.getPrivateKey();
			ECPoint pub = privKey.getPublicKey();
			assertEquals("Public and private keys don't match", pub, Secp256k1.getPoint(pri));
			privKey = Armory.getNext(privKey);
		}
		
		

	}
	
}
