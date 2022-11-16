/**
 * Author: Changzhou Zheng
 * Date: Oct 27, 2022
 *
 * This is a constructor for response messages being sent from the server.
 */

public class ResponseMessage {
    int selection;
    int size;
    String chainHash;
    int totalHashes;
    int totalDiff;
    int recentNonce;
    int diff;
    int hps;
    String response;

    public ResponseMessage(int selection, int size, String chainHash, int totalHashes, int totalDiff, int recentNonce, int diff, int hps, String response) {
        this.selection = selection;
        this.size = size;
        this.chainHash = chainHash;
        this.totalHashes = totalHashes;
        this.totalDiff = totalDiff;
        this.recentNonce = recentNonce;
        this.diff = diff;
        this.hps = hps;
        this.response = response;
    }

}
