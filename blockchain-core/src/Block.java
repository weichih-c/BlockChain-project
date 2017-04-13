import java.util.*;

public class Block {
	
	// Fields defined as part of the protocol format.
    private long version;
    private Sha256Hash prevBlockHash;
    private Sha256Hash merkleRoot;
    private long time;
    private long difficultyTarget; // "nBits"
    private long nonce;
    
    List<Transaction> transactions;
    private Sha256Hash hash;    // Stores the hash of the block.

    public Block(){
    	
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
    
}
