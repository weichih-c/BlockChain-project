package core;
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
	private String pubKeyPath;
	private String privKeyPath;

	
	public Consumer(String PubKeyPath, String PrivKeyPath){
		this.pubKeyPath = PubKeyPath;
		this.privKeyPath = PrivKeyPath;
	}
	
	
	public byte[] getPublicKeyEncoded(String publicKeyPath) throws NoSuchAlgorithmException, InvalidKeySpecException, IOException{
		KeyUtils keyUtil = new KeyUtils();
		PublicKey pubKey = keyUtil.loadPublicKey(publicKeyPath, algorithmName);
		return pubKey.getEncoded();
	}
	
	
	/**
	 * make ScriptSig in every Transaction Input of the specific Transaction.
	 * In this function, a user who wants to spend his UTXO must generate the scriptSig by the p2pKH in UTXOs and concatenate the pubKey in the end.
	 * 
	 * @param transaction
	 * @param utxoList
	 * @throws InvalidKeySpecException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public ArrayList<TransactionInput> makeScriptSignature(Transaction transaction, ArrayList<UnspentTXO> utxoList) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException{
		byte[] scriptSig = {};
		
		KeyUtils keyUtil = new KeyUtils();
		PrivateKey privKey = keyUtil.loadPrivateKey(privKeyPath, algorithmName);
		byte[] pubKey = getPublicKeyEncoded(pubKeyPath);
		
		ArrayList<TransactionInput> txIns = transaction.getTxInputs();
		Transaction txCopy = transaction;	// the transaction which is hashed is a copy.

		// generate the signature, concatenate the the pubKey 
		// and reset the scriptSig field in every TxInput.		
		for(int index=0; index < txIns.size(); index++){
			byte[] txData = hashModifiedTransactions(txCopy, index, utxoList.get(index) );
			byte[] signature = keyUtil.makeSignature(privKey, txData);	// make the signature using private key
			scriptSig = Utils.concatenateByteArrays(signature, pubKey);
//			System.out.println("txIn ScriptSig = " + Utils.getHexString( txIns.get(index).getScriptSignature()));
//			System.out.println("txIn scriptSigLen = " + txIns.get(index).getScriptLen());
			
			txIns.get(index).setScriptSignature(scriptSig);
			txIns.get(index).setScriptLen("" + scriptSig.length);
//			System.out.println("txIn ScriptSig = " + Utils.getHexString( txIns.get(index).getScriptSignature()));
//			System.out.println("txIn scriptSigLen = " + txIns.get(index).getScriptLen());
		}
		
		return txIns;
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
	public byte[] hashModifiedTransactions(Transaction tx, int txInputIndex, UnspentTXO uTXO){
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
	public byte[] modifyTransaction(Transaction tx, int txInputIndex, UnspentTXO uTXO){
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
