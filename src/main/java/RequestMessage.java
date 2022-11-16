/**
 * Author: Changzhou Zheng
 * Date: Oct 27, 2022
 *
 * This is a constructor for messages being sent from clients.
 */

public class RequestMessage {
    int selection;
    int difficulty;
    String data;
    int blockIndex;

    public RequestMessage(int selection, int difficulty, String data, int blockIndex) {
        this.selection = selection;
        this.difficulty = difficulty;
        this.data = data;
        this.blockIndex = blockIndex;
    }

}
