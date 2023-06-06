import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import org.bson.Document;


public class RequestHandler {

    public String postMethod() throws Exception{
        String URL = "http://localhost:8100/prompts/";
        URL url = new URL(URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        OutputStreamWriter out = new OutputStreamWriter(
              conn.getOutputStream()
        );


        Document d = new Document("yo mama", "so fat");
        d.append("she really", "fat");
        String s =  d.toJson();
        out.write(s);
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

    public String SetupEmailToJSON(String prefferedName, String SMTPHost, String SMTPPort, String emailPassword){
        //Document d = new Document( "", );
        //TODO: check what we need for setup email MORE FIELDS
        return "";
    }
}
