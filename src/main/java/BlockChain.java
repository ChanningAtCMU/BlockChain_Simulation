import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Author: Changzhou Zheng
 * Date: Oct 25, 2022
 *
 * This class simulates a blockchain that each node copies information from the blockchain.
 * Information from each block were reformatted by hashing. The time for each hashing is recorded as well.
 * As the difficulty goes up, each blockchain takes longer time to addBlock()
 * but takes almost the same time to verify and less time to react after a corrupted block is repaired.
 */

public class BlockChain {

    public static void main(String[] args) {
        BlockChain chain = new BlockChain();
        Block genesis = new Block(0, chain.getTime(), "Genesis", 2);
        chain.addBlock(genesis);

        long startTime, endTime;

        //Start iteration and catches all inputs
        //Return all required outputs by calling methods above
        Scanner input = new Scanner(System.in);
        while(true) {
            System.out.println("0. View basic blockchain status.\n" +
                    "1. Add a transaction to the blockchain.\n" +
                    "2. Verify the blockchain.\n" +
                    "3. View the blockchain.\n" +
                    "4. Corrupt the chain.\n" +
                    "5. Hide the corruption by repairing the chain.\n" +
                    "6. Exit");
            int choice = input.nextInt();

            if (choice == 0) {
                System.out.println("Current size of chain: " + chain.getChainSize());
                System.out.println("Difficulty of most recent block: " + chain.getLatestBlock().getDifficulty());
                System.out.println("Total difficulty for all blocks: " + chain.getTotalDifficulty());
                chain.computeHashesPerSecond();
                System.out.println("Approximate hashes per second on this machine: " + chain.getHashPerSecond());
                System.out.println("Expected total hashes required for the whole chain: " + chain.getTotalExpectedHashes());
                System.out.println("Nonce for most recent block: " + chain.getLatestBlock().getNonce());
                System.out.println("Chain hash: " + chain.latestHash);
            } else if (choice == 1){
                System.out.println("Enter difficulty > 0");
                int diffLv = input.nextInt();
                System.out.println("Enter transaction");
                input.nextLine();
                String transaction = input.nextLine();

                Block nextBlock = new Block(chain.getChainSize(), chain.getTime(), transaction, diffLv);

                startTime = System.currentTimeMillis();
                chain.addBlock(nextBlock);
                endTime = System.currentTimeMillis();

                System.out.println("Total execution time to add this block was " + (endTime - startTime) + " milliseconds");
            } else if (choice == 2){
                startTime = System.currentTimeMillis();
                System.out.println("Chain verification: " + chain.isChainValid());
                endTime = System.currentTimeMillis();

                System.out.println("Total execution time to verify the chain was " + (endTime - startTime) + " milliseconds");
            } else if (choice == 3){
                String chainOutput = chain.toString();
                System.out.println("View the Blockchain");
                System.out.println(chainOutput);
            } else if (choice == 4){
                System.out.println("corrupt the Blockchain\n" +
                        "Enter block ID of block to corrupt");
                int corruptID = input.nextInt();
                System.out.println("Enter new data for block " + corruptID);
                input.nextLine();
                String transaction = input.nextLine();

                Block corBlock = chain.chain.get(corruptID);
                corBlock.setData(transaction);

                System.out.println("Block " + corruptID + " now holds " + transaction);
            } else if (choice == 5){
                startTime = System.currentTimeMillis();
                chain.repairChain();
                endTime = System.currentTimeMillis();
                System.out.println("Total execution time required to repair the chain was " + (endTime - startTime) + " milliseconds");
            } else if (choice == 6){
                System.exit(0);
            }
        }
    }

    ArrayList<Block> chain;
    String latestHash;
    int hashesPerSecond;

    public BlockChain(){
        this.chain = new ArrayList<>();
        this.latestHash = "";
    }

    /**
     * A new Block is being added to the BlockChain.
     * This new block's previous hash must hold the hash of the most recently added block.
     * After this call on addBlock, the new block becomes the most recently added block on the BlockChain.
     * The SHA256 hash of every block must exhibit proof of work, i.e.,
     * have the requisite number of leftmost 0's defined by its difficulty.
     * Suppose our new block is x. And suppose the old blockchain was
     * a <-- b <-- c <-- d then the chain after addBlock completes is
     * a <-- b <-- c <-- d <-- x. Within the block x, there is a previous hash field.
     * This previous hash field holds the hash of the block d. The block d is called the parent of x.
     * The block x is the child of the block d. It is important to also maintain
     * a hash of the most recently added block in a chain hash. Let's look at our two chains again.
     * a <-- b <-- c <-- d. The chain hash will hold the hash of d. After adding x, we have a <-- b <-- c <-- d <-- x.
     * The chain hash now holds the hash of x.
     * The chain hash is not defined within a block but is defined within the blockchain.
     * The arrows are used to describe these hash pointers. If b contains the hash of a then we write a <-- b.
     * @param newBlock
     */
    public void addBlock(Block newBlock){
        newBlock.setPreviousHash(this.latestHash);
        this.chain.add(newBlock);
        this.latestHash = newBlock.proofOfWork();
    }

    /**
     * This method computes exactly 2 million hashes and times how long that process takes.
     * So, hashes per second is approximated as (2 million / number of seconds).
     * It is run on start up and sets the instance variable hashesPerSecond.
     * It uses a simple string - "00000000" to hash.
     */
    public void computeHashesPerSecond(){
        long start = System.currentTimeMillis();
        int i = 0;
        while(i < 2000000){
            try {
                Block.SHA2Str("00000000");
                i++;
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        long end = System.currentTimeMillis();

        //Finally, calculate time elapse by subtract the end by the start
        this.hashesPerSecond = (int)(i/((end-start)/1000.00));
    }

    /**
     * return block at position i
     * @param i
     * @return block at postion i
     */
    public Block getBlock(int i){
        return this.chain.get(i);
    }

    /**
     * simple getter method
     * @return the size of the chain in blocks.
     */
    public int getChainSize(){
        return this.chain.size();
    }

    /**
     * get hashes per second
     * @return the instance variable approximating the number of hashes per second.
     */
    public int getHashPerSecond() {
        return hashesPerSecond;
    }

    /**
     * Get the latest-added block
     * @return a reference to the most recently added Block.
     */
    public Block getLatestBlock(){
        Block lastBlock = this.chain.get(this.chain.size()-1);
        return lastBlock;
    }

    /**
     * get the current System time
     * @return the current system time
     */
    public java.sql.Timestamp getTime(){
        java.sql.Timestamp now = new java.sql.Timestamp(System.currentTimeMillis());
        return now;
    }

    /**
     * Compute and return the total difficulty of all blocks on the chain. Each block knows its own difficulty.
     * @return totalDifficulty
     */
    public int getTotalDifficulty(){
        int total = 0;
        Block curBlock;
        for(int i = 0; i < this.chain.size(); i++){
            curBlock = this.chain.get(i);
            total += curBlock.getDifficulty();
        }

        return total;
    }

    /**
     * Compute and return the expected number of hashes required for the entire chain.
     * @return totalExpectedHashes
     */
    public double getTotalExpectedHashes(){
        double total = 0;
        int curDif = 0;
        for(int i = 0; i < this.chain.size(); i ++){
            curDif = this.chain.get(i).getDifficulty();
            total += Math.pow(16, curDif);
        }

        return total;
    }

    /**
     * If the chain only contains one block, the genesis block at position 0,
     * this routine computes the hash of the block and checks that the hash has
     * the requisite number of leftmost 0's (proof of work) as specified in the difficulty field.
     * It also checks that the chain hash is equal to this computed hash. If either check fails,
     * return an error message. Otherwise, return the string "TRUE". If the chain has more blocks than one,
     * begin checking from block one. Continue checking until you have validated the entire chain.
     * The first check will involve a computation of a hash in Block 0 and a comparison with the hash pointer in Block 1.
     * If they match and if the proof of work is correct, go and visit the next block in the chain.
     * At the end, check that the chain hash is also correct.
     * @return "TRUE" if the chain is valid, otherwise return "FALSE"
     */
    public String isChainValid(){
        Block curBlock;
        String curHash;
        String lastHash = "";

        for(int i = 0; i < this.chain.size(); i++){
            curBlock = this.chain.get(i);
            if(!curBlock.previousHash.equals(lastHash)){
                return "FALSE";
            }

            curHash = curBlock.calculateHash();
            //Check if the difficulties are different
            if(!curHash.substring(0, curBlock.difficulty).equals("0".repeat(curBlock.difficulty))){
                return "False\\nImproper hash on node " + i + " Does not begin with " + "0".repeat(curBlock.difficulty);
            }

            lastHash = curHash;
        }

        //If the latest block has the same hash in the blockchain, return "TRUE"
        if(latestHash.equals(lastHash)){
            return "TRUE";
        } else {
            return "FALSE";
        }
    }

    /**
     * This routine repairs the chain.
     * It checks the hashes of each block and ensures that any illegal hashes are recomputed.
     * After this routine is run, the chain will be valid.
     * The routine does not modify any difficulty values.
     * It computes new proof of work based on the difficulty specified in the Block.
     */
    public void repairChain(){
        Block curBlock;
        String newHash = "";

        for(int i = 0; i < this.chain.size(); i++){
            curBlock = this.chain.get(i);
            //Redefine the precious hash by null
            curBlock.setPreviousHash(newHash);
            newHash = curBlock.proofOfWork();
        }

        this.latestHash = newHash;
    }

    /**
     * This method overrides to present the results in a JSON format that we wish to have in our blockchain
     * @return a formatted String output
     */
    @Override
    public String toString(){
        String output = "{\"ds_chain\" : [";
        for(int i = 0; i < this.chain.size(); i++){
            output += this.chain.get(i);
            if(i != this.chain.size()-1){
                output += ",\n";

            } else {
                output += "\n";
            }
        }

        output += "], \"chainHash\":\"" + this.latestHash + "\"}";
        return output;
    }

}
