package org.tiernolan.bitcoin.util.protocol.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.tiernolan.bitcoin.util.crypt.Digest;
import org.tiernolan.bitcoin.util.encoding.ByteArray;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;
import org.tiernolan.bitcoin.util.protocol.message.Version;

public class BlockHeader implements MessageType {

	private static final BigInteger BLOCK_WORK_NUMERATOR = BigInteger.ONE.shiftLeft(256);
	
	private final int version;
	private final Hash prev;
	private final Hash merkle;
	private final int timestamp;
	private final TargetBits bits;
	private final int nonce;
	private final VarInt txCount;
	
	private final Hash blockHash;
	
	public BlockHeader(int version, Hash prev, Hash merkle, int timestamp, BigInteger target, int nonce, int txCount) {
		this.version = version;
		if (prev.getLength() != 32 || merkle.getLength() != 32) {
			throw new IllegalArgumentException("Previous and merkle hashes must be 32 bytes");
		}
		this.prev = prev.copy();
		this.merkle = merkle.copy();
		this.timestamp = timestamp;
		this.bits = new TargetBits(target);
		this.nonce = nonce;
		
		this.txCount = new VarInt(txCount);
		
		try {
			this.blockHash = getHash(version, prev, merkle, timestamp, bits.getBits(), nonce);
		} catch (IOException e) {
			throw new IllegalStateException("Block hash calculations should not cause an IOException", e);
		}
	}
	
	public BlockHeader(int version, EndianDataInputStream in) throws IOException {
		this.version = in.readLEInt();
		this.prev = new Hash(in, 32);
		this.merkle = new Hash(in, 32);
		this.timestamp = in.readLEInt();
		this.bits = new TargetBits(version, in);
		this.nonce = in.readLEInt();
		
		this.txCount = new VarInt(version, in);
		
		this.blockHash = getHash(this.version, prev, merkle, timestamp, bits.getBits(), nonce);
	}

	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(this.version);
		prev.write(version, out);
		merkle.write(version, out);
		out.writeLEInt(timestamp);
		bits.write(version, out);
		out.writeLEInt(nonce);
	}
	
	public int getVersion() {
		return version;
	}
	
	public Hash getPrevious() {
		return prev.copy();
	}
	
	public Hash getMerkle() {
		return merkle.copy();
	}
	
	public int getTimestamp() {
		return timestamp;
	}
	
	public TargetBits getTarget() {
		return bits;
	}
	
	public BigInteger getBlockWork() {
		return BLOCK_WORK_NUMERATOR.divide(bits.getTarget().add(BigInteger.ONE));
	}
	
	public int getNonce() {
		return nonce;
	}
	
	public long getTxCount() {
		return txCount.get();
	}
	
	public Hash getBlockHash() {
		return blockHash.copy();
	}
	
	public boolean checkPOW() {
		byte[] hash = blockHash.getReverseData();
		byte[] target = bits.getTarget().toByteArray();
		return ByteArray.compare(hash, target, 32) <= 0;
	}
	
	private static Hash getHash(int version, Hash prev, Hash merkle, int timestamp, int bits, int nonce) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(80);
		EndianDataOutputStream eos = new EndianDataOutputStream(bos);
		
		eos.writeLEInt(version);
		prev.write(Version.VERSION, eos);
		merkle.write(Version.VERSION, eos);
		eos.writeLEInt(timestamp);
		eos.writeLEInt(bits);
		eos.writeLEInt(nonce);
		eos.flush();
		
		byte[] header = bos.toByteArray();
		byte[] hash = Digest.doubleSHA256(header);
		return new Hash(hash);
	}
	
}
