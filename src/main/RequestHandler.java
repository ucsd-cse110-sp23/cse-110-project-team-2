import java.io.*;
import java.net.*;
import org.bson.Document;


public class RequestHandler {

    /**
     * Generic method to send httpRequests to the local 
     * @param bodyString the body of the request. The body should be generated using the methods below
     * @param httpMethod the method (GET, POST, PUT, DELETE) as a string
     * @param path the path, either "login" for login and account creation related queries, or "prompts"
     * @return the string request body (format is undecided as of now, we will figure it out im sure)
     * @throws Exception
     */
    public String sendHttpRequest(String bodyString, String httpMethod, String path) throws Exception{
        String URL = "http://localhost:8100/" + path + "/";
        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod);
        if (!httpMethod.equals("GET")){
            conn.setDoOutput(true);
        }
        else{
            System.out.println("This is not supposed to be used for get requests lmao");
        }
        
        OutputStreamWriter out = new OutputStreamWriter(
              conn.getOutputStream()
        );
        out.write(bodyString);
        out.flush();
        out.close();
        BufferedReader in = new BufferedReader(
            new InputStreamReader(conn.getInputStream())
        );
        String response = in.readLine();
        in.close();
        return response;
    }

    
    public String QNAToJSON(String username, QNA qna){
        Document d = new Document("username", username)
            .append("promptType", qna.getPromptType().toString())
            .append("question", qna.getQuestion())
            .append("answer", qna.getAnswer());
        return d.toJson();
    }
    
    public String CredentionalsToJSON(String username, String password){
        Document d = new Document( "username", username)
            .append("password", password);
        return d.toJson();
    }

    public String DeleteToJSON(String username, String id){
        Document d = new Document("username", username)
            .append("promptID", id);
        return d.toJson();
    }

    public String UsernameToJSON(String username){
        Document d = new Document("username", username);
        return d.toJson();
    }

    public String SetupEmailToJSON(String username, String firstName, String lastName, 
            String displayName, String smtpHost, String smtpPort, String email, String emailPassword){
        Document d = new Document("username", username)
            .append( "firstName", firstName)
            .append("lastName", lastName)
            .append("displayName", displayName)
            .append("smtpHost", smtpHost)
            .append("smtpPort", smtpPort)
            .append("email", email)
            .append("emailPassword", emailPassword);
        //TODO: check what we need for setup email MORE FIELDS
        return d.toJson();
    }

    public static String statusMessageToJSON(String status, String message) {
        Document d = new Document("status", status)
                          .append("message", message);
        return d.toJson();
    }
}
