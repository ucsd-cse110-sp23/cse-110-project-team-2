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

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.JList;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

class RecordPanel extends JPanel {
    private JButton startButton;
    private JButton stopButton;
    private JLabel recordingLabel;
    
    //private Border emptyBorder = BorderFactory.createEmptyBorder();

    RecordPanel(){
        this.setPreferredSize(new Dimension(600, 100));
        this.setBackground(Color.PINK);

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
/*
    private AudioHandler audioHandler;
    private GPTHandler gptHandler;
    private WhisperHandler whisperHandler;
    private static String APIKey = "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";
*/
    QnaPanel(GUIMediator guiM) {
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
            recordPanel.hideRecording();
            apiHandler.stopRecording();
            QNA gptPrompt = apiHandler.audioToAnswer();


            qnaDisplay.setQNASection(gptPrompt);


            revalidate();
// TODO: Separate class for managing history (keep business logic out of gui)

            try {
                
                //System.out.println("getting API responses...");
                //TODO: maybe multithread?
                /*
                whisperResponse = whisperHandler.transcribeAudio(newFile);
                gptResponse = gptHandler.askQuestion(whisperResponse);
                System.out.println(gptResponse);

                QNA gptPrompt = new QNA(whisperResponse, gptResponse);
                System.out.println("response...");
                */

                //TODO: save the prompt
                FileWriter history = new FileWriter("history.txt", true);
                history.write(gptPrompt.getQuestion() + ",,," + gptPrompt.getAnswer() + ",,,");
                history.close();
            } catch (Exception exception) {
                System.out.println(exception.getStackTrace());
            }
            QNA qna = new QNA(gptPrompt.getQuestion(),gptPrompt.getAnswer());
            Prompt prompt = new Prompt(qna);
            historyList.qnalist.add(qna);
            historyList.updateLayout();
            historyList.add(prompt);
            JButton selectButton = prompt.getSelectButton();
            selectButton.addActionListener(
            (ActionEvent e2) -> {
                //TODO: update qnadisplay to show the selected prompt and answer
                prompt.changeState(); // Change color of task
                qnaDisplay.setQNASection(qna);
                revalidate(); // Updates the frame
            }
            );
            selectButton.doClick();
          }
        );
    }
}

class ContentPanel extends JPanel {

    private String title, content;
    private TextPane titlePane, contentPane;

    ContentPanel(String title, String content, 
                 Color titleColor, Color titlePaneColor, 
                 Color contentColor,  Color contentPaneColor) {
        this.setPreferredSize(new Dimension(600, 300));
        this.setLayout(new BorderLayout());

        this.title = title;
        this.content = content;

        titlePane = new TextPane(title, new Dimension(600,50), 20, titleColor, titlePaneColor);
        contentPane = new TextPane(content, new Dimension(600,250), 20, contentColor, contentPaneColor);
        this.setTitle(title);
        this.setContent(content);

        this.add(titlePane, BorderLayout.NORTH);
        this.add(contentPane, BorderLayout.CENTER);
    }

    public void setContent(String content){
        this.content = content;
        contentPane.replace(content);
    }

    public String getContent(){
        return content;
    }

    public void setTitle(String title){
        this.title = title;
        titlePane.replace(title);
    }


    public String getTitle(){
        return title;
    }
}

class TextPane extends JTextPane {   
    TextPane(String text, Dimension size, int fontSize, Color textColor, Color paneColor) {
        this.setEditable(false);
        this.setBackground(paneColor);
        this.setPreferredSize(size);

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, textColor);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "SansSerif");
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_CENTER);
        aset = sc.addAttribute(aset, StyleConstants.FontSize, fontSize);
        this.setCharacterAttributes(aset, false);

        this.replace(text);
    }

    public void replace(String msg) {
        this.setEditable(true);
        this.setText(msg);
        this.setEditable(false);
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

        questionContentPanel = new ContentPanel("Question", "Select a question from the history list.", 
                                                Color.BLACK, Color.LIGHT_GRAY, Color.DARK_GRAY, Color.WHITE);
        answerContentPanel =  new ContentPanel("Answer", "Or ask a question using the record button.",
                                               Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN);

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
    private Color backgroundColor = new Color(240, 248, 255);
    private GridLayout layout;
    ArrayList<QNA> qnalist;

    HistoryList(GUIMediator guiM){
        layout = new GridLayout();
        layout.setVgap(5); // Vertical gap
        this.setLayout(layout); // 10 tasks
        //this.setPreferredSize(new Dimension(100, 500));
        this.setBackground(backgroundColor);

        guiM.setHistoryList(this);

        loadHistory();
    }

    public void setQnaDisplay(QnaDisplay qd){
        qnaDisplay = qd;
    }

    public void updateLayout() {
        this.layout.setRows(qnalist.size());
    }

    private void loadHistory(){
        String tempQuestion;
        String tempAnswer;
        qnalist = new ArrayList<QNA>();
        try {
            FileReader file = new FileReader("history.txt");
            BufferedReader br = new BufferedReader(file);
            Scanner sr = new Scanner(br);
            sr.useDelimiter(",,,");
            while (sr.hasNext()) {
                tempQuestion = sr.next();
                tempAnswer = sr.next();
                qnalist.add(new QNA(tempQuestion,tempAnswer));
            }
            sr.close();
            br.close();
            file.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        updateLayout();
        for(QNA qna : qnalist){
            Prompt prompt = new Prompt(qna);
            this.add(prompt);
            JButton selectButton = prompt.getSelectButton();
            selectButton.addActionListener(
            (ActionEvent e2) -> {
                //TODO: update qnadisplay to show the selected prompt and answer
                prompt.changeState(); // Change color of task
                qnaDisplay.setQNASection(qna);
                revalidate(); // Updates the frame
            }
          );
        }
    }
}

class HistoryPanel extends JPanel {
    private JPanel historyButtonPanel;
    private HistoryList historyList;

    HistoryPanel(GUIMediator guiM) {
        this.setPreferredSize(new Dimension(200, 800));
        this.setBackground(Color.BLUE);
        this.setLayout(new BorderLayout());

        TextPane headerPanel = new TextPane("History", new Dimension(200, 50), 20, Color.LIGHT_GRAY, Color.BLUE);
        this.add(headerPanel, BorderLayout.NORTH);

        historyList = new HistoryList(guiM);
        JScrollPane sp = new JScrollPane(historyList);
        sp.setPreferredSize(new Dimension(200, 670));
        this.setAutoscrolls(true);
        this.add(sp, BorderLayout.CENTER);

        historyButtonPanel = new HistoryButtonPanel();
        this.add(historyButtonPanel, BorderLayout.SOUTH);

    }

    public void setQnaDisplay(QnaDisplay qd){
        historyList.setQnaDisplay(qd);
    }

    public HistoryList getHistoryList(){
        return historyList;
    }
}

class HistoryButtonPanel extends JPanel {
    private JButton deleteAll;
    private JButton deleteSingle;

    private JLabel deleteSelected; // Maybe implement when any delete is selected, label shows up
    HistoryButtonPanel() {
        this.setLayout(new GridLayout(2, 1));
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


        GUIMediator guiMediator =  new GUIMediator();

        HistoryPanel hp = new HistoryPanel(guiMediator);
        QnaPanel qp = new QnaPanel(guiMediator);
        hp.getHistoryList().qnaDisplay = qp.getQnaDisplay();
        this.add(hp, BorderLayout.WEST);
        this.add(qp, BorderLayout.CENTER);
        qp.setHistoryList(hp.getHistoryList());
        this.setVisible(true); // Make visible
    }
}

class Prompt extends JPanel {

    TextPane qtext;
    JButton selectButton;
  
    Color gray = new Color(218, 229, 234);
    Color green = new Color(188, 226, 158);
  
    private boolean selected;
    private QNA qna;
  
    Prompt(QNA qna) {
      this.qna = qna;
      this.setPreferredSize(new Dimension(200, 75)); // set size of task
      this.setBackground(gray); // set background color of task
  
      this.setLayout(new BorderLayout()); // set layout of task
  
      //markedDone = false;
  
      /*qtext = new JLabel(qna.getQuestion()); // create index label
      qtext.setPreferredSize(new Dimension(150, 20)); // set size of index label
      qtext.setHorizontalAlignment(JLabel.CENTER); // set alignment of index label
      this.add(qtext, BorderLayout.WEST); // add index label to task*/

      qtext = new TextPane(qna.getQuestion(), new Dimension(150, 20), 16, Color.BLACK, Color.LIGHT_GRAY);
      this.add(qtext, BorderLayout.WEST);
  
      selectButton = new JButton("Select");
      selectButton.setPreferredSize(new Dimension(50, 20));
      selectButton.setBorder(BorderFactory.createEmptyBorder());
      selectButton.setFocusPainted(false);
  
      this.add(selectButton, BorderLayout.EAST);
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
