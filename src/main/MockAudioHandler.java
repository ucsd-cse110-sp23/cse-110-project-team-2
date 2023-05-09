
import java.io.*;
import javax.sound.sampled.*;

public class MockAudioHandler {

    private AudioFormat audioFormat;
    private TargetDataLine targetDataLine;

    public MockAudioHandler(){
        audioFormat = getAudioFormat();
    }

    /*
     * Taken from Lab 5 Exercise 4
     */
    private AudioFormat getAudioFormat() {
        // the number of samples of audio per second.
        // 44100 represents the typical sample rate for CD-quality audio.
        float sampleRate = 44100;
    
        // the number of bits in each sample of a sound that has been digitized.
        int sampleSizeInBits = 16;
    
        // the number of audio channels in this format (1 for mono, 2 for stereo).
        int channels;
        if (System.getProperty("os.name").equals("Mac OS X")) {
          //Mac OS X does not support stereo audio so we only use one audio channel
          channels = 1;
        }
        else {
          channels = 2;
        }
        // whether the data is signed or unsigned.
        boolean signed = true;
    
        // whether the audio data is stored in big-endian or little-endian order.
        boolean bigEndian = false;
    
        return new AudioFormat(
          sampleRate,
          sampleSizeInBits,
          channels,
          signed,
          bigEndian
        );
    }


    /*
     * Taken from Lab 5 Exercise 4
     */
    public void startRecording() {
        Thread recordingThread = new Thread(
          () -> {
            try {
;  
              // the format of the TargetDataLine
            DataLine.Info dataLineInfo = new DataLine.Info(
                TargetDataLine.class,
                audioFormat
              );
              // the TargetDataLine used to capture audio data from the microphone
              targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);
              targetDataLine.open(audioFormat);
              targetDataLine.start();
        
              // the AudioInputStream that will be used to write the audio data to a file
              AudioInputStream audioInputStream = new AudioInputStream(targetDataLine);
        
              // the file that will contain the audio data
              File audioFile = new File("testrecording.wav");
              AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
            } catch (Exception ex) {
              ex.printStackTrace();
            }
          }
        );
        recordingThread.start();
    }


    /*
     * Taken from Lab 5 Exercise 4
     */
    public void stopRecording() {
        targetDataLine.stop();
        targetDataLine.close();
      }



    
}
