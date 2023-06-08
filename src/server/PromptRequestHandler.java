import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.bson.Document;
import org.json.*;

public class PromptRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;
    private String TRUE = "true", FALSE = "false";

    /**
     * Expected to set the dbHandler using a setter before a handleRequest method is called. 
     */
    public PromptRequestHandler(MongoHandler dbHandler) {
        this.dbHandler = dbHandler;
     }

    /**
     * Setter for dbHandler. 
     * @param dbHandler 
     */
    public void setDbHandler(MongoHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    /**
     * Calls respective handle methods based on http request method contained in httpExchange. 
     * @param httpExchange Contains the requestMethod and context of request operation. 
     *                     The response header is sent through with this object. 
     */
    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "Request received";
        String method = httpExchange.getRequestMethod();
        System.out.print(method);
        try{
            if(method.equals("GET")){
                System.out.println("We dont have a get endpoint you trolling");
                //response = //handleGet(httpExchange);
            } else if (method.equals("POST")){
                response = handlePost(httpExchange);
            } else if (method.equals("DELETE")){
                response = handleDelete(httpExchange);
            } else if (method.equals("PUT")){
                response = handlePut(httpExchange);
            } 
            else {
                throw new Exception("INVALID REQUEST");
            }
        }catch(Exception e){
            System.out.println("oopsies");
            e.printStackTrace();
        }

        // Handle response is here
        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream outStream = httpExchange.getResponseBody();
        outStream.write(response.getBytes());
        outStream.close();
    } 

    /**
     * Gets the given user's prompt history. 
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus"}
     * @returns statusMessage syntax: if successful, {"status": "true", "message": "blahblah"}
     *                                otherwise, {"status": "false", "message": "Some error"}
     * @returns message contains JSON array of user prompt history if successful
     */
    private String handlePut(HttpExchange httpExchange) throws IOException {
        System.out.println("prompt PUT request recieved");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");

        List<Document> promptList = dbHandler.getAllUserPrompts(username);

        String status = "";
        String message = "";
        
        if (promptList != null) {
            message = (new JSONArray(promptList)).toString();
            status = TRUE;
        } 
        else {
            message = "User "+username+" not found.";
            status = FALSE;
        }

        return RequestHandler.statusMessageToJSON(status, message);
    }   

    /**
     * Adds a prompt to the document associated with the given user. 
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus", 
     *                              "promptType": "Send Email", 
     *                              "question": "Send email to shitass@ucsd.edu", 
     *                              "answer": "SMTP Error"}
     * @returns statusMessage syntax: if successful, {"status": "true", "message": "blahblah"}
     *                                otherwise, {"status": "false", "message": "Some error"}
     * @returns message contains promptID of inserted prompt if successful
     */
    private String handlePost(HttpExchange httpExchange) throws IOException {
        System.out.println("prompt POST request recieved");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();

        
        
        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");
        String promptType = jsonRequest.getString("promptType");
        String question = jsonRequest.getString("question");
        String answer = jsonRequest.getString("answer");

        String status = "";
        String message = "";

        String promptID = dbHandler.addPrompt(username, promptType, question, answer);
        if (promptID == null) {
            status = FALSE;
            message = "User " + username + " not found.";
        }
        else {
            status = TRUE;
            message = promptID;
        }

        return RequestHandler.statusMessageToJSON(status, message);
    }

    /**
     * Deletes a prompt based on given ID and given username.
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus", 
     *                              "promptID": "507c7f79bcf86cd7994f6c0e"}
     * @require <username> exists in database
     * @returns statusMessage syntax: if successful, {"status": "true", "message": "blahblah"}
     *                                otherwise, {"status": "false", "message": "Some error"}
     * @returns message contains prompt id if successful
     */
    private String handleDelete(HttpExchange httpExchange) throws IOException {
        System.out.println("prompt DELETE request recieved");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");
        String promptID = jsonRequest.getString("promptID");

        String status = "";
        String message = "";

        if (dbHandler.deletePrompt(username, promptID)) {
            status = TRUE;
            message = promptID;
        }
        else {
            status = FALSE;
            message = "User " + username + " not found.";
        }

        return RequestHandler.statusMessageToJSON(status, message);
    }
}

