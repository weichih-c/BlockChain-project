package core;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Date;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class Miner {
	
	public static void main(String[] args){
		DBConnector dbConnector = new DBConnector();

		
		Block genesisBlock = new Block().createGenesisBlock();
//		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(genesisBlock.getTime())));
//		dbConnector.saveBlock(genesisBlock);
		
		/// 只有第二個參數是跟前一個block有關，其他的都要重算，有些可以先用常數代替，還要加入Txs
		Block block1 = new Block(1, new Sha256Hash(genesisBlock.getBlockHeaderHash()), genesisBlock.getMerkleRoot()
				, Utils.currentTimeMillis(), genesisBlock.getDifficultyTarget(), genesisBlock.getNonce());
	
		Block bb = new Block();
		System.out.println(Utils.currentTimeSeconds());
		bb.setTime(Utils.currentTimeSeconds());
		bb.setPrevBlockHash(new Sha256Hash(genesisBlock.getBlockHeaderHash()));
		bb.setDifficultyTarget(genesisBlock.getDifficultyTarget());
		
		Wallet minerWallet = new Wallet("public2", "private2");
		Transaction newTx = new Miner().createCoinbaseTx(minerWallet);
		
		dbConnector.saveTransaction(newTx);	// test insert
		
		bb.addTransactionIntoBlock(newTx);
		try {
			bb.setMerkleRoot(calculateMerkleRoot(bb.getTransactions()));
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
		
		byte[] a = dbConnector.getTransaction(1);
		Transaction.deserializeTransaction(a);
//		
//		calculateNonce(bb);	// create a nonce, and next step is push to chain.
//		dbConnector.saveBlock(bb);

//		System.out.println("BlockHash = " + bb.getBlockHeaderHash());
//		System.out.println("Block Version = " + bb.getVersion());
//		System.out.println("Block PrevBlockHash = " + bb.getPrevBlockHash());
//		System.out.println("Block MerkleRoot = " + bb.getMerkleRoot());
//		System.out.println("Block Time = " + bb.getTime());
//		System.out.println("Block Difficulty = " + bb.getDifficultyTarget());
//		System.out.println("Block Nonce = " + bb.getNonce());
//		System.out.println("Block Transaction Size = " + bb.getTransactionSize());
//		Transaction tx = bb.getTransactions().get(0);
//		System.out.println("Transaction 1 Hash = " + tx.getTx_hash().toString());
//		System.out.println("Transaction 1 input size = " + tx.getTxInputsSize());
//		System.out.println("Transaction 1 output size = " + tx.getTxOutputsSize());

//		System.out.println("Wallet Balance of miner = " + minerWallet.getBalance() + "BTC");
//		
		Wallet user = new Wallet();
		Transaction expense = minerWallet.createGeneralTransaction(2050000000, minerWallet, user);
		dbConnector.saveTransaction(expense);
		byte[] b = dbConnector.getTransaction(1);
		Transaction.deserializeTransaction(b);
//		user.receiveMoney( expense );
//		if(expense.isAnyChange()){
//			minerWallet.receiveMoney(expense);
//		}
//		
//		System.out.println("user balance = " + user.getBalance() + "BTC");
//		System.out.println("miner balance = " + minerWallet.getBalance() + "BTC");
//		
//		minerWallet.clearWalletSpentTXO();
//		System.out.println("miner balance = " + minerWallet.getBalance() + "BTC");

		

//		String keyPath2 = Constant.getPubKeyPath("public3");
//		Receiver miner2 = new Receiver(keyPath2);
//		Transaction newTx2 = new Miner().createCoinbaseTx(miner2);
//		System.out.println(newTx2.getTx_hash());
		
		
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
	public ArrayList<Transaction> chooseTransactions(DBConnector dbConnector, int pickupSize){
		ArrayList<Transaction> txList = new ArrayList<>();
		for(int a=0; a<pickupSize; a++){
			byte[] txData = dbConnector.getTransaction(0);
			Transaction tx = Transaction.deserializeTransaction(txData);
			
			// TODO: verify tx (complete functions below)
			verifyTransaction(tx);

			txList.add(tx);
		}
		
		return txList;
	}
	
	public void verifyTransaction(Transaction tx){
		
		// TODO: 驗證完tx之後要幫交易雙方加錢扣錢找錢
		// 		 如果是沒過的tx要改資料庫
		//		 驗證成功就可以去算block了
	}
	
	public boolean equalVerify(String pubHashKey, String pubHashNew){
		return false;
	}
	
	public boolean checkSig(byte[] signature){
		
		return false;
	}
}
