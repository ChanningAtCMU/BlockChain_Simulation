import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

/**
 * Author: Changzhou Zheng
 * Date: Oct 27, 2022
 *
 * This class is a client in a TCP communication
 * The main function for it is to send commands from 0 to 5, and 6 to cease itself from running
 * It also reads from the server side
 * Once server send back response messages in Json format,
 * this client will read useful parameters from the Json format response messages.
 * Finally, it shows users the result after the interpretation above.
 */

public class clientTCP {
    public static void main(String[] args) {
        System.out.println("The client is running.");

        //Arguments supply hostname
        Socket clientSocket = null;

        try {
            int serverPort = 6789;
            //Build the client socket with the user-assigned server port number
            clientSocket = new Socket("localhost", serverPort);

            //Enable the client to catch input streams
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            //Enable the client to send output streams
            PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream())));

            //BufferReader return the current user input after each println
            Scanner userInput = new Scanner(System.in);
            int option;

            while (true){ //Keep iterating if there is another order input
                //Ask client for options
                System.out.println("0. View basic blockchain status.\n" +
                        "1. Add a transaction to the blockchain.\n" +
                        "2. Verify the blockchain.\n" +
                        "3. View the blockchain.\n" +
                        "4. Corrupt the chain.\n" +
                        "5. Hide the corruption by repairing the chain.\n" +
                        "6. Exit");
                //Retrieve the option
                option = userInput.nextInt();

                //If the user is going to change one of the values
                if (option == 0) {
                    //The only parameter the server will be using is the first - the "selection"
                    //The other parameters are set to be 0 or "" to avoid code coupling
                    RequestMessage opt0Request = new RequestMessage(0, 0, "", 0);

                    //Parse the request to a Json format String
                    String outputMsg = toJson(opt0Request);

                    //Send the Json format request to the server
                    out.println(outputMsg);
                    out.flush();

                    //Read from the server
                    String response = in.readLine();
                    //Parse the input to a ResponseMessage object
                    ResponseMessage responseObj = buildResponseObj(response);

                    //Print needed parameters by call from the ResponseMessage object
                    System.out.println("Current size of chain: "+responseObj.size);
                    System.out.println("Difficulty of most recent block: "+responseObj.diff);
                    System.out.println("Total difficulty for all blocks: "+responseObj.totalDiff);
                    System.out.println("Approximate hashes per second on this machine: "+responseObj.hps);
                    System.out.println("Expected total hashes required for the whole chain: "+responseObj.totalHashes);
                    System.out.println("Nonce for most recent block: "+responseObj.recentNonce);
                    System.out.println("Chain hash: "+responseObj.chainHash+"\n");

                }
                else if (option == 1) {
                    //Ask for difficulty level and record
                    System.out.println("Enter difficulty > 0");
                    int diffLv = userInput.nextInt();

                    //Ask for transaction content and record
                    System.out.println("Enter transaction");
                    userInput.nextLine();
                    String data = userInput.nextLine();

                    //This time, the RequestMessage contain the selection, difficulty level, and the transaction
                    //However, the block index is left to be 0 to avoid from coupling
                    RequestMessage opt1Request = new RequestMessage(1, diffLv, data, 0);
                    String outputMsg = toJson(opt1Request);

                    //Send the Json format request to the server
                    out.println(outputMsg);
                    out.flush();

                    //Read from the server
                    String response = in.readLine();
                    //Parse the Json format input stream to a ResponseMessage object
                    ResponseMessage responseObj = buildResponseObj(response);

                    System.out.println(responseObj.response);
                }
                else if (option == 2) {
                    //The only parameter the server will be using is the first - the "selection"
                    //The other parameters are set to be 0 or "" to avoid code coupling
                    RequestMessage opt2Request = new RequestMessage(2, 0, "", 0);
                    String outputMsg = toJson(opt2Request);

                    //Send the Json format request to the server
                    out.println(outputMsg);
                    out.flush();

                    //Read from the server
                    String response = in.readLine();
                    //Parse the Json format input stream to a ResponseMessage object
                    ResponseMessage responseObj = buildResponseObj(response);

                    System.out.println(responseObj.response);
                }
                else if (option == 3) {
                    //The only parameter the server will be using is the first - the "selection"
                    //The other parameters are set to be 0 or "" to avoid code coupling
                    RequestMessage opt3Request = new RequestMessage(3, 0, "", 0);
                    String outputMsg = toJson(opt3Request);

                    //Send the Json format request to the server
                    out.println(outputMsg);
                    out.flush();

                    //To read the entire blockchain from server, we have to consider changing lines
                    //Here use an iteration to read response messages line by line
                    String response;
                    while((response = in.readLine()) != null){
                        System.out.println(response);
                        if(response.charAt(response.length()-1) == '}'){
                            System.out.println(in.readLine());
                            break;
                        }
                    }

                }
                else if (option == 4) {
                    System.out.println("corrupt the Blockchain");

                    //Ask for the block index for corruption and record
                    System.out.println("Enter block ID of block to corrupt");
                    int corID = userInput.nextInt();
                    //Ask for new transaction content
                    System.out.println("Enter new data for block "+corID);
                    userInput.nextLine();
                    String data = userInput.nextLine();

                    //A RequestMessage is built to deliver user-input content
                    // alongside with the block index the user wanted to corrupt
                    RequestMessage opt4Request = new RequestMessage(4, 0, data, corID);
                    String outputMsg = toJson(opt4Request);

                    //Send the Json format request to the server
                    out.println(outputMsg);
                    out.flush();

                    //Read from the server
                    String response = in.readLine();
                    //Parse the Json format input stream to a ResponseMessage object
                    ResponseMessage responseObj = buildResponseObj(response);

                    //Show the time for this operation in client console
                    System.out.println(responseObj.response);
                }
                else if (option == 5) {
                    //The only parameter the server will be using is the first - the "selection"
                    //The other parameters are set to be 0 or "" to avoid code coupling
                    RequestMessage opt5Request = new RequestMessage(5, 0, "", 0);
                    String outputMsg = toJson(opt5Request);

                    //Send the Json format request to the server
                    out.println(outputMsg);
                    out.flush();

                    //Read from the server
                    String response = in.readLine();
                    //Parse the Json format input stream to a ResponseMessage object
                    ResponseMessage responseObj = buildResponseObj(response);

                    //Show the time for repairment
                    System.out.println(responseObj.response+"\n");
                }
                else {
                    //Quit from the client side
                    System.exit(0);
                }
            }


        }
        //Catch all exceptions
        catch (SocketException e) {System.out.println("Socket: " + e.getMessage());
        }
        catch (IOException e){System.out.println("IO: " + e.getMessage());
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {if(clientSocket != null) {
            try {
                clientSocket.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        }
    }

    /**
     * This method convert the client request message to a Json format String for the sake of server
     *
     * @param msg
     * @return
     */
    private static String toJson(RequestMessage msg){
        Gson gson = new Gson();
        String messageToSend = gson.toJson(msg);
        return messageToSend;
    }

    /**
     * This method read Json format responses from the server and parse those to ResponseMessage object
     * to call parameters to get necessary information
     *
     * @param output
     * @return
     */
    private static ResponseMessage buildResponseObj(String output){
        Gson gson = new Gson();
        ResponseMessage responseMsg = gson.fromJson(output, ResponseMessage.class);
        return responseMsg;
    }

}
