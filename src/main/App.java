
// gui libraries
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.geom.RoundRectangle2D;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;



class TempPanel extends JPanel {
    private JButton startButton;
    private JButton stopButton;
    private JLabel recordingLabel;

    TempPanel(){
        startButton = new JButton();
        this.add(startButton);
        startButton.setIcon(new ImageIcon("icons8-start-24.png"));
       
        stopButton = new JButton("Stop");
        this.add(stopButton);
        
        recordingLabel = new JLabel("Recording");
        recordingLabel.setForeground(Color.RED);
        recordingLabel.setPreferredSize(new Dimension(20, 20));
        recordingLabel.setVisible(false);
        this.add(recordingLabel);
        revalidate();
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


class PromptHistory extends JPanel {
    JList<String> jList;
    Color backgroundColor = new Color(240, 248, 255);

    PromptHistory() {
        GridLayout layout = new GridLayout(10, 1);
        FlowLayout lay = new FlowLayout(FlowLayout.LEFT, 5, 5);
        layout.setVgap(5); // Vertical gap
        this.setLayout(lay);

        this.setPreferredSize(new Dimension(400, 560));
        this.setBackground(backgroundColor);
        loadTasks();
      }

      public void loadTasks() {
        Vector<String> tasks = new Vector<>();
        try {
            FileReader file = new FileReader("tasks.txt");
            BufferedReader reader = new BufferedReader(file);
            String temp = reader.readLine();
            while (temp != null) {
                tasks.add(temp); // add to array list
                temp = reader.readLine();
            }
            jList = new JList<>(tasks);
            file.close();
            reader.close();
        } catch (Exception e) {
          System.err.println("File not found");
        }
        this.add(jList);
        revalidate();
      }
}


class AppFrame extends JFrame {

    private AudioHandler audioHandler;

    private JButton startButton;
    private JButton stopButton;
    
    private TempPanel tempPanel;
    private PromptHistory promptHistory;

    AppFrame() {
        setTitle("Application");
        setLayout(new BorderLayout());
        this.setSize(1000, 1000); // 400 width and 600 height
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit
        this.setVisible(true); // Make visible

        audioHandler = new AudioHandler();

        tempPanel = new TempPanel();
        this.add(tempPanel, BorderLayout.SOUTH);

        promptHistory = new PromptHistory();
        this.add(promptHistory, BorderLayout.WEST);

        startButton = tempPanel.getStartButton();
        stopButton = tempPanel.getStopButton();
        addListeners();
        revalidate();
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
