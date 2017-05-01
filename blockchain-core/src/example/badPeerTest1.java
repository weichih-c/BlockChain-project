package example;

import java.math.BigInteger;

import core.Block;
import core.DBConnector;
import core.Miner;
import core.Transaction;
import core.TransactionInput;
import core.TransactionOutput;
import core.Wallet;

public class badPeerTest1 {
	public static void main(String[] args){
		DBConnector dbConnector = new DBConnector();
		Miner miner = new Miner();
		
		// 有一個Miner想要偷別人的UTXO，把別人的UTXO偷拿來放進自己帳戶的TransactionInput
		// 預期應該會在miner驗證的時候發生錯誤
		// 原因: 不知道別人的public Key是什麼(公開的是hash過的key)
		
		// create genesis block
		Block genesisBlock = new Block().createGenesisBlock();
		dbConnector.saveBlock(genesisBlock);
		
		// create a block
		Wallet minerWallet = new Wallet("public2", "private2");
		miner.mineBlock(minerWallet);
		
		Wallet thief = new Wallet("public", "private");
		Transaction tx = minerWallet.createGeneralTransaction(2000000000, minerWallet, thief.showPubKeyAddress(0));
		TransactionInput in = tx.getTxInputs().get(0);
		// 把這個transaction的input換成是其他的UTXO
		in.setPrev_hash(genesisBlock.getTransactions().get(0).getTx_hash().toString());
		TransactionOutput out = tx.getTxOutputs().get(1);
		out.setValue(genesisBlock.getTransactions().get(0).getTxOutputs().get(0).getValue());
		out.setScriptPubKey(thief.showPubKeyAddress(0).getBytes());
		out.setScriptLen(new BigInteger(Integer.toHexString(thief.showPubKeyAddress(0).length()), 16));
		
		dbConnector.saveTransaction(tx);
		
		miner.mineBlock(minerWallet);
		
	}
}
