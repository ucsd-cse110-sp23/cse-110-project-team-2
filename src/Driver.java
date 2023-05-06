import java.io.File;
public class Driver {
    public static void main(String[] args){
        WhisperHandler whisperHandler = new WhisperHandler("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");
        GPTHandler gptHandler = new GPTHandler("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");

        try{
            File audio = new File("./untitled.wav");
            String transcription = whisperHandler.transcribeAudio(audio);
            String res = gptHandler.askQuestion(transcription);
            
            System.out.println("Transcription, " + transcription);
            System.out.println(res);
        } catch(Exception e){
            System.out.println(e);
        }
    }
}
