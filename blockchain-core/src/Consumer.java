import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Consumer {
	private String algorithmName = "EC";
	private String PubKeyPath;
	private String PrivKeyPath;

	
	public static void main(String[] args){

	}
	
	public Consumer(String PubKeyPath, String PrivKeyPath){
		this.PubKeyPath = PubKeyPath;
		this.PrivKeyPath = PrivKeyPath;
	}
	
	
	public Transaction createGeneralTransaction(TransactionOutput txOfUTXO, Receiver receiver, long spentValue){
		Transaction transaction = new Transaction();
		// TODO: add a wallet to save UTXO, consumer will spent UTXO as TransactionInput.
		// TODO: spent dollar => receiver[0] get spentValue, receiver[1] get change
		
//		TransactionInput tIn = new TransactionInput();
//		tIn.setPrev_hash(prev_hash);
//		transaction.addTxInput(tIn);
		
		byte[] scriptPubKey = receiver.getPublicKeyHashAddress().getBytes();
		
		return null;
	}
	
	
	public byte[] getPublicKeyEncoded(String publicKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		KeyGenerator keyGen = new KeyGenerator();
		PublicKey pubKey = keyGen.loadPublicKey(publicKeyPath, algorithmName);
		return pubKey.getEncoded();
	}
	
	
	/**
	 * make ScriptSig in every Transaction Input of the specific Transaction.
	 * In this function, a user who wants to spend his UTXO must generate the scriptSig by the p2pKH in UTXOs and concatenate the pubKey in the end.
	 * 
	 * @param pubKeyPath
	 * @param privateKeyPath
	 * @param transaction
	 * @param utxoList
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public void makeScriptSignature(String pubKeyPath, String privateKeyPath, Transaction transaction, ArrayList<TransactionOutput> utxoList) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException{
		byte[] scriptSig = {};
		
		KeyGenerator keyGen = new KeyGenerator();
		PrivateKey privKey = keyGen.loadPrivateKey(privateKeyPath, algorithmName);
		byte[] pubKey = getPublicKeyEncoded(pubKeyPath);
		
		ArrayList<TransactionInput> txIns = transaction.getTxInputs();
		
		// generate the signature, concatenate the the pubKey 
		// and reset the scriptSig field in every TxInput.
		
		for(int index=0; index < txIns.size(); index++){
			byte[] txData = hashModifiedTransactions(transaction, index, utxoList.get(index) );
			byte[] signature = keyGen.makeSignature(privKey, txData);	// make the signature using private key
			scriptSig = Utils.concatenateByteArrays(signature, pubKey);
			
			txIns.get(index).setScriptSignature(scriptSig);
			txIns.get(index).setScriptLen("" + scriptSig.length);
		}
	}
	
	/**
	 * Hashing the Modified Transaction Data without ScriptSig Field in TransactionInputs.
	 * This function is used to make the Signature Field in TransactionInput.
	 * 
	 * The transaction data have to include all data field with the exception of
	 * the scriptSigLen field and scriptSig field in all TransactionInputs. 
	 * (That is, other fields have to be filled)
	 * 
	 * @param tx  Transaction to be modified and hashed
	 * @param txInputIndex which txInput ScriptSig generating
	 * @param uTXO which UTXO is spent in this txInput
	 * 
	 * @return
	 */
	public byte[] hashModifiedTransactions(Transaction tx, int txInputIndex, TransactionOutput uTXO){
		return HashGenerator.hashingSHA256Twice( modifyTransaction(tx, txInputIndex, uTXO) );

	}
	
	/** calculate the modifiedTransaction and transform to byte[]
	 *  set the scriptLen and ScriptSignature fields in all transactionInput to 0 and null respectively
	 * 
	 * @param tx
	 * @param txInputIndex which txInput ScriptSig generating
	 * @param uTXO which UTXO is spent in this txInput
	 * 
	 * @return
	 */
	public byte[] modifyTransaction(Transaction tx, int txInputIndex, TransactionOutput uTXO){
		byte[] serializedTx;
		byte[] version = Utils.getIntByteArray(tx.getVersion());
		serializedTx = Arrays.copyOf(version, version.length);
		byte[] txInputsCount = Utils.getIntByteArray(tx.getTxInputsSize());
		serializedTx = Utils.concatenateByteArrays(serializedTx, txInputsCount);
		
		ArrayList<TransactionInput> txIns = tx.getTxInputs();
		for(int a = 0; a < txIns.size(); a++){
			if(a == txInputIndex){
				byte[] scriptPubKeyWithoutOPs = removeOpcodesFromScriptPubKey(uTXO.getScriptPubKey());
				txIns.get(a).setScriptSignature(scriptPubKeyWithoutOPs);
				txIns.get(a).setScriptLen(new BigInteger("" + scriptPubKeyWithoutOPs.length, 16));
			}else{
				txIns.get(a).setScriptSignature(null);
				txIns.get(a).setScriptLen("0");
			}
		}
	
		List<byte[]>byteList = tx.encodeInputListToByteArray(txIns);
		byte[] tIns = {};
		for(byte[] b : byteList){
			tIns = Utils.concatenateByteArrays(tIns, b);	// gathering all inputs together
		}
		serializedTx = Utils.concatenateByteArrays(serializedTx, tIns);	// add tx_Ins to Tx
		
		byte[] txOutputsCount = Utils.getIntByteArray(tx.getTxOutputsSize());
		serializedTx = Utils.concatenateByteArrays(serializedTx, txOutputsCount);
		
		List<byte[]> byteList2 = tx.encodeOutputListToByteArray(tx.getTxOutputs());
		byte[] tOuts = {};
		for(byte[] b : byteList2){
			tOuts = Utils.concatenateByteArrays(tOuts, b);	// gathering all inputs together
		}
		serializedTx = Utils.concatenateByteArrays(serializedTx, tOuts);	// add tx_outs to Tx
		
		return serializedTx;
	}
	
	/**
	 * cut the first 2 bytes and last 2 bytes off
	 * 
	 * the first 2 bytes are OP_DUP, OP_Hash160
	 * the last 2 bytes are OP_EQUALVERIFY, OP_CHECKSIG
	 * 
	 * @param scriptPubkey
	 * @return
	 */
	public byte[] removeOpcodesFromScriptPubKey(byte[] scriptPubkey){
		return Arrays.copyOfRange(scriptPubkey, 2, scriptPubkey.length-2);
	}
}
