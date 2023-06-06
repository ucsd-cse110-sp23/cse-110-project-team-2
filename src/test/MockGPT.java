import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
//import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Used for mocking ChatGPT question-asking
 */
public class MockGPT {
    private String[] answerSet;
    private int questionIdx;
    private static final String API_ENDPOINT = "https://api.openai.com/v1/completions";
    String MOCK_API_KEY = "f90q324j0j4359f90w";
    private static final String MODEL = "text-davinci-003";
    private static final int MAX_TOKENS = 100; 


    //
    public MockGPT(String[] answerSet) {
        this.answerSet = answerSet;
        this.questionIdx = 0;
    }

    public String askQuestion(String str){
        String ret = answerSet[questionIdx];
        questionIdx = (questionIdx + 1) % answerSet.length;

        return ret;
    }

    /**
     * Builds the HTTPRequest for the ChatGPT API
     * @param prompt - the question to ask ChatGPT
     * @return
     */
    // TODO: handle exception elsewhere
    public HttpRequest buildGPTHttpRequest(String prompt){ // temporary exception
        //Create a request body which you will pass into request object
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", MODEL);
        requestBody.put("prompt", prompt);
        requestBody.put("max_tokens", MAX_TOKENS);
        requestBody.put("temperature", 1.0);

        HttpClient client = HttpClient.newHttpClient();

        //Create the request object
        try{
            HttpRequest request = HttpRequest
            .newBuilder()
            .uri(new URI(API_ENDPOINT))
            .header("Content-Type", "application/json")
            .header("Authorization", String.format("Bearer %s", MOCK_API_KEY))
            .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
            .build();
    
            return request; 

        }catch(URISyntaxException e){
            return null;
        }
    }

}
