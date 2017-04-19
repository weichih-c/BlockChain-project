import java.math.BigInteger;

public class UnspentTXO {
	private Sha256Hash transactionHash;
	private int indexOfTxOutput;
	private BigInteger value;
	private byte[] scriptPubKey;
	private boolean isSpent = false;
	
	public Sha256Hash getTransactionHash() {
		return transactionHash;
	}
	
	public void setTransactionHash(Sha256Hash transactionHash) {
		this.transactionHash = transactionHash;
	}
	
	public int getIndexOfTxOutput() {
		return indexOfTxOutput;
	}

	public void setIndexOfTxOutput(int indexOfTxOutput) {
		this.indexOfTxOutput = indexOfTxOutput;
	}

	public BigInteger getValue() {
		return value;
	}
	
	public void setValue(BigInteger value) {
		this.value = value;
	}
	
	public byte[] getScriptPubKey() {
		return scriptPubKey;
	}
	
	public void setScriptPubKey(byte[] scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}

	public boolean isSpent() {
		return isSpent;
	}

	public void setSpent(boolean isSpent) {
		this.isSpent = isSpent;
	}
	
}
