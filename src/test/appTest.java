
import java.net.http.HttpRequest;
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

    // @Test
    // public void testRecordingAndStopping() throws InterruptedException {
    //     MockAudioHandler audioHandler = new MockAudioHandler();
    //     audioHandler.startRecording();
    //     Thread.sleep(5000); // record for 5 seconds
    //     audioHandler.stopRecording();
    //     assertTrue(new File("testrecording.wav").exists());
    // }

    // @Test
    // public void testAudioToAnswer() {
    //     // Create a mock API handler
    //     MockAPIHandler mockAPIHandler = new MockAPIHandler();

    //     // Set the expected question and answer
    //     String expectedQuestion = "What is the meaning of life?";
    //     String[] expectedAnswers = {"42", "The meaning of life is 42", "42 is the meaning of life"};

    //     // Record audio and convert to QNA object
    //     mockAPIHandler.startRecording();
    //     QNA qna = mockAPIHandler.audioToAnswer();
    //     mockAPIHandler.stopRecording();

    //     // Assert that the returned QNA object contains the expected question and one of the expected answers
    //     assertEquals(expectedQuestion, qna.getQuestion());
    //     assertTrue(Arrays.asList(expectedAnswers).contains(qna.getAnswer()));
    // }
    
}


