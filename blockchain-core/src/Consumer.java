import java.io.IOException;
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
	
	
	public Transaction createGeneralTransaction(TransactionInput UTXO, Receiver receiver, long spentValue){
		Transaction transaction = new Transaction();
		// TODO: add a wallet to save UTXO, consumer will spent UTXO as TransactionInput.
		// TODO: spent dollar => receiver[0] get spentValue, receiver[1] get change
		
//		TransactionInput tIn = new TransactionInput();
//		tIn.setPrev_hash(prev_hash);
//		transaction.addTxInput(tIn);
		
		byte[] scriptPubKey = receiver.getPublicKeyHashAddress().getBytes();
		
		return null;
	}
	
	public byte[] getScriptSignature(String pubKeyPath, String privKeyPath, Transaction transaction){
		try {
			byte[] signature = getSignature(privKeyPath, transaction);
			byte[] pubKey = getPublicKeyEncoded(pubKeyPath);
			byte[] scriptSig = Utils.concatenateByteArrays(signature, pubKey);
			
			return scriptSig;
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | IOException e) {
			System.out.println("Error: " + e.getMessage());
			return null;
		}
	}
	
	public byte[] getPublicKeyEncoded(String publicKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		KeyGenerator keyGen = new KeyGenerator();
		PublicKey pubKey = keyGen.loadPublicKey(publicKeyPath, algorithmName);
		return pubKey.getEncoded();
	}
	
	public byte[] getSignature(String privateKeyPath, Transaction transaction) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException{
		
		KeyGenerator keyGen = new KeyGenerator();
		PrivateKey privKey = keyGen.loadPrivateKey(privateKeyPath, algorithmName);
		byte[] txData = hashModifiedTransactions(transaction);	// hashing all txs
		byte[] signature = keyGen.makeSignature(privKey, txData);	// make the signature using private key

		return signature;
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
	 * @return
	 */
	public byte[] hashModifiedTransactions(Transaction tx){
		return HashGenerator.hashingSHA256Twice( modifyTransaction(tx) );

	}
	
	/** calculate the modifiedTransaction and transform to byte[]
	 *  set the scriptLen and ScriptSignature fields in all transactionInput to 0 and null respectively
	 * 
	 * @param tx
	 * @return
	 */
	public byte[] modifyTransaction(Transaction tx){
		byte[] serializedTx;
		byte[] version = Utils.getIntByteArray(tx.getVersion());
		serializedTx = Arrays.copyOf(version, version.length);
		byte[] txInputsCount = Utils.getIntByteArray(tx.getTxInputsSize());
		serializedTx = Utils.concatenateByteArrays(serializedTx, txInputsCount);
		
		ArrayList<TransactionInput> txIns = tx.getTxInputs();
		for(TransactionInput txIn : txIns){
			txIn.setScriptLen("0");
			txIn.setScriptSignature(null);
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
}
