package core;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;

import org.spongycastle.util.Arrays;

public class Miner {
	DBConnector dbConnector;
	
	public Miner(){
		dbConnector = new DBConnector();
	}

	
	public static void main(String[] args){
		DBConnector dbConnector = new DBConnector();
		Miner miner = new Miner();
		
		Block genesisBlock = new Block().createGenesisBlock();
		dbConnector.saveBlock(genesisBlock);


		Wallet minerWallet = new Wallet("public2", "private2");
		miner.mineBlock(minerWallet);
		
		Wallet user = new Wallet();
		Transaction expense = minerWallet.createGeneralTransaction(2050000000, minerWallet, user.showPubKeyAddress(0));
		dbConnector.saveTransaction(expense);
		
		miner.mineBlock(minerWallet);
		
		
		user.receiveMoney(expense);
		if(expense.isAnyChange()){
			minerWallet.receiveMoney(expense);
		}
		
		Wallet user2 = new Wallet();
		Transaction ex2 = user.createGeneralTransaction(1050000000, user, user2.showPubKeyAddress(0));
		dbConnector.saveTransaction(ex2);
		user2.receiveMoney(ex2);
		if(ex2.isAnyChange()){
			user.receiveMoney(ex2);
		}

		miner.mineBlock(minerWallet);
		
	}
	
	public void mineBlock(Wallet minerWallet){
		Block lastBlock = dbConnector.getLastBlock();
		Block b = new Block();
		b.setTime(System.currentTimeMillis()/1000);
		b.setPrevBlockHash(new Sha256Hash(lastBlock.getBlockHashWithoutCalculate()));
		b.setDifficultyTarget(lastBlock.getDifficultyTarget());
		
		Transaction coinbaseTx = this.createCoinbaseTx(minerWallet);
		
		dbConnector.saveTransaction(coinbaseTx);	// test insert
		dbConnector.setTransactionVerified(coinbaseTx.getTx_hash().toString(), 1); // set coinbaseTx verified
		b.addTransactionIntoBlock(coinbaseTx);
		
		ArrayList<Transaction> txList = this.pickupTransactionsFromPool(dbConnector, 1);
		System.out.println("txlist size = " + txList.size());
		for(Transaction tx : txList){
			System.out.println("tx = " + tx.getTx_hash().toString());
			b.addTransactionIntoBlock(tx);
		}

		try {
			b.setMerkleRoot(calculateMerkleRoot(b.getTransactions()));
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
		calculateNonce(b);	// create a nonce, and next step is push to chain.
		dbConnector.saveBlock(b);
		
	}
	
	public static int byteArrayToInt(byte[] b) 
	{
	    return   b[3] & 0xFF |
	            (b[2] & 0xFF) << 8 |
	            (b[1] & 0xFF) << 16 |
	            (b[0] & 0xFF) << 24;
	}
	
	public static int calculateNonce(Block block){
		while(true){
			SecureRandom r = new SecureRandom();
			int nonce;
			int a = r.nextInt(2147483647);
			nonce = byteArrayToInt( Utils.getIntByteArray(a) );
		
		    block.setNonce(nonce);
		    String hash = block.getBlockHeaderHash();
//			System.out.println("Calculating...Hash = " + hash);

			if(hash.startsWith("000")){
				return nonce;
			}
		}
	}
	
	private  Transaction createCoinbaseTx (Wallet minerWallet) {
		Transaction coinbaseTx = new Transaction();
    	TransactionInput txIn = new TransactionInput();
    	txIn.setPrev_hash("0000000000000000000000000000000000000000000000000000000000000000");
    	txIn.setPrev_txOut_index("ffffffff");
    	String message = "No bugs in this program";
    	
    	byte[] coinbaseScript = Utils.hexStringToByteArray("04FFFF001D010445" + Utils.HEX.encode(message.getBytes()));
    	
    	txIn.setScriptSignature(coinbaseScript);
    	txIn.setScriptLen(new BigInteger(Integer.toHexString(coinbaseScript.length), 16));
    	
    	TransactionOutput txOut = new TransactionOutput();
    	txOut.setValue(new BigInteger("5000000000"));
    	byte[] scriptPubKey = minerWallet.showPubKeyAddress(0).getBytes();
    	
    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_HASH160);
    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_DUP);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_EQUALVERIFY);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_CHECKSIG);
    	
    	txOut.setScriptPubKey(scriptPubKey);	// adding the opCodes to scriptPubKey in TXOut
    	txOut.setScriptLen(new BigInteger(Integer.toHexString(scriptPubKey.length), 16));

    	coinbaseTx.setVersion(1);
    	coinbaseTx.addTxInput(txIn);
    	coinbaseTx.addTxOutput(txOut);
    	
    	minerWallet.receiveMoney(coinbaseTx);
    	
		return coinbaseTx;
	}
	
	private static byte[] reverseEndian (byte[]b){
		return Utils.reverseBytes(b);
	}
	
	public static Sha256Hash calculateMerkleRoot(ArrayList<Transaction> txList) throws Exception{
		if(txList.size()<1){
			throw new Exception("calculate should after gathering transactions.");
		}
		
		if(txList.size() == 1){
			return txList.get(0).getTx_hash();
		}else{
			byte[] tempArr = {};
			for(Transaction tx : txList){
				tempArr = Utils.concatenateByteArrays(tempArr, reverseEndian(tx.getTx_hash().getBytes()));
			}
			
			return new Sha256Hash( reverseEndian( HashGenerator.hashingSHA256Twice(tempArr) ) );
		}
	}
	
	// 從pool中選一些transaction用來算merkleRoot
	public ArrayList<Transaction> pickupTransactionsFromPool(DBConnector dbConnector, int pickupSize){
		ArrayList<Transaction> txList = new ArrayList<>();
		for(int a=0; a<pickupSize; a++){
			byte[] txData = dbConnector.getTransactionsForVerify(0);
			if(txData.length == 0){
				return txList;	// return empty list
			}
			Transaction tx = Transaction.deserializeTransaction(txData);
			
			if(! verifyTransaction(dbConnector, tx)){
				continue;
			}

			txList.add(tx);
		}
		
		return txList;
	}
	
	public Transaction findPrevTransaction(DBConnector dbConnector, String txHash){
		byte[] txData = dbConnector.getTransaction(txHash);
		Transaction tx = Transaction.deserializeTransaction(txData);
		return tx;
		
	}
	
	
	// verify a transaction, if any unpassed condition, then return false.
	public boolean verifyTransaction(DBConnector dbConnector, Transaction tx){
		String transactionHash = tx.getTx_hash().toString();
		ArrayList<TransactionInput> tIns = tx.getTxInputs();
		
		int inputIndex = 0;
		for(TransactionInput input : tIns){
			String prevHash = input.getPrev_hash();
			int prevOutputIndex = input.getPrev_txOut_index().intValue();
			Transaction prevTx = findPrevTransaction(dbConnector, prevHash);
			byte[] prevScriptPubkey = prevTx.getTxOutputs().get(prevOutputIndex).getScriptPubKey();
			String prevPubkeyHash = getPrevPubkeyHash(prevScriptPubkey);
			byte[] scriptSig = input.getScriptSignature();
			int signatureLen = Utils.byteArrayToInt(Arrays.copyOfRange(scriptSig, 0, 4));	// first 4 bytes are len integer
			byte[] signature = Arrays.copyOfRange(scriptSig, 4, 4+signatureLen);
			byte[] pubkey = Arrays.copyOfRange(scriptSig, 4+signatureLen, scriptSig.length);
			
			// create the consumer object for getting the hashTx by the methods
			Consumer consumer = new Consumer();
			byte[] txData = consumer.modifyTransaction(tx, 
									inputIndex, 
									consumer.removeOpcodesFromScriptPubKey(prevScriptPubkey));

			byte[] sigOriginData = consumer.hashModifiedTransactions(txData);
			
					
			if(! equalVerify(prevPubkeyHash, getProcessedPubkeyHash(pubkey) )){
				System.out.println("Verify Error: equalVerify");
				dbConnector.setTransactionVerified(transactionHash, 0);
				return false;
			}
			
			if(! checkSig(pubkey, signature, sigOriginData)){
				System.out.println("Verify Error: checkSig");
				dbConnector.setTransactionVerified(transactionHash, 0);
				return false;
			}
			
			System.out.println("verify success : " + transactionHash);
			dbConnector.setTransactionVerified(transactionHash, 1);
			

		}
		
		return true;
		
	}
	
	public boolean equalVerify(String pubHashKey, String pubHashNew){
//		System.out.println(pubHashNew);
//		System.out.println(pubHashKey);
		return pubHashNew.equals(pubHashKey);
	}
	
	public boolean checkSig(byte[] pubkey, byte[] signature, byte[] originSigData){
		try {
			PublicKey publicKey = 
				    KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(pubkey));
			
			Signature sig = Signature.getInstance("SHA256withECDSA");
			sig.initVerify(publicKey);
			sig.update(originSigData);
			return sig.verify(signature);
			
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException e) {
			e.printStackTrace();
			return false;
		} catch (SignatureException e) {
			e.printStackTrace();
			return false;
		}

	}
	
	private String getPrevPubkeyHash(byte[] scriptPubKey){
		byte[] pubkeyHash = Arrays.copyOfRange(scriptPubKey, 2, scriptPubKey.length-2);
		return Utils.getHexString(pubkeyHash);
	}
	
	private String getProcessedPubkeyHash(byte[] pubkey){
		String base58Pubkey = getPublicHashKey(pubkey);
		return Utils.getHexString(base58Pubkey.getBytes());
	}
	
	private String getPublicHashKey(byte[] pubkey){
		// get public key from keyStore
		PublicKey publicKey = null;
		try {
			publicKey = KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(pubkey));
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// generate a pubKeyHash
		byte[] pubKeyHash = HashGenerator.hashingRIPEMD160(HashGenerator.hashingSHA256(publicKey.getEncoded()));
		
		byte[] pkhPrefix = { 0x00 };	// prefix 1 byte (0x00)
		byte[] pkhChecksum = HashGenerator.hashingSHA256Twice(pubKeyHash);	// truncate first 4 bytes
		pkhChecksum = Arrays.copyOfRange(pkhChecksum, 0, 4);
		
		
		// concatenate arrays including prefix, pubKeyHash, checksum
		byte[] p2pkhAddress = Utils.concatenateByteArrays(pkhPrefix, pubKeyHash);
		p2pkhAddress = Utils.concatenateByteArrays(p2pkhAddress, pkhChecksum);

		return Base58.encode( p2pkhAddress );	// return a string encoded by Base58

	}
}
