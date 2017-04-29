package core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.*;

public class Transaction {
	private Sha256Hash tx_hash;
	private int version;
	private ArrayList<TransactionInput> tx_Inputs;
	private ArrayList<TransactionOutput> tx_Outputs;
	private boolean anyChange = false;
	
	public Transaction(){
		this.version = 1;
		this.tx_Inputs = new ArrayList<>();
		this.tx_Outputs = new ArrayList<>();
	}
	
	public Transaction(int version, ArrayList<TransactionInput> tx_inputs,
			ArrayList<TransactionOutput> tx_outputs){
		this.version = version;
		this.tx_Inputs = tx_inputs;
		this.tx_Outputs = tx_outputs;
	}
	

	public Sha256Hash getTx_hash() {
		byte[] tx;
		byte[] version = reverseEndian( Utils.getIntByteArray(this.version));
		tx = Arrays.copyOf(version, version.length);	// add version to tx
		tx = Utils.concatenateByteArrays(tx, reverseEndian( Utils.getIntByteArray(this.tx_Inputs.size())));	// add in_counter to tx

		List<byte[]> bList = encodeInputListToByteArray(this.tx_Inputs);
		byte[] tIns = {};
		for(byte[] b : bList){
			tIns = Utils.concatenateByteArrays(tIns, b);	// gathering all inputs together
		}
		tx = Utils.concatenateByteArrays(tx, reverseEndian( tIns ));	// add tx_Ins to tx
		
		tx = Utils.concatenateByteArrays(tx, reverseEndian( Utils.getIntByteArray(this.tx_Outputs.size())));	// add out_counter to tx
		
		List<byte[]> bList2 = encodeOutputListToByteArray(this.tx_Outputs);
		byte[] tOuts = {};
		for(byte[] b : bList2){
			tOuts = Utils.concatenateByteArrays(tOuts, b);	// gathering all inputs together
		}
		tx = Utils.concatenateByteArrays(tx, reverseEndian(tOuts));	// add tx_outs to tx
		
		byte[] txHash = reverseEndian( HashGenerator.hashingSHA256Twice(tx) );
		
		return new Sha256Hash( txHash );
	}

//	private void setTx_hash(Sha256Hash tx_hash) {
//		this.tx_hash = tx_hash;
//	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getTxInputsSize() {
		return tx_Inputs.size();
	}
	
	public ArrayList<TransactionInput> getTxInputs(){
		return tx_Inputs;
	}

	public void addTxInput(TransactionInput tx_Input) {
		this.tx_Inputs.add( tx_Input );
//		this.in_counter = new BigInteger(Integer.toHexString( tx_Inputs.size() ), 16);
	}

	public int getTxOutputsSize() {
		return tx_Outputs.size();
	}
	
	public ArrayList<TransactionOutput> getTxOutputs(){
		return tx_Outputs;
	}

	public void addTxOutput(TransactionOutput tx_Output) {
		this.tx_Outputs.add( tx_Output );
//		this.out_counter = new BigInteger(Integer.toHexString( tx_Outputs.size() ), 16);
	}
	
	public byte[] encodeTransactionInputToByte(TransactionInput txIn){
		byte[] data = SerializationUtils.serialize(txIn);
		return data;
	}
	
	public static TransactionInput decodeByteToTransactionInput(byte[] data){
		TransactionInput txIn = (TransactionInput) SerializationUtils.deserialize(data);
		return txIn;
	}
	
	// write TransactionInput to byte array
	public List<byte[]> encodeInputListToByteArray(ArrayList<TransactionInput> list){
		
		List<byte[]> tmp = new ArrayList<>();
		for(TransactionInput in : list){
			byte[] b = encodeTransactionInputToByte(in);
			tmp.add(b);
		}

    	return tmp;
    }
	
	// decode to transactionInput array
	public static ArrayList<TransactionInput> decodeByteArrayToInputList(List<byte[]> byteList){
		
		ArrayList<TransactionInput> list = new ArrayList<>();
		for(byte[] arr : byteList){
			TransactionInput txIn = decodeByteToTransactionInput(arr);
			list.add(txIn);
		}		
		
		return list;
	}
	
	public byte[] encodeTransactionOutputToByte(TransactionOutput txOut){
		byte[] data = SerializationUtils.serialize(txOut);
		return data;
	}
	
	public static TransactionOutput decodeByteToTransactionOutput(byte[] data){
		TransactionOutput txOut = (TransactionOutput) SerializationUtils.deserialize(data);
		return txOut;
	}
	
	// write TransactionOutput to byte array
	public List<byte[]> encodeOutputListToByteArray(ArrayList<TransactionOutput> list){
		
		List<byte[]> tmp = new ArrayList<>();
		for(TransactionOutput in : list){
			byte[] b = encodeTransactionOutputToByte(in);
			tmp.add(b);
		}

    	return tmp;
    }
	
	// decode to transactionOutput array
	public static ArrayList<TransactionOutput> decodeByteArrayToOutputList(List<byte[]> byteList){
		
		ArrayList<TransactionOutput> list = new ArrayList<>();
		for(byte[] arr : byteList){
			TransactionOutput txIn = decodeByteToTransactionOutput(arr);
			list.add(txIn);
		}		
		
		return list;
	}
	
	/**
	 * turn big endian to little endian
	 * @param b
	 * @return
	 */
	private byte[] reverseEndian (byte[]b){
		return Utils.reverseBytes(b);
	}

	public boolean isAnyChange() {
		return anyChange;
	}

	public void setAnyChange(boolean anyChange) {
		this.anyChange = anyChange;
	}
	
	
	/**
	 * Serialize a given transaction.
	 * 
	 * @param tx transaction
	 * @return
	 */
	public static byte[] serializeTransaction(Transaction tx){
		byte[] serializedTx;
		byte[] version = Utils.getIntByteArray(tx.getVersion());
		serializedTx = Arrays.copyOf(version, version.length);
		System.out.println("version len = "+version.length);
		byte[] txInputsCount = Utils.getIntByteArray(tx.getTxInputsSize());
		serializedTx = Utils.concatenateByteArrays(serializedTx, txInputsCount);
		System.out.println("tx Input Size = " + tx.getTxInputsSize());
		System.out.println("txInputsCount len = "+txInputsCount.length);

		ArrayList<TransactionInput> txIns = tx.getTxInputs();
	
		List<byte[]>byteList = tx.encodeInputListToByteArray(txIns);
		for(byte[] b : byteList){
			serializedTx = Utils.concatenateByteArrays(serializedTx, Utils.getIntByteArray(b.length));
			serializedTx = Utils.concatenateByteArrays(serializedTx, b);	// gathering all inputs together
			
			System.out.println("input byte len = " + b.length);
			
		}
		
		byte[] txOutputsCount = Utils.getIntByteArray(tx.getTxOutputsSize());
		serializedTx = Utils.concatenateByteArrays(serializedTx, txOutputsCount);
		System.out.println("tx Out Size = " + tx.getTxOutputsSize());
		System.out.println("txOutputsCount len = "+txOutputsCount.length);
		
		List<byte[]> byteList2 = tx.encodeOutputListToByteArray(tx.getTxOutputs());
		for(byte[] b : byteList2){
			serializedTx = Utils.concatenateByteArrays(serializedTx, Utils.getIntByteArray(b.length));
			serializedTx = Utils.concatenateByteArrays(serializedTx, b);	// gathering all inputs together
			System.out.println("output byte len = " + b.length);

		}
		
		return serializedTx;
	}
	
	public static Transaction deserializeTransaction(byte[] data){
		byte[] versionBytes = Arrays.copyOfRange(data, 0, 4);
		int version = Utils.byteArrayToInt(versionBytes);
		System.out.println("version = "+version);
		
		byte[] tInCountBytes = Arrays.copyOfRange(data, 4, 8);
		int tInCount = Utils.byteArrayToInt(tInCountBytes);
		System.out.println("input size = "+tInCount);
		
		List<byte[]> tInsList = new ArrayList<>();
		int from = 8;
		// iterative decode every txInput's byte array and add to a list
		for(int a=0; a<tInCount; a++){
			int tInByteLen = Utils.byteArrayToInt(Arrays.copyOfRange(data, from, from + 4));
			System.out.println("input byte len = " + tInByteLen);
			
			byte[] tInBytes = Arrays.copyOfRange(data, from + 4, from + 4 + tInByteLen);
			tInsList.add(tInBytes);
			from = from + 4 + tInByteLen;
		}
		ArrayList<TransactionInput>txInsList = decodeByteArrayToInputList(tInsList);
		
		byte[] tOutCountBytes = Arrays.copyOfRange(data, from, from+4);
		int tOutCount = Utils.byteArrayToInt(tOutCountBytes);
		System.out.println("output size = "+tOutCount);
		
		List<byte[]> tOutsList = new ArrayList<>();
		from = from+4;
		// iterative decode every txOutput's byte array and add to a list
		for(int a=0; a<tOutCount; a++){
			int tOutByteLen = Utils.byteArrayToInt(Arrays.copyOfRange(data, from, from+4));
			System.out.println("output byte len = " + tOutByteLen);
			
			byte[] tOutBytes = Arrays.copyOfRange(data, from + 4, from + 4 + tOutByteLen);
			tOutsList.add(tOutBytes);
			from = from + 4 + tOutByteLen;
		}
		ArrayList<TransactionOutput>txOutsList = decodeByteArrayToOutputList(tOutsList);

		Transaction tx = new Transaction(version, txInsList, txOutsList);
		return tx;
		
		
	}
}
