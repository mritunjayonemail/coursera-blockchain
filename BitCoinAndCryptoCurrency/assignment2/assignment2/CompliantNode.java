package assignment2;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/* CompliantNode refers to a node that follows the rules (not malicious)*/
public class CompliantNode implements Node {
	
	private double p_graph;
	private double p_malicious;
	private double p_txDistribution;
	private int numRounds;
	
	private boolean[] followees;
	private Set<Transaction> pendingTransactions;
	private boolean[] blackListed;
	

    public CompliantNode(double p_graph, double p_malicious, double p_txDistribution, int numRounds) {
       this.p_graph = p_graph;
       this.p_malicious = p_malicious;	
       this.p_txDistribution = p_txDistribution;
       this.numRounds = numRounds;
        	
    }

    public void setFollowees(boolean[] followees) {
       this.followees = followees;
       this.blackListed = new boolean[this.followees.length];
    }

    public void setPendingTransaction(Set<Transaction> pendingTransactions) {
        this.pendingTransactions = pendingTransactions;
        
    }

    public Set<Transaction> sendToFollowers() {
    	Set<Transaction> pendingTarnsToSend = new HashSet<>(pendingTransactions);
    	pendingTransactions.clear();
    	return pendingTarnsToSend;
    }

    public void receiveFromFollowees(Set<Candidate> candidates) {
    	Set<Integer> senderIds = new HashSet<Integer>();
    	for(Candidate candidate:candidates){
    		senderIds.add(candidate.sender);
    	}
        /*get blacklisted transactions*/
    	for (int index=0; index < followees.length; index++){
    		if (followees[index] && !senderIds.contains(index)){
    			blackListed[index] = true;
    		}
    	}
    	/* if not balcklisted then add into nodes */
    	for(Candidate candidate:candidates){
    		if(!blackListed[candidate.sender]){
    			this.pendingTransactions.add(candidate.tx);
    		}
    	}	
    }
}
