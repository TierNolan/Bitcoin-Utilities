package org.tiernolan.bitcoin.util.protocol.types;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.tiernolan.bitcoin.util.encoding.StringCreator;
import org.tiernolan.bitcoin.util.protocol.Message;
import org.tiernolan.bitcoin.util.protocol.MessageType;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataInputStream;
import org.tiernolan.bitcoin.util.protocol.endian.EndianDataOutputStream;

public class Transaction implements MessageType {

	private final int version;
	private final VarInt inCount;
	private final TxIn[] inArr;
	private final VarInt outCount;
	private final TxOut[] outArr;
	private final int lockTime;
	
	private final Hash txHash;
	
	public Transaction(int version, TxIn[] in, TxOut[] out, int lockTime) {
		this.version = version;
		this.inCount = new VarInt(in.length);
		this.inArr = in;
		this.outCount = new VarInt(out.length);
		this.outArr = out;
		this.lockTime = lockTime;
		try {
			this.txHash = Message.getHash(version, this);
		} catch (IOException e) {
			throw new IllegalStateException("Transaction hash calculations should not cause an IOException", e);
		}
	}
	
	public Transaction(int version, EndianDataInputStream in) throws IOException {
		this.version = in.readLEInt();
		this.inCount = new VarInt(version, in);
		if (inCount.get() < 1 || inCount.get() > 1000000) {
			throw new IOException("Input count " + inCount.get() + " out of range");
		}
		List<TxIn> inList = new ArrayList<TxIn>(5);
		for (int i = 0; i < (int) inCount.get(); i++) {
			inList.add(new TxIn(version, in));
		}
		this.inArr = inList.toArray(new TxIn[0]);
		
		this.outCount = new VarInt(version, in);
		if (outCount.get() < 1 || outCount.get() > 1000000) {
			throw new IOException("Output count " + inCount.get() + " out of range");
		}
		List<TxOut> outList = new ArrayList<TxOut>(5);
		for (int i = 0; i < (int) inCount.get(); i++) {
			outList.add(new TxOut(version, in));
		}
		this.outArr = outList.toArray(new TxOut[0]);
		this.lockTime = in.readLEInt();
		this.txHash = Message.getHash(version, this);
	}
	
	public Hash getTxHash() {
		return txHash;
	}
	
	public int getVersion() {
		return version;
	}
	
	public int getInCount() {
		return inArr.length;
	}
	
	public TxIn getInput(int index) {
		if (index < 0 || index >= inArr.length) {
			return null;
		}
		return this.inArr[index];
	}
	
	public TxIn[] getInputs() {
		return Arrays.copyOf(inArr, inArr.length);
	}
	
	public int getOutCount() {
		return outArr.length;
	}
	
	public TxOut getOutput(int index) {
		if (index < 0 || index >= outArr.length) {
			return null;
		}
		return this.outArr[index];
	}
	
	public TxOut[] getOutputs() {
		return Arrays.copyOf(outArr, outArr.length);
	}
	
	public int getLockTime() {
		return lockTime;
	}
	
	@Override
	public void write(int version, EndianDataOutputStream out) throws IOException {
		out.writeLEInt(this.version);
		if (inCount.get() != inArr.length) {
			throw new IllegalStateException("Input count and input array length mismatch");
		}
		this.inCount.write(version, out);
		for (TxIn in : this.inArr) {
			in.write(version, out);
		}
		if (outCount.get() != outArr.length) {
			throw new IllegalStateException("Input count and input array length mismatch");
		}
		this.outCount.write(version, out);
		for (TxOut o : this.outArr) {
			o.write(version, out);
		}
		out.writeLEInt(lockTime);
	}
	
	@Override
	public int hashCode() {
		return txHash.hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		} else if (!(o instanceof Transaction)) {
			return false;
		} else {
			return ((Transaction) o).txHash.equals(txHash);
		}
	}
	
	@Override
	public String toString() {
		return new StringCreator()
			.add("txHash", txHash)
			.add("version", version)
			.add("inputs", inArr)
			.add("outputs", outArr)
			.add("locktime", lockTime)
			.toString();
	}

}