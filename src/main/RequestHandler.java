import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.bson.Document;


public class RequestHandler {

    public String sendHttpRequest(String bodyString, String httpMethod, String path) throws Exception{
        String URL = "http://localhost:8100/" + path;
        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(httpMethod);
        conn.setDoOutput(true);
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

    
    public String QNAToJSON(QNA qna){
        Document d = new Document("promptType", qna.getPromptType().toString())
            .append("question", qna.getQuestion())
            .append("answer", qna.getAnswer());
        return d.toJson();
    }
    
    public String CredentionalsToJSON(String username, String password){
        Document d = new Document( "username", username)
            .append("password", password);
        return d.toJson();
    }

    public String SetupEmailToJSON(String firstName, String lastName, String displayName, String smtpHost, String smtpPort, String email, String emailPassword){
        Document d = new Document( "firstName", firstName)
            .append("lastName", lastName)
            .append("displayName", displayName)
            .append("smtpHost", smtpHost)
            .append("smtpPort", smtpPort)
            .append("email", email)
            .append("emailPassword", emailPassword);
        //TODO: check what we need for setup email MORE FIELDS
        return d.toJson();
    }
}
