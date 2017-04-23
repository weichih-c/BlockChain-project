package core;
import java.math.BigInteger;
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
	
	public Transaction(int version, BigInteger in_counter, ArrayList<TransactionInput> tx_inputs
			, BigInteger out_counter, ArrayList<TransactionOutput> tx_outputs){
		
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
	
	public TransactionInput decodeByteToTransactionInput(byte[] data){
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
	public ArrayList<TransactionInput> decodeByteArrayToInputList(List<byte[]> byteList){
		
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
	
	public TransactionOutput decodeByteToTransactionOutput(byte[] data){
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
	public ArrayList<TransactionOutput> decodeByteArrayToOutputList(List<byte[]> byteList){
		
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
	
}