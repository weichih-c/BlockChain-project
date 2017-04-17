import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Random;

import org.spongycastle.util.encoders.Hex;

public class Miner {
	public static void main(String[] args){
		Block genesisBlock = new Block().createGenesisBlock();
		
		
//		/// 只有第二個參數是跟前一個block有關，其他的都要重算，有些可以先用常數代替，還要加入Txs
//		Block block1 = new Block(1, new Sha256Hash(genesisBlock.getBlockHeaderHash()), genesisBlock.getMerkleRoot()
//				, Utils.currentTimeMillis(), genesisBlock.getDifficultyTarget(), genesisBlock.getNonce());
	
		Block bb = new Block();
		bb.setTime(Utils.currentTimeSeconds());
		bb.setPrevBlockHash(new Sha256Hash(genesisBlock.getBlockHeaderHash()));
		bb.setDifficultyTarget(genesisBlock.getDifficultyTarget());
		
		String keyPath = Constant.getPubKeyPath("public2");
		Receiver miner1 = new Receiver(keyPath);
		Transaction newTx = new Miner().createCoinbaseTx(miner1);
		
		bb.addTransactionIntoBlock(newTx);
		try {
			bb.setMerkleRoot(calculateMerkleRoot(bb.getTransactions()));
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		System.out.println("Nonce = " + calculateNonce(bb) );
		System.out.println("Hi = " + bb.getNonce());
		
		
		
//		SecureRandom r = new SecureRandom();
//		byte[] randByte = new byte[8];
//		r.nextBytes(randByte);
//		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
//	    buffer.put(randByte);
//	    buffer.flip();//need flip 
//		bb.setNonce(buffer.getLong());
//		
//		System.out.println(bb.getBlockHeaderHash());
		
		
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
	
	public static long calculateNonce(Block block){
		while(true){
			SecureRandom r = new SecureRandom();
			int nonce;
			int a = r.nextInt(2147483647);
			nonce = byteArrayToInt( Utils.getIntByteArray(a) );
			
//			byte[] randByte = new byte[4];
//			r.nextBytes(randByte);
//			
//			ByteBuffer buffer = ByteBuffer.allocate(4);
//		    buffer.put(randByte);
//		    buffer.flip();//need flip 
//		    long nonce = buffer.getInt();
		    block.setNonce(nonce);
			
			if(block.getBlockHeaderHash().startsWith("000")){
				return nonce;
			}
		}
	}
	
	private  Transaction createCoinbaseTx (Receiver miner) {
		Transaction coinbaseTx = new Transaction();
    	TransactionInput txIn = new TransactionInput();
    	txIn.setPrev_hash("0000000000000000000000000000000000000000000000000000000000000000");
    	txIn.setPrev_txOut_index("ffffffff");
    	String message = "No bugs in this program";
    	
    	byte[] coinbaseScript = Utils.hexStringToByteArray("04FFFF001D010445" + Utils.HEX.encode(message.getBytes()));
    	
    	txIn.setScriptSignature(coinbaseScript);
    	txIn.setScriptLen(new BigInteger(Integer.toHexString(coinbaseScript.length), 16));
    	
    	TransactionOutput txOut = new TransactionOutput();
    	txOut.setValue(5000000000L);
    	
    	byte[] scriptPubKey = miner.getPublicKeyHashAddress().getBytes();
    	
    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_HASH160);
    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_DUP);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_EQUALVERIFY);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_CHECKSIG);

    	txOut.setScriptPubKey(scriptPubKey);
    	txOut.setScriptLen(new BigInteger(Integer.toHexString(scriptPubKey.length), 16));

    	coinbaseTx.setVersion(1);
    	coinbaseTx.addTxInput(txIn);
    	coinbaseTx.addTxOutput(txOut);
    	
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
}
