import java.io.*;
import java.util.*;
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
    
    public HistoryList historyList;
    RecordPanel recordPanel;
    

    APIHandler apiHandler;
    HistoryManager historyManager;
    GUIMediator guiMediator; 
/*
    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";
*/
    QnaPanel(GUIMediator guiM, HistoryManager histManager) {
        this.setPreferredSize(new Dimension(600, 800));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.RED);


        qnaDisplay = new QnaDisplay(guiM);
        recordPanel = new RecordPanel();
        this.add(qnaDisplay, BorderLayout.CENTER);
        this.add(recordPanel, BorderLayout.SOUTH);


        startButton = recordPanel.getStartButton();
        stopButton = recordPanel.getStopButton();

        apiHandler = new APIHandler();
        historyManager = histManager;
        guiMediator = guiM;

/*     
        audioHandler = new AudioHandler();
        gptHandler = new GPTHandler(APIKey);
        whisperHandler = new WhisperHandler(APIKey);
*/
        addListeners();
    }

    public QnaDisplay getQnaDisplay(){
        return qnaDisplay;
    }

    public void setHistoryList(HistoryList hl){
        historyList = hl;
    }

    public void addListeners() {
        startButton.addActionListener(
          (ActionEvent e) -> {
              System.out.println("START PRESSED");
              //audioHandler.startRecording();
              recordPanel.showRecording();
              apiHandler.startRecording();
          }
        );
        stopButton.addActionListener(
        (ActionEvent e) -> {
            System.out.println("STOP PRESSED");
            //TODO: maybe multithread?
            recordPanel.hideRecording();
            apiHandler.stopRecording();
            QNA gptQNA = apiHandler.audioToAnswer();

            guiMediator.changeQnaDisplayText(gptQNA);
            Prompt newPrompt = historyManager.addToHistory(gptQNA);
            guiMediator.addHistoryListPrompt(newPrompt);            
            revalidate();                
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

        titlePanel = new TextPanel(title, titleColor, new Dimension(600,50));
        contentPanel = new TextPanel(content, contentColor, new Dimension(600,250));

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

    TextPanel(String text, Color color, Dimension size) {
        this.setBackground(color);
        this.setPreferredSize(size);
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
    QnaDisplay(GUIMediator guiM) {
        this.setPreferredSize(new Dimension(600, 600));
        this.setLayout(new GridLayout(2, 1));
        this.setBackground(Color.GREEN);

        questionContentPanel = new ContentPanel("Question", "", Color.RED, Color.BLUE);
        answerContentPanel =  new ContentPanel("Answer", "", Color.CYAN, Color.PINK);
        this.add(questionContentPanel);
        this.add(answerContentPanel);

        guiM.setQnaDisplay(this);
        
    }

    public void setQNASection(QNA qna){
        questionContentPanel.setContent(qna.getQuestion());
        answerContentPanel.setContent(qna.getAnswer());
        
    }
}

//SHOULD ONLY DISPLAY QUESTIONS
// TODO: Separate class for managing history (keep business logic out of gui)
class HistoryList extends JPanel {
    public QnaDisplay qnaDisplay;
    Color backgroundColor = new Color(240, 248, 255);
    HistoryManager historyManager;
    GUIMediator guiMediator;

    HistoryList(GUIMediator guiM, HistoryManager histManager){
        GridLayout layout = new GridLayout(10, 1);
        layout.setVgap(5); // Vertical gap

        this.setLayout(layout); // 10 tasks
        this.setPreferredSize(new Dimension(100, 500));
        this.setBackground(backgroundColor);

        guiM.setHistoryList(this);
        this.historyManager = histManager;
        this.guiMediator = guiM;
        loadHistory();
    }

    public void setQnaDisplay(QnaDisplay qd){
        qnaDisplay = qd;
    }

    public void addPrompt(Prompt prompt){
        this.add(prompt);
        JButton selectButton = prompt.getSelectButton();
        selectButton.addActionListener(
        (ActionEvent e2) -> {
            //TODO: update qnadisplay to show the selected prompt and answer
            historyManager.setSelected(prompt);
            guiMediator.changeQnaDisplayText(prompt.getQNA());
            setQnaDisplay(qnaDisplay);
            revalidate(); // Updates the frame
        }
        );

    }

    public void removePrompt(Prompt prompt){
        this.remove(prompt);
        //repaint();
    }

    private void loadHistory(){
        ArrayList<Prompt> promptList = historyManager.getHistoryList();
        for(Prompt prompt : promptList){
            addPrompt(prompt);
        }
    }
}

class HistoryPanel extends JPanel {
    private JPanel historyFooter;
    private HistoryButtonPanel historyButtonPanel;
    private HistoryList historyList;
    private HistoryManager historyManager;
    private JButton deleteSingleButton;
    private JButton deleteAllButton;

    HistoryPanel(GUIMediator guiM, HistoryManager histManager) {
        this.setPreferredSize(new Dimension(200, 800));
        this.setBackground(Color.BLUE);
        this.setLayout(new BorderLayout());

        TextPanel headerPanel = new TextPanel("History", Color.LIGHT_GRAY, new Dimension(200, 50));
        this.add(headerPanel, BorderLayout.NORTH);

        historyList = new HistoryList(guiM, histManager);
        this.add(historyList, BorderLayout.CENTER);

        historyButtonPanel = new HistoryButtonPanel();
        this.add(historyButtonPanel, BorderLayout.SOUTH);

        this.deleteSingleButton = historyButtonPanel.getDeleteSingleButton();
        this.deleteAllButton = historyButtonPanel.getDeleteAllButton();

        this.historyManager = histManager;
        addListeners();

    }

    public void setQnaDisplay(QnaDisplay qd){
        historyList.setQnaDisplay(qd);
    }

    public HistoryList getHistoryList(){
        return historyList;
    }

    public void addListeners(){
        deleteSingleButton.addActionListener(
            (ActionEvent e) -> {
                Prompt toDelete = historyManager.getSelectedToDelete();
                System.out.println("Deleting prompt" + toDelete.getQNA().toString());
                historyList.remove(toDelete);
                repaint();
                revalidate();
            }
        );
    }
}

class HistoryButtonPanel extends JPanel {
    private JButton deleteAll;
    private JButton deleteSingle;

    private JLabel deleteSelected; // Maybe implement when any delete is selected, label shows up
    HistoryButtonPanel() {
        deleteAll = new JButton("Delete All");
        deleteAll.setPreferredSize(new Dimension(80, 20));

        deleteSingle = new JButton("D.O");
        deleteSingle.setPreferredSize(new Dimension(80, 20));

        this.setPreferredSize(new Dimension(200, 100));
        this.add(deleteAll, BorderLayout.CENTER);
        this.add(deleteSingle, BorderLayout.CENTER);
    }

    public JButton getDeleteAllButton(){
        return deleteAll;
    }

    public JButton getDeleteSingleButton(){
        return deleteSingle;
    }



}

class AppFrame extends JFrame {

    AppFrame() {
        this.setTitle("Application");
        this.setSize(800, 800);
        this.setBackground(Color.DARK_GRAY);
        this.setLayout(new BorderLayout());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close on exit


        GUIMediator guiMediator =  new GUIMediator();
        HistoryManager historyManager = new HistoryManager("history.txt");

        HistoryPanel hp = new HistoryPanel(guiMediator, historyManager);
        QnaPanel qp = new QnaPanel(guiMediator, historyManager);
        hp.getHistoryList().qnaDisplay = qp.getQnaDisplay();
        this.add(hp, BorderLayout.WEST);
        this.add(qp, BorderLayout.CENTER);
        qp.setHistoryList(hp.getHistoryList());
        this.setVisible(true); // Make visible
    }
}

class Prompt extends JPanel {

    JLabel qtext;
    JButton selectButton;
  
    Color gray = new Color(218, 229, 234);
    Color green = new Color(188, 226, 158);
  
    private boolean selected;
    private QNA qna;
  
    Prompt(QNA qna) {
      this.qna = qna;
      this.setPreferredSize(new Dimension(400, 20)); // set size of task
      this.setBackground(gray); // set background color of task
  
      this.setLayout(new BorderLayout()); // set layout of task
  
      //markedDone = false;
  
      qtext = new JLabel(qna.getQuestion()); // create index label
      qtext.setPreferredSize(new Dimension(150, 20)); // set size of index label
      qtext.setHorizontalAlignment(JLabel.CENTER); // set alignment of index label
      this.add(qtext, BorderLayout.WEST); // add index label to task
  
      selectButton = new JButton("Select");
      selectButton.setPreferredSize(new Dimension(80, 20));
      selectButton.setBorder(BorderFactory.createEmptyBorder());
      selectButton.setFocusPainted(false);
  
      this.add(selectButton, BorderLayout.EAST);
    }

    public boolean equals(Prompt otherPrompt){
        return this.qna.equals(otherPrompt.getQNA());
    }
  
    public void changeIndex(int num) {
      this.qtext.setText(num + ""); // num to String
      this.revalidate(); // refresh
    }

    public QNA getQNA(){
      return qna;
    }
  
    public JButton getSelectButton() {
      return selectButton;
    }
  
    public boolean getState() {
      return selected;
    }
  
    // TODO: deselect every time we select a new one
    public void changeState() {
      if(!this.getState()){
        this.setBackground(green);
        selected = true;
      }
      else{
        this.setBackground(gray);
        selected = false;
      }
      revalidate();
    }
}



public class App {
    public static void main(String[] args) throws Exception {
        new AppFrame();
    }
}
