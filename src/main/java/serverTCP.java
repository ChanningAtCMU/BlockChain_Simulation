import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * Author: Changzhou Zheng
 * Date: Oct 27, 2022
 *
 * This class acts as a TCP server that receives and process orders from clients
 * It will be constantly running until we manually turn it off
 * There will be 6 different categories of order in general
 * The server will first build a safe TCP tunnel with a client and listen to new orders
 * This server will first read Json format RequestMessage from clients,
 * and then read specific parameters for different commands.
 * From 0 to 5, each command will trigger the server to send back different Json format Strings as responses
 *
 * Important!!!
 * All sentence based Strings will be store under "response" key in each of the Json format response message
 * if there is one.
 */

public class serverTCP {

    public static void main(String[] args) {
        System.out.println("Blockchain server running");
        //Create a socket for clients and ready to connect
        Socket clientSocket = null;

        //Creat a "Genesis" block
        BlockChain bc = new BlockChain();
        Block genesis = new Block(0, bc.getTime(), "Genesis", 2);
        bc.addBlock(genesis);

        try {
            int serverPort = 6789; // the server port we are using

            // Create a new server socket
            ServerSocket listenSocket = new ServerSocket(serverPort);

            /*
             * Forever,
             *   read a line from the socket
             *   print it to the console
             *   echo it (i.e. write it) back to the client
             */
            while (true) {
                /*
                 * Block waiting for a new connection request from a client.
                 * When the request is received, "accept" it, and the rest
                 * the tcp protocol handshake will then take place, making
                 * the socket ready for reading and writing.
                 */
                clientSocket = listenSocket.accept();
                // If we get here, then we are now connected to a client.
                System.out.println("We have a visitor");

                // Set up "in" to read from the client socket
                Scanner inPort;
                inPort = new Scanner(clientSocket.getInputStream());

                // Set up "out" to write to the client socket
                PrintWriter outPort;
                outPort = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

                /*
                 * The following loop constantly receives messages sending from the client
                 */
                while(inPort.hasNextLine()){
                    //Constantly receive messages from clients
                    String userData = inPort.nextLine();
                    //Parse the String input to a RequestMessage Object
                    RequestMessage msg = buildRequestObj(userData);

                    //Find selection from the RequestMessage
                    int userOpt = msg.selection;

                    String response;

                    //If the order is 0
                    if(userOpt == 0){
                        //compute hashes per second and store the result inside the blockchain object
                        bc.computeHashesPerSecond();

                        //Call self-defined method to get response message
                        response = getOpt0RequestStr(msg, bc);

                        System.out.println("Response : "+response);

                        //Send the response message back to the client
                        outPort.println(response);
                        outPort.flush();
                    }
                    //Else if the order is 1
                    else if(userOpt == 1){
                        System.out.println("Adding a block");

                        //Start counting time
                        long startTime = System.currentTimeMillis();

                        //Create a new block and read parameters from the request message as input parameters
                        Block nextBlock = new Block(bc.getChainSize(), bc.getTime(), msg.data, msg.difficulty);
                        bc.addBlock(nextBlock);

                        //End counting time
                        long endTime = System.currentTimeMillis();

                        System.out.println("Setting response to Total execution time to add this block was "+
                                (endTime-startTime)+" milliseconds");

                        //Generate a response message in Json format (everything is included for client to call)
                        response = "{\"selection\":"+msg.selection+",\"size\":"+bc.getChainSize()+",\"chainHash\":\""+
                                bc.latestHash+"\",\"totalHashes\":"+(int)(bc.getTotalExpectedHashes())+
                                ",\"totalDiff\":"+bc.getTotalDifficulty()+",\"recentNonce\":"+
                                bc.getLatestBlock().getNonce()+",\"diff\":"+bc.getLatestBlock().getDifficulty()+
                                ",\"hps\":"+bc.getHashPerSecond()+",\"response\":\"Total execution time to add this block was "+
                                (endTime-startTime)+" milliseconds\"}";

                        System.out.println("..."+response);

                        //Send the response message back to the client
                        outPort.println(response);
                        outPort.flush();
                    }
                    //Else if the order is 2
                    else if(userOpt == 2){
                        System.out.println("Verifying entire chain");
                        //Counting time and verify the blockchain
                        long startTime = System.currentTimeMillis();
                        bc.isChainValid();
                        long endTime = System.currentTimeMillis();

                        //time elapsed = endTime - startTime
                        System.out.println("Chain verification: " + bc.isChainValid());
                        System.out.println("Total execution time to verify the chain was " + (endTime - startTime) + " milliseconds");
                        System.out.println("Setting response to Total execution time to verify the chain was " +
                                (endTime - startTime) + " milliseconds");

                        //Generate a response message in Json format (everything is included for client to call)
                        response = "{\"selection\":"+msg.selection+",\"size\":"+bc.getChainSize()+",\"chainHash\":\""+
                                bc.latestHash+"\",\"totalHashes\":"+(int)(bc.getTotalExpectedHashes())+
                                ",\"totalDiff\":"+bc.getTotalDifficulty()+",\"recentNonce\":"+
                                bc.getLatestBlock().getNonce()+",\"diff\":"+bc.getLatestBlock().getDifficulty()+
                                ",\"hps\":"+bc.getHashPerSecond()+",\"response\":\"Chain verification: "+
                                bc.isChainValid()+"\\nTotal execution time to add this block was "+
                                (endTime-startTime)+" milliseconds\"}";

                        //Send the response message back to the client
                        outPort.println(response);
                        outPort.flush();
                    }
                    //If no next line of input could be found, means client has quit
                    //Else if the order is 3
                    else if(userOpt == 3){
                        System.out.println("View the Blockchain");
                        //.toString() method directly parse a blockchain to a String
                        //We can directly use this result as response to the client
                        response = bc.toString();
                        System.out.println("Setting response to "+response);

                        //Send the response message back to the client
                        outPort.println(response);
                        outPort.flush();
                    }
                    //Else if the order is 4
                    else if(userOpt == 4){
                        System.out.println("Corrupt the Blockchain");

                        //Read blockIndex parameter from the request message to change corresponding block
                        Block corBlock = bc.chain.get(msg.blockIndex);
                        //Reset the content of the specified block
                        corBlock.setData(msg.data);
                        String newMsg =  "Block "+msg.blockIndex+" now holds "+msg.data;
                        System.out.println(newMsg);
                        System.out.println("Setting response to "+newMsg);

                        //Generate a response message in Json format (everything is included for client to call)
                        response = "{\"selection\":"+msg.selection+",\"size\":"+bc.getChainSize()+",\"chainHash\":\""+
                                bc.latestHash+"\",\"totalHashes\":"+(int)(bc.getTotalExpectedHashes())+
                                ",\"totalDiff\":"+bc.getTotalDifficulty()+",\"recentNonce\":"+
                                bc.getLatestBlock().getNonce()+",\"diff\":"+bc.getLatestBlock().getDifficulty()+
                                ",\"hps\":"+bc.getHashPerSecond()+",\"response\":\""+newMsg+"\"}";

                        //Send the response message back to the client
                        outPort.println(response);
                        outPort.flush();
                    }
                    //Else if the order is 5
                    else if(userOpt == 5){
                        System.out.println("Repairing the entire chain");
                        //Counting time for repairing the block
                        long startTime = System.currentTimeMillis();
                        bc.repairChain();
                        long endTime = System.currentTimeMillis();

                        String newReply = "Total execution time required to repair the chain was "+
                                (endTime - startTime)+" milliseconds";
                        System.out.println("Setting response to "+newReply);

                        //Generate a response message in Json format (everything is included for client to call)
                        response = "{\"selection\":"+msg.selection+",\"size\":"+bc.getChainSize()+",\"chainHash\":\""+
                                bc.latestHash+"\",\"totalHashes\":"+(int)(bc.getTotalExpectedHashes())+
                                ",\"totalDiff\":"+bc.getTotalDifficulty()+",\"recentNonce\":"+
                                bc.getLatestBlock().getNonce()+",\"diff\":"+bc.getLatestBlock().getDifficulty()+
                                ",\"hps\":"+bc.getHashPerSecond()+",\"response\":\""+newReply+"\"}";

                        //Send the response message back to the client
                        outPort.println(response);
                        outPort.flush();
                    }

                }

            }

        }
        // Handle exceptions
        catch (IOException e) {
            System.out.println("IO Exception:" + e.getMessage());

        }
        // If quitting (typically by you sending quit signal) clean up sockets
        finally {
            try {
                if (clientSocket != null) {
                    clientSocket.close();
                }
            } catch (IOException e) {
                // ignore exception on close
            }
        }
    }

    /**
     * This method specifically handle command 0
     * It generates a String response to the client by extracting parameters and variables
     * from the request message and the blockchain itself
     *
     * @param msg
     * @param bc
     * @return
     */
    private static String getOpt0RequestStr(RequestMessage msg, BlockChain bc){
        String response = "{\"selection\":"+msg.selection+",\"size\":"+bc.getChainSize()+",\"chainHash\":\""+
                bc.latestHash+"\",\"totalHashes\":"+bc.getTotalExpectedHashes()+
                ",\"totalDiff\":"+bc.getTotalDifficulty()+",\"recentNonce\":"+
                bc.getLatestBlock().getNonce()+",\"diff\":"+bc.getLatestBlock().getDifficulty()+
                ",\"hps\":"+bc.getHashPerSecond()+"}";

        return response;
    }

    /**
     * This method parse a Json format String to a RequestMessage object
     *
     * @param output
     * @return
     */
    private static RequestMessage buildRequestObj(String output){
        Gson gson = new Gson();
        RequestMessage incommingMsg = gson.fromJson(output, RequestMessage.class);
        return incommingMsg;
    }

}