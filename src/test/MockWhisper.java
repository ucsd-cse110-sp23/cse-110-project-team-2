import java.io.File;

/**
 * Mock Whisper API, do not waste API credits.
 * "Hard Code" a response from transcribeAudio
 */
public class MockWhisper {
    private String transcription;
    
    public MockWhisper(String transcription){
        this.transcription = transcription;
    }

    
    public String transcribeAudio(File Audio){
        return transcription;
    }

    public String getTranscription(){
        return transcription;
    }
}
