import java.math.BigInteger;
import java.util.ArrayList;

public class Transaction {
	private Sha256Hash tx_hash;
	private int version;
	private BigInteger in_counter;
	private ArrayList<TransactionInput> tx_Inputs;
	private BigInteger out_counter;
	private ArrayList<TransactionOutput> tx_Outputs;
	
	public Transaction(){
		this.tx_Inputs = new ArrayList<>();
		this.tx_Outputs = new ArrayList<>();
	}
	
	public Transaction(int version, BigInteger in_counter, ArrayList<TransactionInput> tx_inputs
			, BigInteger out_counter, ArrayList<TransactionOutput> tx_outputs){
		
	}

	public Sha256Hash getTx_hash() {
		return tx_hash;
	}

	public void setTx_hash(Sha256Hash tx_hash) {
		this.tx_hash = tx_hash;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public BigInteger getIn_counter() {
		return in_counter;
	}

	public int getTxInputsSize() {
		return tx_Inputs.size();
	}
	
	public ArrayList<TransactionInput> getTxInputs(){
		return tx_Inputs;
	}

	public void addTxInput(TransactionInput tx_Input) {
		this.tx_Inputs.add( tx_Input );
		this.in_counter = new BigInteger(Integer.toHexString( tx_Inputs.size() ), 16);
	}

	public BigInteger getOut_counter() {
		return out_counter;
	}

	public int getTxOutputsSize() {
		return tx_Outputs.size();
	}
	
	public ArrayList<TransactionOutput> getTxOutputs(){
		return tx_Outputs;
	}

	public void addTxOutput(TransactionOutput tx_Output) {
		this.tx_Outputs.add( tx_Output );
		this.out_counter = new BigInteger(Integer.toHexString( tx_Outputs.size() ), 16);
	}
	
	
	
}
