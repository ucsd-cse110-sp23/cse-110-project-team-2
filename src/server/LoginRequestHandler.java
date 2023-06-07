import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.json.JSONObject;


public class LoginRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;

    public LoginRequestHandler(MongoHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        String response = "request received";
        System.out.println("METHOD IS" + method);

        try{
            if(method.equals("POST")){
                response = handlePost(httpExchange);
            } else if (method.equals("PUT")){
                response = handlePut(httpExchange);
            } else {
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

    // Add method for adding new user info? 
    private String handlePost(HttpExchange httpExchange) throws IOException {
        System.out.println("prompt GET request recieved");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);

        System.out.println(jsonRequest.toString());

        String username = jsonRequest.getString("username");
        String password = jsonRequest.getString("password");

        Document user = dbHandler.getUser(username);
        JSONObject errorResponse = new JSONObject();
        errorResponse.append("status", 404);


        if(user == null){
            errorResponse.append("error","no such user was found");
            return errorResponse.toString();
        } else if (user.get("password").toString().equals(password)) {
            return new JSONObject(user.toJson()).toString();
        } else {
            errorResponse.append("error", "passwords do not match");
            return errorResponse.toString();
        }
    }   

    //Create a user
    private String handlePut(HttpExchange httpExchange) throws IOException {
        System.out.println("prompt PUT request recieved");
        Scanner scnr = new Scanner(httpExchange.getRequestBody());
        String requestBody = scnr.nextLine(); 
        scnr.close();
        
        JSONObject jsonRequest = new JSONObject(requestBody);

        System.out.println(jsonRequest.toString());

        String username = jsonRequest.getString("username");
        String password = jsonRequest.getString("password");
        Document user = dbHandler.getUser(username);


        JSONObject errorResponse = new JSONObject();
        errorResponse.append("status", 404);

        //user DNE
        if(user == null){

            dbHandler.addUser(username,password);

            JSONObject successResponse = new JSONObject();
            successResponse.append("status",200);
            return successResponse.toString();
        } else {
            //A user with the username already exists
            errorResponse.append("error","A user with that username already exists");
            return errorResponse.toString();
        }
    }   
}

