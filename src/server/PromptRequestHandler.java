import com.sun.net.httpserver.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class PromptRequestHandler implements HttpHandler {

    

    public PromptRequestHandler() {
    }

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

    private String handleGet(HttpExchange httpExchange) throws IOException {
        //TODO get a user's prompts
        return "prompt";
    }   
    private String handlePost(HttpExchange httpExchange) throws IOException {
        //TODO add a new prompt to a user
        return "prompt";
    }
    private String handleDelete(HttpExchange httpExchange) throws IOException {
        //TODO delete a user's prompts
        return "prompt";
    }
    private String handlePut(HttpExchange httpExchange) throws IOException {
        //TODO change a user's prompt?
        return "prompt";
    }
}

