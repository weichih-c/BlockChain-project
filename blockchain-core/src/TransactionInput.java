import java.io.Serializable;
import java.math.BigInteger;

public class TransactionInput implements Serializable{
	private String prev_hash;
	private BigInteger prev_txOut_index;
	private BigInteger scriptLen;
	private byte[] scriptSignature;
	
	public TransactionInput(){
	}
	
	public String getPrev_hash() {
		return prev_hash;
	}
	
	public void setPrev_hash(String prev_hash) {
		this.prev_hash = prev_hash;
	}
	
	public BigInteger getPrev_txOut_index() {
		return prev_txOut_index;
	}
	
	public void setPrev_txOut_index(String hexNumber) {
		// measure if a '0x' character in hexadecimal
		if(hexNumber.indexOf("0x") == -1){
			this.prev_txOut_index = new BigInteger(hexNumber, 16);
		
		}else{
			// use the string after '0x'
			this.prev_txOut_index = new BigInteger(hexNumber.split("0x")[1], 16);
		}
	}
	
	public void setPrev_txOut_index(BigInteger prev_txOut_index) {
		this.prev_txOut_index = prev_txOut_index;
	}
	
	public BigInteger getScriptLen() {
		return scriptLen;
	}
	
	public void setScriptLen(BigInteger scriptLen) {
		this.scriptLen = scriptLen;
	}
	
	public void setScriptLen(String hexNum) {
		this.scriptLen = new BigInteger(hexNum,16);
	}
	
	public byte[] getScriptSignature() {
		return scriptSignature;
	}
	
	public void setScriptSignature(byte[] scriptSignature) {
		this.scriptSignature = scriptSignature;
	}
	
}