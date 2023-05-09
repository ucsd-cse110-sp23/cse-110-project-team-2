import java.io.*;
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
import javax.swing.JList;

import java.util.ArrayList;

class RecordPanel extends JPanel {
    private JButton startButton;
    private JButton stopButton;
    private JLabel recordingLabel;
    
    
    
    private Border emptyBorder = BorderFactory.createEmptyBorder();

    

    RecordPanel(){
        this.setPreferredSize(new Dimension(600, 200));
        this.setBackground(Color.YELLOW);

        startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(80, 20));
        this.add(startButton);
        stopButton = new JButton("Stop");
        stopButton.setPreferredSize(new Dimension(80, 20));
        this.add(stopButton); 

        recordingLabel = new JLabel("Recording");
        recordingLabel.setVerticalAlignment(JLabel.CENTER);
        recordingLabel.setForeground(Color.RED);
        recordingLabel.setPreferredSize(new Dimension(100, 20));
        recordingLabel.setVisible(false);
        this.add(recordingLabel);

    }


    //TODO: Move listeners to QNA panel class possibly.
    
    

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

class QnaPanel extends JPanel {

    JButton startButton;
    JButton stopButton;

    //TODO: REFACTOR TO USE THIS STYLE (IMPORTANT)
    QnaDisplay qnaDisplay;
    RecordPanel recordPanel;

    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";

    QnaPanel() {
        this.setPreferredSize(new Dimension(600, 800));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.RED);

        qnaDisplay = new QnaDisplay();
        recordPanel = new RecordPanel();
        this.add(qnaDisplay, BorderLayout.CENTER);
        this.add(recordPanel, BorderLayout.SOUTH);

        startButton = recordPanel.getStartButton();
        stopButton = recordPanel.getStopButton();

        audioHandler = new AudioHandler();
        gptHandler = new GPTHandler(APIKey);
        whisperHandler = new WhisperHandler(APIKey);

        addListeners();
    }


    public void addListeners() {
        startButton.addActionListener(
          (ActionEvent e) -> {
              System.out.println("START PRESSED");
              audioHandler.startRecording();
              recordPanel.showRecording();
          }
        );
        stopButton.addActionListener(
        (ActionEvent e) -> {
            System.out.println("STOP PRESSED");
            audioHandler.stopRecording();
            recordPanel.hideRecording();
            //Note: change spot recording is saved
            File newFile = new File("recording.wav");
            String whisperResponse;
            String gptResponse;
            try {
                
                System.out.println("getting API responses...");
                //TODO: maybe multithread?
                whisperResponse = whisperHandler.transcribeAudio(newFile);
                gptResponse = gptHandler.askQuestion(whisperResponse);
                System.out.println(gptResponse);

                QNA gptPrompt = new QNA(whisperResponse, gptResponse);
                System.out.println("response...");
                qnaDisplay.setQNASection(gptPrompt);

                //TODO: save the prompt
                FileWriter history = new FileWriter("history.txt", true);
                history.write(whisperResponse + ",,," + gptResponse + ",,,");
                history.close();
            } catch (Exception exception) {
                System.out.println(exception.getStackTrace());
            }
          }
        );
    }
}

class ContentPanel extends JPanel {

    private String title;
    private String content;
    private TextPanel titlePanel;
    private TextPanel contentPanel;


    ContentPanel(String title, String content, Color titleColor, Color contentColor) {
        this.setPreferredSize(new Dimension(600, 300));
        this.setLayout(new BorderLayout());

        this.title = title;
        this.content = content;

        titlePanel = new TextPanel(title, titleColor);
        contentPanel = new TextPanel(content, contentColor);

        this.add(titlePanel, BorderLayout.NORTH);
        this.add(contentPanel, BorderLayout.CENTER);
    }

    public void setContent(String content){
        this.content = content;
        contentPanel.setText(content);
    }

    public String getContent(){
        return content;
    }

    public void setTitle(String title){
        this.title = title;
        titlePanel.setText(title);
    }


    public String getTitle(){
        return title;
    }
}

class TextPanel extends JPanel {

    private JLabel textLabel;

    TextPanel(String text, Color color) {
        this.setBackground(color);
        textLabel = new JLabel(text);
        textLabel.setHorizontalAlignment(JLabel.CENTER);

        this.add(textLabel);
    }

    //FIX XD
    public void setText(String text){
        textLabel.setText(text);
    }
}

class QnaDisplay extends JPanel {

    //Refactor maybe?
    ContentPanel questionContentPanel;
    ContentPanel answerContentPanel;
    QnaDisplay() {
        this.setPreferredSize(new Dimension(600, 600));
        this.setLayout(new GridLayout(2, 1));
        this.setBackground(Color.GREEN);

        questionContentPanel = new ContentPanel("Question", "MOCK_QUESTION", Color.RED, Color.BLUE);
        answerContentPanel =  new ContentPanel("Answer", "MOCK_ANSWER", Color.CYAN, Color.PINK);
        this.add(questionContentPanel);
        this.add(answerContentPanel);
        
    }

    public void setQNASection(QNA qna){
        questionContentPanel.setContent(qna.getQuestion());
        answerContentPanel.setContent(qna.getAnswer());
        
    }
}

//SHOULD ONLY DISPLAY QUESTIONS
class HistoryList extends JPanel {
    private ArrayList<QNA> qnas;
    private JList<String> jl;
    /*
    //TODO: use ArrayList to initialize JList
    HistoryList(){
        try{
            //Populating qnas ArrayList
            FileReader history = new FileReader("history.txt");
            BufferedReader br = new BufferedReader(history);
            while(br.ready()){
                String q = br.readLine();
                String a = br.readLine(); //Should be no errors with reading two lines at a time in theory
                QNA newQNA = new QNA(q,a);
                qnas.add(newQNA);
            }
        }
        catch(Exception ex){
            System.out.println(exception.getStackTrace());
        }
    }
    */
}

class HistoryPanel extends JPanel {
    private JPanel historyFooter;
    private JPanel historyButtonPanel;
    private JPanel historyList;

    private String[] questionList = {
        "This is an example question",
        "This is another question",
        "question 3",
        "question 4",
        "question 5"
    };

    HistoryPanel() {
        this.setPreferredSize(new Dimension(200, 800));
        this.setBackground(Color.BLUE);
        this.setLayout(new BorderLayout());

        TextPanel headerPanel = new TextPanel("History", Color.LIGHT_GRAY);

        this.add(headerPanel, BorderLayout.NORTH);

        historyList = new JPanel();
        historyList.setPreferredSize(new Dimension(100, 500));
        historyList.setBackground(Color.MAGENTA);
        historyList.add(new JList<String>(questionList));
        this.add(historyList, BorderLayout.CENTER);

        historyButtonPanel = new HistoryButtonPanel();
        this.add(historyButtonPanel, BorderLayout.SOUTH);
    }
}

class HistoryButtonPanel extends JPanel {
    private JButton deleteAll;
    private JButton deleteSingle;

    private JLabel deleteSelected; // Maybe implement when any delete is selected, label shows up
    HistoryButtonPanel() {
        deleteAll = new JButton("Delete All");
        deleteAll.setPreferredSize(new Dimension(80, 20));

        deleteSingle = new JButton("Delete One");
        deleteSingle.setPreferredSize(new Dimension(80, 20));

        this.setPreferredSize(new Dimension(200, 100));
        this.add(deleteAll, BorderLayout.CENTER);
        this.add(deleteSingle, BorderLayout.CENTER);
    }

}

class AppFrame extends JFrame {

    AppFrame() {
        this.setTitle("Application");
        this.setSize(800, 800);
        this.setBackground(Color.DARK_GRAY);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit

        this.add(new HistoryPanel(), BorderLayout.WEST);
        this.add(new QnaPanel(), BorderLayout.CENTER);
        
        this.setVisible(true); // Make visible
    }
}



public class App {
    public static void main(String[] args) throws Exception {
        new AppFrame();
    }
}
