
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONObject;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.junit.jupiter.api.Assertions.assertFalse;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class appTest {

    /*
     * Setup mock questions and answers
     * retrieve answer and try saving to prompt history
     */

     GPTHandler gpt;
     WhisperHandler wh;
     String[] answerSet = {"42", "The meaning of life is 42", "42 is the meaning of life"};
     MockGPT mockGPT = new MockGPT(answerSet);
     MockWhisper mockWhisper = new MockWhisper("What is the meaning of life?");
     private MockAPIHandler mockAPIHandler = new MockAPIHandler();
     private static final String API_ENDPOINT = "https://api.openai.com/v1/completions";
     String MOCK_API_KEY = "f90q324j0j4359f90w";
     private static final String MODEL = "text-davinci-003";
     private static final int MAX_TOKENS = 100; 

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
        String expectedQuestion = "What is the meaning of life?";
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
        String historyFilePath = "testhistory.txt";
        HistoryManager historyManager = new HistoryManager(historyFilePath);

        // Create a sample QNA
        QNA sampleQNA = new QNA("Sample Answer", "Sample Question");

        // Add the sample QNA to the history
        Prompt addedPrompt = historyManager.addToHistory(sampleQNA);

        // Get the history list
        ArrayList<Prompt> historyList = historyManager.getHistoryList();

        // Ensure the prompt was added to the history list
        assertTrue(historyList.contains(addedPrompt));

        // Cleanup: Delete the file created during the test
        File historyFile = new File(historyFilePath);
        historyFile.delete();
    }

    @Test
    public void testDeleteFromHistory() {
        String historyFilePath = "testhistory.txt";
        HistoryManager historyManager = new HistoryManager(historyFilePath);

        // Create sample QNAs
        QNA qna1 = new QNA("Answer 1", "Question 1");
        QNA qna2 = new QNA("Answer 2", "Question 2");
        QNA qna3 = new QNA("Answer 3", "Question 3");

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

        // Cleanup: Delete the file created during the test
        File historyFile = new File(historyFilePath);
        historyFile.delete();
    }
}
    



