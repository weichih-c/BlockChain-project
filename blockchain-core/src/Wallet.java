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
			keyUtil.keyGenerate(keyName[0], keyName[1]);	// 0 is pubKey, 1 is privKey
			Receiver receiver = new Receiver( Constant.getKeyPath(keyName[0]) );
			publicHashKeyList.add(receiver.getPublicKeyHashAddress());
			
		} catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
			System.out.println(e.getMessage());
		}
		
		this.balance = new BigInteger("0");

	}
	
	public Wallet(String keyName){
		Receiver receiver = new Receiver( Constant.getKeyPath(keyName) );
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
		System.out.println("Receive Money");
		UnspentTXO utxo = new UnspentTXO();
		utxo.setTransactionHash(transaction.getTx_hash());
		
		int index = countIndexInTxOut(transaction);
		
		TransactionOutput txOut = transaction.getTxOutputs().get(index);
		utxo.setIndexOfTxOutput(index);
		utxo.setValue(txOut.getValue());
		System.out.println("Receive " + txOut.getValue());
		utxo.setScriptPubKey(txOut.getScriptPubKey());
		
		utxoList.add(utxo);
	}
	
	// counting which transactionOutput index that the key of this wallet be used in
	private int countIndexInTxOut(Transaction tx){
		ArrayList<TransactionOutput> txOutList = tx.getTxOutputs();
		int index = 0;
		for(int i=0; i < txOutList.size(); i++){
			TransactionOutput txOut = txOutList.get(i);
			String keyInTxOut = keyRemoveOpcodes(txOut.getScriptPubKey());	// removing opcodes in scriptPubKey
			
			// compare which key in OutputList belongs to this wallet
			if(keyInUse.equals(keyInTxOut)){
				index = i;
				System.out.println("it match the " + i + " index in Tx");
				break;
			}
		}
		
		return index;
	}
	
	// sum all utxo value and return balance in 10^-8
	public double getBalance(){
		
		BigInteger balance = this.balance;
		for(UnspentTXO utxo : utxoList){
			balance = balance.add(utxo.getValue());
		}
		return balance.doubleValue() * Math.pow(10, -8);
	}
	
	// remove the first 2 bytes and last 2 bytes, and encoding into HexString
	private String keyRemoveOpcodes(byte[] scriptPubkey){
		byte[] temp = Arrays.copyOfRange(scriptPubkey, 2, scriptPubkey.length-2);
		return Utils.getHexString(temp);
	}
	
}
