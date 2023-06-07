import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class SetupEmailRequestHandler implements HttpHandler {

    private MongoHandler dbHandler;

    public SetupEmailRequestHandler(MongoHandler dbHandler) {
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

    // Add method for adding new user info? 

 
    private String handlePost(HttpExchange httpExchange) throws IOException {
        //check if the provided username and password match with a user
        return "foo";
    }   
 
    private String handlePut(HttpExchange httpExchange) throws IOException {
        //check if the provided username and password match with a user
        return "foo";
    }   
}

