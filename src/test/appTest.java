
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.json.JSONException;
import org.json.JSONObject;

import org.junit.jupiter.api.Test;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;

public class appTest {

    /*
     * Setup mock questions and answers
     * retrieve answer and try saving to prompt history
     */

     MockMail mockMail = new MockMail(new Mail());
     String[] answerSet = {"42", "The meaning of life is 42", "42 is the meaning of life"};
     MockGPT mockGPT = new MockGPT(answerSet);
     MockWhisper mockWhisper = new MockWhisper("question What is the meaning of life?");
     MockAPIHandler mockAPIHandler = new MockAPIHandler();
     private static final String API_ENDPOINT = "https://api.openai.com/v1/completions";
     String MOCK_API_KEY = "f90q324j0j4359f90w";
     private static final String MODEL = "text-davinci-003";
     private static final int MAX_TOKENS = 100; 
     public static HttpServer server;

     @BeforeAll
        public static void setup() throws IOException {
            // Start the server
            server = SayItServer.runServer();
        }
     @AfterAll
        public static void teardown() {
            // Stop the server
            SayItServer.stopServer(server);
        }

    @Test
    public void testTranscribeAudio() {
        // Create a test audio file
        File audioFile = new File("test_audio.wav");

        // Call the method being tested
        String actualTranscription = mockWhisper.transcribeAudio(audioFile);

        // Assert that the returned transcription is correct
        assertEquals(mockWhisper.getTranscription(), actualTranscription);
    }

    @Test
    public void testBuildGPTHttpRequest() {
        // Create a test prompt
        String prompt = "This is a test prompt.";

        // Call the method being tested
        HttpRequest httpRequest = mockGPT.buildGPTHttpRequest(prompt);

        // Assert that the HTTP request is not null
        assertNotNull(httpRequest);

        // Assert that the HTTP request method is POST
        assertEquals("POST", httpRequest.method());

        // Assert that the HTTP request URI is the API endpoint
        assertEquals(API_ENDPOINT, httpRequest.uri().toString());

        // Assert that the HTTP request headers are set correctly
        assertEquals("application/json", httpRequest.headers().firstValue("Content-Type").orElse(null));
        assertEquals("Bearer " + MOCK_API_KEY, httpRequest.headers().firstValue("Authorization").orElse(null));

        // Assert that the HTTP request body is not empty
        assertFalse(httpRequest.bodyPublisher().isEmpty());
    }

    @Test
    public void testAskQuestion() {
        // Create a MockGPT instance with a set of predefined answers
        String[] answerSet = {"Answer 1", "Answer 2", "Answer 3"};
        MockGPT mockGPT = new MockGPT(answerSet);

        // Test asking three questions and checking the responses
        assertEquals("Answer 1", mockGPT.askQuestion("Question 1"));
        assertEquals("Answer 2", mockGPT.askQuestion("Question 2"));
        assertEquals("Answer 3", mockGPT.askQuestion("Question 3"));

        // Test asking a fourth question (wraps around to the first answer)
        assertEquals("Answer 1", mockGPT.askQuestion("Question 4"));
    }

    // THIS TEST ONLY WORKS ON LOCAL MACHINES WITH MICROPHONES
    // THIS WILL NOT WORK ON THE CI/CD PIPELINE
    // @Test
    // public void testRecordingAndStopping() throws InterruptedException {
    //     MockAudioHandler audioHandler = new MockAudioHandler();
    //     audioHandler.startRecording();
    //     Thread.sleep(5000); // record for 5 seconds
    //     audioHandler.stopRecording();
    //     assertTrue(new File("testrecording.wav").exists());
    // }

    @Test
    public void testUserStory1() throws InterruptedException {
        // Create a mock API handler
        MockAPIHandler mockAPIHandler = new MockAPIHandler();

        // Set the expected question and answer
        String expectedQuestion = "question What is the meaning of life?";
        String[] expectedAnswers = {"42", "The meaning of life is 42", "42 is the meaning of life"};

        // Record audio and convert to QNA object
        mockAPIHandler.startRecording();
        Thread.sleep(3000);
        mockAPIHandler.stopRecording();
        QNA qna = mockAPIHandler.audioToAnswer();

        // Assert that the returned QNA object contains the expected question and one of the expected answers
        assertEquals(expectedQuestion, qna.getQuestion());
        assertTrue(Arrays.asList(expectedAnswers).contains(qna.getAnswer()));
    }
    
    @Test
    public void testAddToHistory() {
        HistoryManager historyManager = new HistoryManager( "testusername");

        // Create a sample QNA
        QNA sampleQNA = new QNA("Sample Answer", "Sample Question", PromptType.QUESTION);

        // Add the sample QNA to the history
        Prompt addedPrompt = historyManager.addToHistory(sampleQNA);

        // Get the history list
        ArrayList<Prompt> historyList = historyManager.getHistoryList();

        // Ensure the prompt was added to the history list
        assertTrue(historyList.contains(addedPrompt));

    }

    @Test
    public void testDeleteFromHistory() {
        HistoryManager historyManager = new HistoryManager("testusername");

        // Create sample QNAs
        QNA qna1 = new QNA("Answer 1", "Question 1", PromptType.QUESTION);
        QNA qna2 = new QNA("Answer 2", "Question 2", PromptType.QUESTION);
        QNA qna3 = new QNA("Answer 3", "Question 3", PromptType.QUESTION);

        // Add sample QNAs to the history
        historyManager.addToHistory(qna1);
        Prompt promptToDelete = historyManager.addToHistory(qna2);
        historyManager.addToHistory(qna3);

        // Delete the prompt from the history
        historyManager.deleteFromHistory(promptToDelete);

        // Get the updated history list
        ArrayList<Prompt> historyList = historyManager.getHistoryList();

        // Ensure the prompt was removed from the history list
        assertFalse(historyList.contains(promptToDelete));
    }

    @Test
    public void testClearHistory() throws IOException {
        HistoryManager historyManager = new HistoryManager("testusername");

        // Create sample QNAs
        QNA qna1 = new QNA("Answer 1", "Question 1", PromptType.QUESTION);
        QNA qna2 = new QNA("Answer 2", "Question 2", PromptType.QUESTION);
        QNA qna3 = new QNA("Answer 3", "Question 3", PromptType.QUESTION);

        // Add sample QNAs to the history
        historyManager.addToHistory(qna1);
        historyManager.addToHistory(qna2);
        historyManager.addToHistory(qna3);

        // Clear the history
        historyManager.clearHistory();

        // Get the updated history list
        ArrayList<Prompt> historyList = historyManager.getHistoryList();

        // Ensure the history list is empty
        assertTrue(historyList.isEmpty());

        // Ensure the selected prompt is null
        assertNull(historyManager.getSelectedToDelete());
    }

    @Test
    public void testUserStory2() {
        
    }

    @Test
    public void testUserStory3() {
        HistoryManager historyManager = new HistoryManager("testusername");

        // Create sample QNAs
        QNA sampleQNA = new QNA("Sample Answer", "Sample Question", PromptType.QUESTION);
        QNA qna1 = new QNA("Answer 1", "Question 1", PromptType.QUESTION);
        QNA qna2 = new QNA("Answer 2", "Question 2", PromptType.QUESTION);
        QNA qna3 = new QNA("Answer 3", "Question 3", PromptType.QUESTION);

        // Add the sample QNA to the history
        Prompt addedPrompt = historyManager.addToHistory(sampleQNA);

        // Add sample QNAs to the history
        historyManager.addToHistory(qna1);
        Prompt promptToDelete = historyManager.addToHistory(qna2);
        historyManager.addToHistory(qna3);

        // Get the history list
        ArrayList<Prompt> historyList = historyManager.getHistoryList();

        // Ensure the prompt was added to the history list
        assertTrue(historyList.contains(addedPrompt));

        // Delete the prompt from the history
        historyManager.deleteFromHistory(promptToDelete);

        // Get the updated history list
        historyList = historyManager.getHistoryList();

        // Ensure the prompt was removed from the history list
        assertFalse(historyList.contains(promptToDelete));

        // Clear the history
        historyManager.clearHistory();

        // Get the updated history list
        historyList = historyManager.getHistoryList();

        // Ensure the history list is empty
        assertTrue(historyList.isEmpty());

        // Ensure the selected prompt is null
        assertNull(historyManager.getSelectedToDelete());
    }


    // US 4 / 6 tests
    // We test for correct prompt type being parsed and returned
    @Test
    public void testPromptParser() {
        // Test case for empty prompt
        PromptType promptTypeEmpty = mockAPIHandler.promptParser("");
        assertNull(promptTypeEmpty);

        // Test case for question prompt
        PromptType promptTypeQuestion = mockAPIHandler.promptParser("question What is the capital of France?");
        assertEquals(PromptType.QUESTION, promptTypeQuestion);

        // Test case for delete prompt
        PromptType promptTypeDelete = mockAPIHandler.promptParser("delete prompt");
        assertEquals(PromptType.DELETEPROMPT, promptTypeDelete);

        //Test case for setup email
        PromptType promptTypeSetupEmail = mockAPIHandler.promptParser("sEt Up EmAiL.");
        assertEquals(PromptType.SETUPEMAIL, promptTypeSetupEmail);

        // Test case for no command prompt
        /*
        PromptType promptTypeOther = mockAPIHandler.promptParser("blah blah blah");
        */
        PromptType promptTypeOther = mockAPIHandler.promptParser("this should return no command");
      
        assertEquals(PromptType.NOCOMMAND, promptTypeOther);

        // Test case for setup email
        PromptType promptTypeCreateEmail = mockAPIHandler.promptParser("create email to Felix");
        assertEquals(PromptType.CREATEEMAIL, promptTypeCreateEmail);

        PromptType promptTypeSendEmail = mockAPIHandler.promptParser("send email to fpeng@ucsd.edu");
        assertEquals(PromptType.SENDEMAIL, promptTypeSendEmail);

    }

    @Test
    public void testAudioToReply() {
        QNA qna = mockAPIHandler.audioToReply();

        // Assert that the returned QNA object contains the expected question and one of the expected answers
        assertEquals("What is the meaning of life?", qna.getQuestion());
        assertTrue(Arrays.asList("42", "The meaning of life is 42", "42 is the meaning of life").contains(qna.getAnswer()));
    }

    @Test
    public void testSendEmail() {

        // Capture the console output
        
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        ByteArrayOutputStream errContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        mockMail.sendEmail();
        assertEquals("Email Sent Successfully!!", outContent.toString());
        assertEquals("", errContent.toString());
        System.out.flush();
        System.setOut(originalOut);
        System.setErr(originalErr);
        
     }

     @Test
     public void testPromptToEmail(){
        EmailSetupPanel esp = new EmailSetupPanel("fpeng");
        MailSendingHandler msh; 
        MailBuilder mb = new MailBuilder();
        QNA sendEmailQNA = new QNA("send email to fpeng at ucsd.edu", null, PromptType.SENDEMAIL);
        QNA createEmailQNA = new QNA(null, "hello felix, this is a test email. Sincerely, Quandale.", PromptType.CREATEEMAIL);

        esp.setSmtpHostFieldContent("host");
        esp.setSmtpPortFieldContent("port");
        esp.setEmailFieldContent("userEmail");
        esp.setPasswordFieldContent("hunter2");
        
        Mail mail = mb.setSmtpHost("host")
        .setSmtpPort("port")
        .setSenderEmail("userEmail")
        .setEmailPassword("hunter2")
        .setMailbody("hello felix, this is a test email. Sincerely, Quandale.")
        .setSubjectLine("New email from SayIt")
        .setRecipientEmail("fpeng@ucsd.edu")
        .create();

        msh = new MailSendingHandler(createEmailQNA, sendEmailQNA, esp);
        
        //test to see if the mail object is created correctly
        assertEquals(mail.toString(), msh.getMail().toString());
        
     }
    
     @Test
     public void testLogin() {
        RequestHandler rh = new RequestHandler();
        String user = "testusername";
        String pass = "testpassword";
        String invalidUser = "invalidusername";
        String invalidPass = "invalidpassword";
        String loginResponse = null;
        String invalidLoginResponse = null;
        try {
            loginResponse = rh.sendHttpRequest(rh.CredentionalsToJSON(user, pass), "POST", "login");
        }
        catch (Exception e){
            e.printStackTrace();
        }
        
        JSONObject loginResponseJSON = new JSONObject(loginResponse);

        // Test case for correct login
        assertEquals(loginResponseJSON.get("username").toString(), user);
        assertEquals(loginResponseJSON.get("password").toString(), pass);


        try {
            invalidLoginResponse = rh.sendHttpRequest(rh.CredentionalsToJSON(invalidUser, invalidPass), "POST", "login");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        JSONObject invalidLoginResponseJSON = new JSONObject(invalidLoginResponse);

        // Test case for incorrect login
        assertTrue(invalidLoginResponseJSON.has("error"));

     }

     @Test
     public void testCreateAccount() {
        String user = "testusername";
        String pass = "testpassword";

        RequestHandler rh = new RequestHandler();
        String createAccountResponse = null;

        try {
            createAccountResponse = rh.sendHttpRequest(rh.CredentionalsToJSON(user, pass), "PUT", "login");
        }
        catch (Exception e){
            e.printStackTrace();
        }

        JSONObject createAccountResponseJSON = new JSONObject(createAccountResponse);

        // Test for error (duplicate account)
        assertTrue(createAccountResponseJSON.has("error"));

     }

     @Test
     public void testEmailSetup() {
        String user = "testusername";
        // String pass = "testpassword";
        String firstname = "testfirstname";
        String lastname = "testlastname";
        String displayName = "test-kun";
        String smtpHost = "testhost";
        String smtpPort = "testport";
        String email = "testemail";
        String emailPassword = "testemailpassword";

        RequestHandler rh = new RequestHandler();
        String emailSetupResponse = null;
        String retrieveEmail = null;
        JSONObject retrieveEmailJSON = null;

        String SetupEmailJSON = rh.SetupEmailToJSON(user, firstname, lastname, 
                                                    displayName, smtpHost, smtpPort,
                                                    email, emailPassword);


        try {
            emailSetupResponse = rh.sendHttpRequest(SetupEmailJSON, "PUT", "email");
                                                                   
        }
        catch (Exception e){
            e.printStackTrace();
        }

        try {
            retrieveEmail = rh.sendHttpRequest(rh.UsernameToJSON(user), "POST", "email");
            retrieveEmailJSON = new JSONObject(retrieveEmail);
            retrieveEmailJSON.put("username", user);
        } catch (Exception e) {
            e.printStackTrace();   
        }

        assertTrue(retrieveEmailJSON.get("username").toString().equals(user));
        assertTrue(retrieveEmailJSON.get("firstName").toString().equals(firstname));
        assertTrue(retrieveEmailJSON.get("lastName").toString().equals(lastname));
        assertTrue(retrieveEmailJSON.get("displayName").toString().equals(displayName));
        assertTrue(retrieveEmailJSON.get("smtpHost").toString().equals(smtpHost));
        assertTrue(retrieveEmailJSON.get("smtpPort").toString().equals(smtpPort));
        assertTrue(retrieveEmailJSON.get("email").toString().equals(email));
        assertTrue(retrieveEmailJSON.get("emailPassword").toString().equals(emailPassword));
     }
     
}
    



