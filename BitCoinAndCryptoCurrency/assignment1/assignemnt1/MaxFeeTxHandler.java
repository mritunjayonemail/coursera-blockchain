package assignemnt1;
import java.util.ArrayList;
import java.util.List;


public class MaxFeeTxHandler {
	
	UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public MaxFeeTxHandler(UTXOPool utxoPool) {
        this.utxoPool = new UTXOPool(utxoPool);
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
    	UTXOPool uniqueUtxoPool = new UTXOPool();
        ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        double inputValueSum = 0;
        // check if input is present in utxopool
        for(int i=0; i< inputs.size(); i++ ){
        	Transaction.Input input = inputs.get(i);
        	UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        	
        	Transaction.Output output = this.utxoPool.getTxOutput(utxo);
        	// transaction should not exist in pool
        	if (!utxoPool.contains(utxo)) {
        		return false;
        	}
        	// verify the signature
        	byte[] txRawDataToSign = tx.getRawDataToSign(i);
        	if(! Crypto.verifySignature(output.address, txRawDataToSign, input.signature)){
        		return false;
        	}
        	// check for uniqness
        	if (uniqueUtxoPool.contains(utxo)) {
        		return false;
        	}
        	uniqueUtxoPool.addUTXO(utxo, output);
        	if(output.value<0){
        		return false;
        	}
        	inputValueSum = inputValueSum + output.value;
        	
        }
        
        double outputValueSum = 0;
        for (Transaction.Output output: outputs){
        	outputValueSum = outputValueSum + output.value;
        }
        
        if (inputValueSum<outputValueSum){
        	return false;
        }
        
        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
    	List<Transaction> validTransactions = new ArrayList<Transaction>();
        for(Transaction tx : possibleTxs){
        	boolean validTransaction = isValidTx(tx);
        	
        	if(validTransaction){
        		validTransactions.add(tx);
        		for (Transaction.Input in : tx.getInputs()) {
                    UTXO utxo = new UTXO(in.prevTxHash, in.outputIndex);
                    utxoPool.removeUTXO(utxo);
                }
                for (int i = 0; i < tx.numOutputs(); i++) {
                	Transaction.Output out = tx.getOutput(i);
                    UTXO utxo = new UTXO(tx.getHash(), i);
                    utxoPool.addUTXO(utxo, out);
                }
        	}
        }
        Transaction[] validTxArray = new Transaction[validTransactions.size()];
        return validTransactions.toArray(validTxArray);
    }
    
    /*
     * get transaction fees
     */
    private double calcTxFees(Transaction tx) {
    	
        if(!isValidTx(tx)){
        	return 0;
        }
        double fees = 0;
    	ArrayList<Transaction.Input> inputs = tx.getInputs();
        ArrayList<Transaction.Output> outputs = tx.getOutputs();
        double inputValueSum = 0;
        // check if input is present in utxopool
        for(int i=0; i< inputs.size(); i++ ){
        	Transaction.Input input = inputs.get(i);
        	UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        	
        	Transaction.Output output = this.utxoPool.getTxOutput(utxo);
        	if (utxoPool.contains(utxo)) {
        		inputValueSum = inputValueSum + output.value;
        	}
        }
        double outputValueSum = 0;
        for(Transaction.Output output: outputs){
        	outputValueSum = outputValueSum + output.value;
        }
        fees = inputValueSum - outputValueSum; 
        return fees;
    }
    	 

}
