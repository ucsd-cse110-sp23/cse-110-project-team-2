
// gui libraries
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;



class TempPanel extends JPanel {
    private JButton startButton;
    private JButton stopButton;
    private JLabel recordingLabel;
    TempPanel(){
        startButton = new JButton("Start");
        this.add(startButton);

        stopButton = new JButton("Stop");
        this.add(stopButton); 
        recordingLabel = new JLabel("Recording");
        recordingLabel.setForeground(Color.RED);
        recordingLabel.setPreferredSize(new Dimension(20, 20));
        recordingLabel.setVisible(false);
        this.add(recordingLabel);

    }

    public JButton getStartButton(){
        return startButton;
    }

    public JButton getStopButton(){
        return stopButton;
    }

    public void hideRecording(){
        recordingLabel.setVisible(false);
    }
    public void showRecording(){
        recordingLabel.setVisible(true);
    }

}


class AppFrame extends JFrame {

    private AudioHandler audioHandler;

    private JButton startButton;
    private JButton stopButton;
    
    private TempPanel tempPanel;

    AppFrame() {
        setTitle("Application");
        setLayout(new BorderLayout());
        this.setSize(600, 1200); // 400 width and 600 height
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit
        this.setVisible(true); // Make visible

        audioHandler = new AudioHandler();

        tempPanel = new TempPanel();
        this.add(tempPanel);

        startButton = tempPanel.getStartButton();
        stopButton = tempPanel.getStopButton();
        addListeners();
    }

    

    public void addListeners() {
        startButton.addActionListener(
          (ActionEvent e) -> {
              System.out.println("START PRESSED");
              audioHandler.startRecording();
              tempPanel.showRecording();
          }
        );
        stopButton.addActionListener(
        (ActionEvent e) -> {
            System.out.println("STOP PRESSED");
            audioHandler.stopRecording();
            tempPanel.hideRecording();
          }
        );
    }
}



public class App {
    public static void main(String[] args) throws Exception {
        new AppFrame();
    }
}
