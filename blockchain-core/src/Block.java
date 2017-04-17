import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.*;

import org.spongycastle.util.encoders.Hex;

import com.google.common.primitives.Longs;

public class Block {
	
	// Fields defined as part of the protocol format.
    private int version;
    private Sha256Hash prevBlockHash;
    private Sha256Hash merkleRoot;
    private long time;
    private long difficultyTarget; // "nBits"
    private int nonce;
    
    ArrayList<Transaction> transactions;
    private Sha256Hash hash;    // Stores the hash of the block.

    public Block(){
    	this.version = 1;
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
//    	System.out.println("version = " + b.getVersion());
//    	System.out.println("PrevBlockHash = " + b.getPrevBlockHash().toString());
//    	System.out.println("MerkleRoot = " + b.getMerkleRoot().toString());
//    	System.out.println("TimeStamp = " + b.getTime());
//    	System.out.println("nBits = " + b.getDifficultyTarget());
//    	System.out.println("Nonce = " + b.getNonce());
//    	System.out.println("Transaction sizes = " + b.getTransactionSize());
//    	System.out.println("Tx1-Version = " + b.getTransactions().get(0).getVersion());
//    	System.out.println("Tx1-Input numbers = " + b.getTransactions().get(0).getTxInputsSize());
//    	System.out.println("Tx1-Vin1-PrevOutput = " + b.getTransactions().get(0).getTxInputs().get(0).getPrev_hash().toString());
//    	System.out.println("Tx1-Vin1-ScriptLen = " + b.getTransactions().get(0).getTxInputs().get(0).getScriptLen());
//    	System.out.println("Tx1-Vin1-ScriptSig = " + getHexString( b.getTransactions().get(0).getTxInputs().get(0).getScriptSignature()) );
//    	System.out.println("Tx1-Output numbers = " + b.getTransactions().get(0).getTxOutputsSize());
//    	System.out.println("Tx1-Vout1-Value = " + b.getTransactions().get(0).getTxOutputs().get(0).getValue());
//    	System.out.println("Tx1-Vout1-pk_ScriptLen = " + b.getTransactions().get(0).getTxOutputs().get(0).getScriptLen());
//    	System.out.println("Tx1-Vout1-pk_Script = " + getHexString( b.getTransactions().get(0).getTxOutputs().get(0).getScriptPubKey()) );
		
    	System.out.println( b.getBlockHeaderHash() );
    }
    
 // encoding the byte[] to hex string
 	private static String getHexString(byte[] b) {
 		String result = "";
 		for (int i = 0; i < b.length; i++) {
 			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
 		}
 		return result;
 	}
    
    public Block(int version, Sha256Hash prevBlockHash, Sha256Hash merkleRoot, long time, long difficultyTarget, int nonce){
    	this.version = version;
    	this.prevBlockHash = prevBlockHash;
    	this.setMerkleRoot(merkleRoot);
    	this.setTime(time);
    	this.setDifficultyTarget(difficultyTarget);
    	this.setNonce(nonce);
    }
    
    
    public Block createGenesisBlock(){
    	final int version = 1;
    	final Sha256Hash prevBlockHash = new Sha256Hash("0000000000000000000000000000000000000000000000000000000000000000");
    	final Sha256Hash merkleRoot = new Sha256Hash("4a5e1e4baab89f3a32518a88c31bc87f618f76673e2cc77ab2127b7afdeda33b");
    	final long time = 1231006505L;
    	final long difficultyTarget = 486604799L;
    	final int nonce = 2083236893;
    	
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
    	txIn.setPrev_hash("0000000000000000000000000000000000000000000000000000000000000000");
    	txIn.setPrev_txOut_index("ffffffff");
    	
    	byte[] coinbaseScript = Utils.hexStringToByteArray("04FFFF001D010445"+
              "5468652054696D65732030332F4A616E2F32303039204368616E63656C6C6F72206F6E2062" +
              "72696E6B206F66207365636F6E64206261696C6F757420666F722062616E6B73");
    	
    	txIn.setScriptSignature(coinbaseScript);
    	txIn.setScriptLen(new BigInteger(Integer.toHexString(coinbaseScript.length), 16));
    	
    	TransactionOutput txOut = new TransactionOutput();
    	txOut.setValue(5000000000L);
    	String keyStorePath = Constant.getPubKeyPath("public.key");
    	Receiver chainCreator = new Receiver(keyStorePath);
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

	public int getVersion() {
		return version;
	}
	
	public String getVersionInString(){
		return Long.toString(version, 16);
	}

	public void setVersion(int version) {
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

	public int getNonce() {
		return nonce;
	}

	public void setNonce(int nonce) {
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
	

	private void setBlockHash(Sha256Hash hash) {
		this.hash = hash;
	}
	
	
	/**
	 * hash the block header, and return a hex String
	 * 
	 * @return
	 */
	public String getBlockHeaderHash(){
		byte[] blockHeader;
		
		byte[] version = reverseEndian(Utils.getIntByteArray(this.version));
//		System.out.println(getHexString(version));
		blockHeader = Arrays.copyOf(version, version.length);
//		System.out.println(getHexString(this.prevBlockHash.getBytes()));
		blockHeader = Utils.concatenateByteArrays(blockHeader, reverseEndian(this.prevBlockHash.getBytes()));
//		System.out.println(getHexString(this.merkleRoot.getBytes()));
		blockHeader = Utils.concatenateByteArrays(blockHeader, reverseEndian(this.merkleRoot.getBytes()));
		byte[] time = reverseEndian(Utils.getHexEncodeByteArray(this.time));
//		System.out.println(getHexString(time));
		blockHeader = Utils.concatenateByteArrays(blockHeader, time);
		byte[] nBits = reverseEndian(Utils.getHexEncodeByteArray(this.difficultyTarget));
//		System.out.println(getHexString(nBits));
		blockHeader = Utils.concatenateByteArrays(blockHeader, nBits);
		byte[] nonce = reverseEndian(Utils.getIntByteArray(this.nonce));
		System.out.println("Nonce = " + getHexString(nonce));
		blockHeader = Utils.concatenateByteArrays(blockHeader, nonce);
		byte[] blockHash = reverseEndian(HashGenerator.hashingSHA256Twice(blockHeader));
		
		System.out.println("Hash = " + getHexString(blockHash));
		return getHexString(blockHash);
	}

	/**
	 * turn big endian to little endian
	 * @param b
	 * @return
	 */
	private byte[] reverseEndian (byte[]b){
		return Utils.reverseBytes(b);
	}
	
}
