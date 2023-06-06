import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.bson.Document;
import org.json.*;

public class PromptRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;

    /**
     * Expected to set the dbHandler using a setter before a handleRequest method is called. 
     */
    public PromptRequestHandler() { }

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

        try{
            if(method.equals("GET")){
                response = handleGet(httpExchange);
            } else if (method.equals("POST")){
                response = handlePost(httpExchange);
            } else if (method.equals("DELETE")){
                response = handleDelete(httpExchange);
            } //else if (method.equals("PUT")){
                //response = handlePut(httpExchange);
            //} 
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
     * @returns If user exists in database, <username>'s prompt history in JSON array formatted string. 
     *          Each element is a JSON object that represents each prompt. 
     *          Otherwise, null returned. 
     */
    private String handleGet(HttpExchange httpExchange) throws IOException {
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");

        List<Document> promptList = dbHandler.getAllUserPrompts(username);

        if (promptList != null) return (new JSONArray(promptList)).toString();
        else                    return null;
    }   

    /**
     * Adds a prompt to the document associated with the given user. 
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus", 
     *                              "promptType": "Send Email", 
     *                              "question": "Send email to shitass@ucsd.edu", 
     *                              "answer": "SMTP Error"}
     * @returns If user exists in database, id of created prompt is returned. 
     *          Otherwise, null returned. 
     */
    private String handlePost(HttpExchange httpExchange) throws IOException {
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");
        String promptType = jsonRequest.getString("promptType");
        String question = jsonRequest.getString("question");
        String answer = jsonRequest.getString("answer");

        String promptID = dbHandler.addPrompt(username, promptType, question, answer);
        return promptID; // addPrompt returns null if user not found
    }

    /**
     * Deletes a prompt based on given ID and given username.
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus", 
     *                              "promptID": "507c7f79bcf86cd7994f6c0e"}
     * @require <username> exists in database
     * @returns If prompt exists in database, return id. 
     *          Otherwise, return null
     */
    private String handleDelete(HttpExchange httpExchange) throws IOException {
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");
        String promptID = jsonRequest.getString("promptID");

        if (dbHandler.deletePrompt(username, promptID)) return promptID;
        else                                            return null;
    }
}

