import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.json.*;
import org.bson.Document;

public class LoginRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;
    private String TRUE = "true", FALSE = "false";


    public LoginRequestHandler(MongoHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String response = "Request received";
        String method = httpExchange.getRequestMethod();

        try{
            if(method.equals("GET")){
                System.out.println("We dont have a get method for this endpoint you trolling");
                //response = //handleGet(httpExchange);
            } else if (method.equals("POST")){
                response = handlePost(httpExchange);
            } else if (method.equals("DELETE")){
                System.out.println("We dont have a delete method for this endpoint you trolling");
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

        httpExchange.sendResponseHeaders(200, response.length());
        OutputStream outStream = httpExchange.getResponseBody();
        outStream.write(response.getBytes());
        outStream.close();
    } 

    
    /**
     * Adds a user document given user credentials. 
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus", 
     *                              "password": "sus"}
     * @returns statusMessage syntax: if successful, {"status": "true", "message": "blahblah"}
     *                                otherwise, {"status": "false", "message": "Some error"}
     */
    private String handlePost(HttpExchange httpExchange) throws IOException {
        System.out.println("Login POST request recieved.");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();

        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");
        String password = jsonRequest.getString("password");

        String status = "";
        String message = "";

        if (dbHandler.getUser(username) == null) {
            boolean userAdded = dbHandler.addUser(username, password);
            if (userAdded) {
                status = TRUE;
                message = "User " + username + " was successfully added.";
            }
            else {
                status = FALSE;
                message = "Username "+username+" was valid but adding user in database went wrong.";
            }
        }
        else {
            status = FALSE;
            message = "User "+username+" already exists in database.";
        }

        log(message);
        return RequestHandler.statusMessageToJSON(status, message);
    }   

    /**
     * Compare credentials. 
     * @param httpExchange Contains the request bodyString
     * @require bodyString syntax: {"username": "amongus", 
     *                              "password": "sus"}
     * @returns statusMessage syntax: if successful, {"status": "true", "message": "blahblah"}
     *                                otherwise, {"status": "false", "message": "Some error"}
     */
    private String handlePut(HttpExchange httpExchange) throws IOException {
        System.out.println("Login PUT request recieved.");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();

        JSONObject jsonRequest = new JSONObject(requestBody);
        String username = jsonRequest.getString("username");
        String password = jsonRequest.getString("password");

        Document userDoc = dbHandler.getUser(username);

        String status = "";
        String message = "";

        if (userDoc != null) {
            JSONObject userJson = new JSONObject(userDoc);
            String dbPassword = userJson.getString("password");
            if (password.equals(dbPassword)) {
                message = "User " + username + " successfully logged in.";
                status = TRUE;
            }
            else {
                message = "Password incorrect for " + username + ".";
                status = FALSE;
            }
        }
        else {
            message = "User " + username + " not found.";
            status = FALSE;
        }

        log(message);
        return RequestHandler.statusMessageToJSON(status, message);
    }   
    
    private static void log(String msg) {
        System.out.println(msg);
    }
}

