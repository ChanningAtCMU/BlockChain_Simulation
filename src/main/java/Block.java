import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;

/**
 * Author: Changzhou Zheng
 * Date: Oct 26, 2022
 *
 * The Block class is a constructor for each block that will be store in a blockchain.
 * 4 parameters were asked when create a new Block object.
 * nonce and previousHash will be calculated by hashing.
 */

public class Block {

    public static void main(String[] args) {}

    int index;
    Timestamp timestamp;
    String data;
    int difficulty;
    String previousHash;
    BigInteger nonce;

    /**
     * Constructor for each Block object
     * @param ind
     * @param ts
     * @param dt
     * @param diff
     */
    Block(int ind, Timestamp ts, String dt, int diff){
        this.index = ind;
        this.timestamp = ts;
        this.data = dt;
        this.difficulty = diff;
    }

    /**
     * Convert string variables by SHA-256
     * @param input
     * @return
     * @throws NoSuchAlgorithmException
     */
    public static String SHA2Str(String input) throws NoSuchAlgorithmException {
        //Get digest ready
        MessageDigest digest;
        digest = MessageDigest.getInstance("SHA-256");
        //Store digested characters
        byte[] hashed;
        digest.update(input.getBytes());
        hashed = digest.digest();

        //Extract hashed characters from byte array and append to a String
        StringBuffer sbf = new StringBuffer();
        for(int i = 0; i < hashed.length; i++){
            //Reference: https://www.tutorialspoint.com/Bitwise-right-shift-operator-in-Java
            //Reference: https://stackoverflow.com/questions/12989969/what-does-0x0f-mean-and-what-does-this-code-mean
            int upperNibble = (hashed[i] >>> 4) & 0x0F;
            int detector = 0;
            while(detector++ <1){
                if((upperNibble >= 0) && (upperNibble <= 9)){
                    sbf.append((char)('0'+upperNibble));
                } else {
                    sbf.append((char)('a'+ (upperNibble - 10)));
                }
                upperNibble = hashed[i] & 0x0F;
            }
        }

        String resultStr = sbf.toString().toUpperCase();

        return resultStr;
    }

    /**
     * This method computes a hash of the concatenation of the index, timestamp, data, previousHash, nonce, and difficulty.
     * Append all 6 parameters together and convert to a hashed String output
     * @return
     */
    public String calculateHash(){
        String s = "";
        s += String.valueOf(this.index);
        s += this.timestamp.toString();
        s += this.data;
        s += this.previousHash;
        s += this.nonce;
        s += this.difficulty;

        try {
            String hashStr = SHA2Str(s);

            return hashStr;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Simple getter method
     * @return
     */
    public String getData(){
        return this.data;
    }

    /**
     * Simple getter method
     * @return
     */
    public int getDifficulty(){
        return this.difficulty;
    }

    /**
     * Simple getter method
     * @return
     */
    public int getIndex() {
        return this.index;
    }

    /**
     * Simple getter method
     * @return
     */
    public BigInteger getNonce() {
        return this.nonce;
    }

    /**
     * Simple getter method
     * @return
     */
    public String getPreviousHash() {
        return this.previousHash;
    }

    /**
     * Simple getter method
     * @return
     */
    public Timestamp getTimestamp() {
        return this.timestamp;
    }

    /**
     * The proof of work methods finds a good hash. It increments the nonce until it produces a good hash.
     *
     * This method calls calculateHash() to compute a hash of the concatenation of the index,
     * timestamp, data, previousHash, nonce, and difficulty.
     * If the hash has the appropriate number of leading hex zeroes, it is done and returns that proper hash.
     * If the hash does not have the appropriate number of leading hex zeroes,
     * it increments the nonce by 1 and tries again.
     * It continues this process, burning electricity and CPU cycles, until it gets lucky and finds a good hash.
     * @return a String with a hash that has the appropriate number of leading hex zeroes.
     * The difficulty value is already in the block. This is the minimum number of hex 0's a proper hash must have.
     */
    public String proofOfWork(){
        this.nonce = BigInteger.valueOf(0);
        String hash = calculateHash();
        while(true){
            if(hash.substring(0, this.difficulty).equals("0".repeat(this.difficulty))){
                return hash;
            } else {
                this.nonce = this.nonce.add(BigInteger.valueOf(1));
            }
            hash = calculateHash();
        }
    }

    /**
     * Simple setter method
     * @param data
     */
    public void setData(String data) {
        this.data = data;
    }

    /**
     * Simple setter method
     * @param difficulty
     */
    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Simple setter method
     * @param index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Simple setter method
     * @param nonce
     */
    public void setNonce(BigInteger nonce) {
        this.nonce = nonce;
    }

    /**
     * Simple setter method
     * @param previousHash
     */
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    /**
     * Simple setter method
     * @param timestamp
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * This method overrides the previous usage and now returns a String
     * in a JSON format that we wish to have in our BlockChain.
     * @return
     */
    @Override
    public String toString(){
        String s = "{\"index\" : ";
        s += String.valueOf(this.index);
        s += ",\"time stamp \" : \"";
        s += this.timestamp.toString()+"\"";
        s += ",\"Tx \" : \"";
        s += this.data;
        s += "\"";
        s += ",\"PrevHash\" : \"";
        s += this.previousHash;
        s += "\"";
        s += ",\"nonce\" : ";
        s += this.nonce;
        s += ",\"difficulty\" : ";
        s += String.valueOf(this.difficulty);
        s += "}";
        return s;
    }

}
