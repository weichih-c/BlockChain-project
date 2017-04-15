import java.math.BigInteger;
import java.util.*;

import org.spongycastle.util.encoders.Hex;

public class Block {
	
	// Fields defined as part of the protocol format.
    private long version;
    private Sha256Hash prevBlockHash;
    private Sha256Hash merkleRoot;
    private long time;
    private long difficultyTarget; // "nBits"
    private long nonce;
    
    ArrayList<Transaction> transactions;
    private Sha256Hash hash;    // Stores the hash of the block.

    public Block(){
    	this.version = 00000001L;
    	this.transactions = new ArrayList<>();
    }
    
    public static void main(String[]args) {
//    	System.out.println("str = " +  new String( Hex.encode("The Times 03/Jan/2009 Chancellor on brink of second bailout for banks".getBytes())));
//    	System.out.println("hex to int = " + new BigInteger("486604799", 16));
//    	System.out.println("long to hex = " + Long.toHexString(486604799L));
//    	System.out.println("hex = " + new BigInteger(1, Hex.decode("4d")).toString(16));
//    	
//    	
//    	System.out.println(Utils.hexStringToByteArray("FFFF001D").length);
//    	System.out.println( "FFFF001D".getBytes().length);
//    	
//    	byte[] coinbaseScript = Utils.hexStringToByteArray("04FFFF001D010445"+
//                "5468652054696D65732030332F4A616E2F32303039204368616E63656C6C6F72206F6E2062" +
//                "72696E6B206F66207365636F6E64206261696C6F757420666F722062616E6B73");
//    	System.out.println(Integer.toHexString(coinbaseScript.length));
//    	System.out.println(new BigInteger(Integer.toHexString(coinbaseScript.length),16));
//    	
//    	System.out.println("xxx : " + Long.toHexString(5000000000L));
//    	
//    	Receiver chainCreator = new Receiver();
//    	byte[] scriptPubKey = chainCreator.getPublicKeyHashAddress().getBytes();
//    	
//    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_HASH160);
//    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_DUP);
//    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_EQUALVERIFY);
//    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_CHECKSIG);
//    	
//    	System.out.println("scriptPubKey = " + getHexString(scriptPubKey));
    	
    	Block b = new Block().createGenesisBlock();
    	System.out.println("version = " + b.getVersion());
    	System.out.println("PrevBlockHash = " + b.getPrevBlockHash().toString());
    	System.out.println("MerkleRoot = " + b.getMerkleRoot().toString());
    	System.out.println("TimeStamp = " + b.getTime());
    	System.out.println("nBits = " + b.getDifficultyTarget());
    	System.out.println("Nonce = " + b.getNonce());
    	System.out.println("Transaction sizes = " + b.getTransactionSize());
    	System.out.println("Tx1-Version = " + b.getTransactions().get(0).getVersion());
    	System.out.println("Tx1-Input numbers = " + b.getTransactions().get(0).getTxInputsSize());
    	System.out.println("Tx1-Vin1-PrevOutput = " + b.getTransactions().get(0).getTxInputs().get(0).getPrev_hash().toString());
    	System.out.println("Tx1-Vin1-ScriptLen = " + b.getTransactions().get(0).getTxInputs().get(0).getScriptLen());
    	System.out.println("Tx1-Vin1-ScriptSig = " + getHexString( b.getTransactions().get(0).getTxInputs().get(0).getScriptSignature()) );
    	System.out.println("Tx1-Output numbers = " + b.getTransactions().get(0).getTxOutputsSize());
    	System.out.println("Tx1-Vout1-Value = " + b.getTransactions().get(0).getTxOutputs().get(0).getValue());
    	System.out.println("Tx1-Vout1-pk_ScriptLen = " + b.getTransactions().get(0).getTxOutputs().get(0).getScriptLen());
    	System.out.println("Tx1-Vout1-pk_Script = " + getHexString( b.getTransactions().get(0).getTxOutputs().get(0).getScriptPubKey()) );

    }
    
 // encoding the byte[] to hex string
 	private static String getHexString(byte[] b) {
 		String result = "";
 		for (int i = 0; i < b.length; i++) {
 			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
 		}
 		return result;
 	}
    
    public Block(long version, Sha256Hash prevBlockHash, Sha256Hash merkleRoot, long time, long difficultyTarget, long nonce){
    	this.version = version;
    	this.prevBlockHash = prevBlockHash;
    	this.setMerkleRoot(merkleRoot);
    	this.setTime(time);
    	this.setDifficultyTarget(difficultyTarget);
    	this.setNonce(nonce);
    }
    
    
    public Block createGenesisBlock(){
    	final long version = 00000001L;
    	final Sha256Hash prevBlockHash = new Sha256Hash("0000000000000000000000000000000000000000000000000000000000000000");
    	final Sha256Hash merkleRoot = new Sha256Hash("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
    	final long time = 1230977705L;
    	final long difficultyTarget = 486604799L;
    	final long nonce = 2083236893L;
    	
    	Block genesisBlock = new Block();
    	genesisBlock.setVersion(version);
    	genesisBlock.setPrevBlockHash(prevBlockHash);
    	genesisBlock.setMerkleRoot(merkleRoot);
    	genesisBlock.setTime(time);
    	genesisBlock.setDifficultyTarget(difficultyTarget);
    	genesisBlock.setNonce(nonce);
    	
    	Transaction coinbaseTx = new Transaction();
    	genesisBlock.addTransactionIntoBlock(coinbaseTx);
    	TransactionInput txIn = new TransactionInput();
    	txIn.setPrev_hash(new Sha256Hash("0000000000000000000000000000000000000000000000000000000000000000"));
    	txIn.setPrev_txOut_index("ffffffff");
    	
    	byte[] coinbaseScript = Utils.hexStringToByteArray("04FFFF001D010445"+
              "5468652054696D65732030332F4A616E2F32303039204368616E63656C6C6F72206F6E2062" +
              "72696E6B206F66207365636F6E64206261696C6F757420666F722062616E6B73");
    	
    	txIn.setScriptSignature(coinbaseScript);
    	txIn.setScriptLen(new BigInteger(Integer.toHexString(coinbaseScript.length), 16));
    	
    	TransactionOutput txOut = new TransactionOutput();
    	txOut.setValue(5000000000L);
    	Receiver chainCreator = new Receiver();
    	byte[] scriptPubKey = chainCreator.getPublicKeyHashAddress().getBytes();
    	
    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_HASH160);
    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_DUP);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_EQUALVERIFY);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_CHECKSIG);

    	txOut.setScriptPubKey(scriptPubKey);
    	txOut.setScriptLen(new BigInteger(Integer.toHexString(scriptPubKey.length), 16));

    	coinbaseTx.setVersion(1);
    	coinbaseTx.addTxInput(txIn);
    	coinbaseTx.addTxOutput(txOut);
    	
    	return genesisBlock;
    }

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Sha256Hash getPrevBlockHash() {
		return prevBlockHash;
	}

	public void setPrevBlockHash(Sha256Hash prevBlockHash) {
		this.prevBlockHash = prevBlockHash;
	}

	public Sha256Hash getMerkleRoot() {
		return merkleRoot;
	}

	public void setMerkleRoot(Sha256Hash merkleRoot) {
		this.merkleRoot = merkleRoot;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getDifficultyTarget() {
		return difficultyTarget;
	}

	public void setDifficultyTarget(long difficultyTarget) {
		this.difficultyTarget = difficultyTarget;
	}

	public long getNonce() {
		return nonce;
	}

	public void setNonce(long nonce) {
		this.nonce = nonce;
	}
	
	public int getTransactionSize() {
		return transactions.size();
	}
    
	public void addTransactionIntoBlock(Transaction tx){
		this.transactions.add(tx);
	}
	
	public ArrayList<Transaction> getTransactions(){
		return transactions;
	}
}
