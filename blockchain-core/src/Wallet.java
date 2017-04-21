import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;

public class Wallet {
	private BigInteger balance;
	private ArrayList<String> publicHashKeyList = new ArrayList<>();
	private ArrayList<UnspentTXO> utxoList = new ArrayList<>();
	private String keyInUse;
	private String pubKeyPath;
	private String privKeyPath;
	private String walletName;
	
	public static void main (String [] args){
		Wallet w = new Wallet();
		w.balance = new BigInteger("" +55523400000L);
		System.out.println(w.getBalance() + "BTC");
	}
	
	// if no specific key, will randomly generate a pair of keys
	public Wallet(){
		KeyUtils keyUtil = new KeyUtils();
		RandomString rs = new RandomString(8); // generate a 8 characters string
		String[] keyName = rs.generateKeyPairName();
		
		try {
			this.walletName = keyName[0];
			keyUtil.keyGenerate(keyName[0], keyName[1]);	// 0 is pubKey, 1 is privKey
			this.pubKeyPath = Constant.getKeyPath(keyName[0]);
			this.privKeyPath = Constant.getKeyPath(keyName[1]);
			Receiver receiver = new Receiver( pubKeyPath );
			publicHashKeyList.add(receiver.getPublicKeyHashAddress());
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			System.out.println(e.getMessage());
		}
		
		this.balance = new BigInteger("0");

	}
	
	public Wallet(String pubkeyName, String privkeyNAme){
		this.walletName = pubkeyName;
		this.pubKeyPath = Constant.getKeyPath(pubkeyName);
		this.privKeyPath = Constant.getKeyPath(privkeyNAme);
		Receiver receiver = new Receiver( Constant.getKeyPath(pubkeyName) );
		publicHashKeyList.add(receiver.getPublicKeyHashAddress());
		this.balance = new BigInteger("0");
	}
	
	public String showPubKeyAddress(int numberOfKey){
		String keyAddress = publicHashKeyList.get(numberOfKey);
		this.keyInUse = keyAddress;
		return keyAddress;
	}
	
	/**
	 * This method should be called when a wallet receiving money.
	 * Will add a utxo into list of wallet.
	 * 
	 * @param transaction
	 */
	public void receiveMoney(Transaction transaction){
		UnspentTXO utxo = new UnspentTXO();
		utxo.setTransactionHash(transaction.getTx_hash());
		
		int index = countIndexInTxOut(transaction);
		
		TransactionOutput txOut = transaction.getTxOutputs().get(index);
		utxo.setIndexOfTxOutput(index);
		utxo.setValue(txOut.getValue());
		System.out.println(walletName + " received " + txOut.getValue());
		utxo.setScriptPubKey(txOut.getScriptPubKey());
		
		utxoList.add(utxo);
	}
	
	
	public Transaction createGeneralTransaction(int value, Wallet consumerWallet, Wallet recevierWallet){
//		public Transaction createGeneralTransaction(ArrayList<UnspentTXO> utxoList, double value, Wallet recevierWallet){
		BigInteger spentValue = new BigInteger(Integer.toString(value));
		BigInteger payUTXOAmount = new BigInteger("0");
		ArrayList<UnspentTXO> readyToSpentTXO = new ArrayList<>();
		
		for(UnspentTXO utxo : utxoList){
			// if pay < spentValue
			if(payUTXOAmount.compareTo(spentValue) < 0){
				payUTXOAmount = payUTXOAmount.add(utxo.getValue());
				readyToSpentTXO.add(utxo);
				utxo.setSpent(true); // remove the spent TXO from wallet
//				utxoList.remove(utxo);
				
			}
		}
		
		
		
		Transaction tx = new Transaction();
		TransactionOutput txOut = new TransactionOutput();
		txOut.setValue(spentValue);
		
		// use the first key address.(Simple implementation)
		byte[] scriptPubKey = recevierWallet.showPubKeyAddress(0).getBytes();

    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_HASH160);
    	scriptPubKey = Utils.prependByte(scriptPubKey, OpCode.OP_DUP);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_EQUALVERIFY);
    	scriptPubKey = Utils.appendByte(scriptPubKey, OpCode.OP_CHECKSIG);
    	
    	txOut.setScriptPubKey(scriptPubKey);	// adding the opCodes to scriptPubKey in TXOut
    	txOut.setScriptLen(new BigInteger(Integer.toHexString(scriptPubKey.length), 16));
    	tx.addTxOutput(txOut);
    	
    	System.out.println("pay money = " + payUTXOAmount.toString(10));
    	System.out.println("SpentMoney = " + spentValue.toString(10));
    	
    	// there are changes after paying
    	if(payUTXOAmount.compareTo(spentValue) > 0){
    		TransactionOutput txOut_Change = new TransactionOutput();
    		BigInteger bi = payUTXOAmount.subtract(spentValue);
    		System.out.println("Change = " +bi.toString(10));
    		txOut_Change.setValue(bi);
    		
    		// use the first key address.(Simple implementation)
    		byte[] scriptPubKey_change = consumerWallet.showPubKeyAddress(0).getBytes();

    		scriptPubKey_change = Utils.prependByte(scriptPubKey_change, OpCode.OP_HASH160);
    		scriptPubKey_change = Utils.prependByte(scriptPubKey_change, OpCode.OP_DUP);
    		scriptPubKey_change = Utils.appendByte(scriptPubKey_change, OpCode.OP_EQUALVERIFY);
    		scriptPubKey_change = Utils.appendByte(scriptPubKey_change, OpCode.OP_CHECKSIG);
        	
    		txOut_Change.setScriptPubKey(scriptPubKey_change);	// adding the opCodes to scriptPubKey in TXOut
    		txOut_Change.setScriptLen(new BigInteger(Integer.toHexString(scriptPubKey_change.length), 16));
    		tx.addTxOutput(txOut_Change);
    		tx.setAnyChange(true);
    	}
    	
		for(int a=0; a < readyToSpentTXO.size(); a++){
			TransactionInput txIn = new TransactionInput();
			txIn.setPrev_hash(readyToSpentTXO.get(a).getTransactionHash().toString());
			txIn.setPrev_txOut_index("" + readyToSpentTXO.get(a).getIndexOfTxOutput());
			txIn.setScriptSignature(null);
			txIn.setScriptLen("0");
			tx.addTxInput(txIn);
		}
		
		
		Consumer consumer = new Consumer(pubKeyPath, privKeyPath);
		try {
//			System.out.println("txIn scriptSig before makeSS = " + Utils.getHexString( tx.getTxInputs().get(0).getScriptSignature()));
			ArrayList<TransactionInput> txIns = consumer.makeScriptSignature(tx, utxoList);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
			System.out.println(e.getMessage());
		}
		// TODO: 理論上過完makeFunction, 原本的tx裡的Inputs應該也會被改到, 用print驗證看看
		
		return tx;
	}
	
	
	
	// counting which transactionOutput index that the key of this wallet be used in
	private int countIndexInTxOut(Transaction tx){
		ArrayList<TransactionOutput> txOutList = tx.getTxOutputs();
		int index = 0;
		for(int i=0; i < txOutList.size(); i++){
			TransactionOutput txOut = txOutList.get(i);
			String keyInTxOut = keyRemoveOpcodes(txOut.getScriptPubKey());	// removing opcodes in scriptPubKey
			
			/* For compare purpose:
			 * this is because original keyInUse is in Base58 encoding,
			 * so has be re encoding in hex format
			 * 
			 * it is a bad implementation, but just a workaround.
			 */
			String keyInUse = Utils.getHexString(this.keyInUse.getBytes());	
			
			// compare which key in OutputList belongs to this wallet
			if(keyInUse.equals(keyInTxOut)){
				index = i;
//				System.out.println("it match the " + i + " index in Tx");
				break;
			}
		}
		
		return index;
	}
	
	// sum all utxo value and return balance in 10^-8
	public double getBalance(){
		
		BigInteger balance = this.balance;
		for(UnspentTXO utxo : utxoList){
//			System.out.println(i + "- value = " + utxo.getValue());
//			System.out.println(i + "- isSpent? = " + utxo.isSpent());

			if(! utxo.isSpent()){
				balance = balance.add(utxo.getValue());
			}
		}
		System.out.println("utxoList records = " + utxoList.size());
		
		return balance.doubleValue() * Math.pow(10, -8);
	}
	
	public void clearWalletSpentTXO(){
		for(UnspentTXO utxo : utxoList){
			if(utxo.isSpent())
				utxoList.remove(utxo);
		}
	}
	
	// remove the first 2 bytes and last 2 bytes, and encoding into HexString
	private String keyRemoveOpcodes(byte[] scriptPubkey){
		byte[] temp = Arrays.copyOfRange(scriptPubkey, 2, scriptPubkey.length-2);
		return Utils.getHexString(temp);
	}
	
}
