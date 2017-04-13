import java.math.BigInteger;

public class TransactionInput {
	public BigInteger prev_hash;
	public int prev_txOut_index;
	public BigInteger scriptLen;
	public byte[] scriptSignature;
	
}