import java.io.Serializable;
import java.math.BigInteger;

public class TransactionOutput implements Serializable{
	private long value;
	private BigInteger scriptLen;
	private byte[] scriptPubKey;
	public long getValue() {
		return value;
	}
	public void setValue(long value) {
		this.value = value;
	}
	public BigInteger getScriptLen() {
		return scriptLen;
	}
	public void setScriptLen(BigInteger scriptLen) {
		this.scriptLen = scriptLen;
	}
	public byte[] getScriptPubKey() {
		return scriptPubKey;
	}
	public void setScriptPubKey(byte[] scriptPubKey) {
		this.scriptPubKey = scriptPubKey;
	}
}