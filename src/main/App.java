import java.util.*;
// gui libraries
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.StyleContext;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/*
 * Recording panel with buttons start and stop,with recording label
 */
class RecordPanel extends JPanel {
    private JButton startButton;
    private JButton stopButton;
    private JLabel recordingLabel;

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);
    
    //private Border emptyBorder = BorderFactory.createEmptyBorder();

    RecordPanel(){
        this.setPreferredSize(new Dimension(600, 100));
        this.setBackground(blue);

        startButton = new JButton("Start");
        startButton.setPreferredSize(new Dimension(80, 20));
        startButton.setFont(new Font("Verdana", 0, 15));
        //startButton.setBackground(blue);
        this.add(startButton);
        stopButton = new JButton("Stop");
        stopButton.setPreferredSize(new Dimension(80, 20));
        stopButton.setFont(new Font("Verdana", 0, 15));
        //stopButton.setBackground(blue);
        this.add(stopButton); 

        recordingLabel = new JLabel("Recording");
        recordingLabel.setVerticalAlignment(JLabel.CENTER);
        recordingLabel.setForeground(Color.RED);
        recordingLabel.setPreferredSize(new Dimension(100, 20));
        recordingLabel.setFont(new Font("Verdana", 0, 15));
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

/*
 * Total panel with the prompts/answers and the buttons?
 */
class QnaPanel extends JPanel {

    JButton startButton;
    JButton stopButton;

    QnaDisplay qnaDisplay;
    
    public HistoryList historyList;
    RecordPanel recordPanel;
    

    APIHandler apiHandler;
    HistoryManager historyManager;
    GUIMediator guiMediator; 

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

    QnaPanel(GUIMediator guiM, HistoryManager histManager) {
        this.setPreferredSize(new Dimension(600, 300));

        this.setLayout(new BorderLayout());
        this.setBackground(yellow);


        qnaDisplay = new QnaDisplay(guiM);
        recordPanel = new RecordPanel();
        this.add(qnaDisplay, BorderLayout.CENTER);
        this.add(recordPanel, BorderLayout.SOUTH);


        startButton = recordPanel.getStartButton();
        stopButton = recordPanel.getStopButton();

        apiHandler = new APIHandler();
        historyManager = histManager;
        guiMediator = guiM;

        addListeners();
    }

    /*
     * Adds listeners for the start and stop recordings button
     */
    public void addListeners() {
        startButton.addActionListener(
            (ActionEvent e) -> {
                System.out.println("START PRESSED");
                
                //show the recording text then start recording
                recordPanel.showRecording();
                apiHandler.startRecording();
          }
        );
        stopButton.addActionListener(
            (ActionEvent e) -> {
                System.out.println("STOP PRESSED");

                //hide recording text stop recording
                recordPanel.hideRecording();
                apiHandler.stopRecording();

                //use APi handler to get the response from chatGPT based on the audio file
                QNA gptQNA = apiHandler.audioToReply();

                //update the display to show the qna
                guiMediator.changeQnaDisplayText(gptQNA);

                //add the prompt to the history manager/get prompt to display in history
                Prompt newPrompt = historyManager.addToHistory(gptQNA);
                guiMediator.addHistoryListPrompt(newPrompt);            
                revalidate();                
            }
        );
    }
}

/*
 * Panel type to generically display content
 */
class ContentPanel extends JPanel {
    private static final int MIN_HEIGHT = 35;
    private String title, content;
    private TextPane titlePane, contentPane;

    ContentPanel(String title, String content, 
                 Color titleColor, Color titlePaneColor, 
                 Color contentColor,  Color contentPaneColor, 
                 int height, int width) {
        this.setPreferredSize(new Dimension(width, height));
        this.setLayout(new BorderLayout());

        this.title = title;
        this.content = content;

        titlePane = new TextPane(title, new Dimension(width, MIN_HEIGHT), 20, titleColor, titlePaneColor);
        contentPane = new TextPane(content, new Dimension(width, height-MIN_HEIGHT), 20, contentColor, contentPaneColor);
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


/*
 * class to be used as a text pane
 */
class TextPane extends JTextPane {   

    TextPane(String text, Dimension size, int fontSize, Color textColor, Color paneColor) {
        this.setEditable(false);
        this.setBackground(paneColor);
        this.setPreferredSize(size);
        //this.setFont(new Font("Open Sans", 0, size));

        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, textColor);
        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Verdana");
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

    public void setBG(Color paneColor) {
        this.setBackground(paneColor);
    }
}


/*
 * Display for the question and answer
 */ 
class QnaDisplay extends JPanel {
    private static final int WIDTH = 600, HEIGHT = 600;

    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

    ContentPanel commandContentPanel;
    ContentPanel questionContentPanel;
    ContentPanel answerContentPanel;
    QnaDisplay(GUIMediator guiM) {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setLayout(new BorderLayout());
        this.setBackground(Color.GREEN);
        
        commandContentPanel = new ContentPanel("Command", "DEFAULT_COMMMAND",
                                                Color.BLACK, turquoise, Color.BLACK, yellow,
                                                HEIGHT/4, WIDTH);
        questionContentPanel = new ContentPanel("Question", "DEFAULT_QUESTION", 
                                                Color.BLACK, turquoise, Color.DARK_GRAY, yellow,
                                                HEIGHT/4, WIDTH);
        answerContentPanel =  new ContentPanel("Answer", "DEFAULT_ANSWER",
                                               Color.BLACK, turquoise, Color.BLUE, yellow,
                                               HEIGHT/2, WIDTH);

        JPanel qc = new JPanel(new GridLayout(2,1));
        qc.add(commandContentPanel);
        qc.add(questionContentPanel);

        this.add(qc, BorderLayout.NORTH);
        this.add(answerContentPanel, BorderLayout.CENTER);

        guiM.setQnaDisplay(this);
        
    }

    public void setQNASection(QNA qna){
        questionContentPanel.setContent(qna.getQuestion());
        answerContentPanel.setContent(qna.getAnswer());
    }
}

/*
 * panel that holds all the prompts in the prompt history
 */
class HistoryList extends JPanel {
    private GridLayout layout;
    private final int MIN_ROWS = 6;

    Color backgroundColor = new Color(240, 248, 255);
    HistoryManager historyManager;
    GUIMediator guiMediator;
    int count;
    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);

    HistoryList(GUIMediator guiM, HistoryManager histManager){
        layout = new GridLayout(0, 1);
        layout.setVgap(5); // Vertical gap
        layout.setColumns(1);

        this.setLayout(layout); // 10 tasks
        //this.setPreferredSize(new Dimension(100, 500));
        this.setBackground(green);

        guiM.setHistoryList(this);
        this.historyManager = histManager;
        this.guiMediator = guiM;
        loadHistory();
    }

    public void updateLayout() {
        int numRows = (count < MIN_ROWS) ? MIN_ROWS : count;
        this.layout.setRows(numRows);
    }

    /*
     * Add a prompt to the history list
     */
    public void addPrompt(Prompt prompt){
        count++;
        updateLayout();
        this.add(prompt);
        JButton selectButton = prompt.getSelectButton();

        //Add a listener to the button on the prompt to select the prompt for viewing
        selectButton.addActionListener(
            (ActionEvent e2) -> {
                //TODO: update qnadisplay to show the selected prompt and answer
                historyManager.setSelected(prompt);
                guiMediator.changeQnaDisplayText(prompt.getQNA());
                revalidate(); // Updates the frame
            }
        );
    }

    /*
     * remove a prompt from the history list
     */
    public void removePrompt(Prompt prompt){
        count--;
        updateLayout();
        this.remove(prompt);
        revalidate();
        //repaint();
    }

    /*
     * Load all the prompts from the history.txt file. delegates to history manager
     */
    private void loadHistory(){
        ArrayList<Prompt> promptList = historyManager.getHistoryList();
        for(Prompt prompt : promptList){
            addPrompt(prompt);
        }
        count = promptList.size();
    }
}


/*
 * Wrapper panel for history
 */
class HistoryPanel extends JPanel {
    //private JPanel historyFooter;
    private HistoryButtonPanel historyButtonPanel;
    private HistoryList historyList;
    private HistoryManager historyManager;
    private JButton deleteSingleButton;
    private JButton deleteAllButton;

    private GUIMediator guiM;
    Color yellow = new Color(229, 239, 193);
    Color green = new Color(162, 213, 171);
    Color turquoise = new Color(57, 174, 169);
    Color blue = new Color(85, 123, 131);


    HistoryPanel(GUIMediator guiM, HistoryManager histManager) {
        this.setPreferredSize(new Dimension(200, 800));
        this.setBackground(green);
        this.setLayout(new BorderLayout());

        TextPane headerPanel = new TextPane("History", new Dimension(200, 50), 20, Color.LIGHT_GRAY, blue);
        this.add(headerPanel, BorderLayout.NORTH);

        historyList = new HistoryList(guiM, histManager);
        //this.add(historyList, BorderLayout.CENTER);

        JScrollPane sp = new JScrollPane(historyList);
        sp.setPreferredSize(new Dimension(200, 670));
        this.setAutoscrolls(true);
        this.add(sp, BorderLayout.CENTER);

        historyButtonPanel = new HistoryButtonPanel();
        this.add(historyButtonPanel, BorderLayout.SOUTH);

        this.deleteSingleButton = historyButtonPanel.getDeleteSingleButton();
        this.deleteAllButton = historyButtonPanel.getDeleteAllButton();

        this.guiM = guiM;

        this.historyManager = histManager;
        addListeners();

    }

    /*
     * add listeners to the delete selected, and the delete all buttons
     */
    public void addListeners(){
        deleteSingleButton.addActionListener(
            (ActionEvent e) -> {
                Prompt toDelete = historyManager.getSelectedToDelete();
                if (toDelete != null) {
                    System.out.println("Deleting prompt" + toDelete.getQNA().toString());
                    historyList.removePrompt(toDelete);
                    guiM.clearQNADisplayText();
                    repaint();
                    revalidate();
                }
            }
        );
    
        deleteAllButton.addActionListener(
            (ActionEvent e) -> {
                for (Component prompt : historyList.getComponents()){
                    if (prompt instanceof Prompt) {
                        historyList.removePrompt((Prompt) prompt);
                        repaint();
                        revalidate();
                    }
                }
                historyManager.clearHistory();
                guiM.clearQNADisplayText();
                repaint();
                revalidate();
            }
        );
    }
}


/*
 * Panel for delete buttons
 */ 
class HistoryButtonPanel extends JPanel {
    private JButton deleteAll;
    private JButton deleteSingle;

    HistoryButtonPanel() {
        this.setLayout(new GridLayout(2, 1));
        deleteAll = new JButton("Delete All");
        deleteAll.setFont(new Font("Verdana", 0, 15));
        deleteAll.setPreferredSize(new Dimension(80, 20));


        deleteSingle = new JButton("Delete Selected");
        deleteSingle.setFont(new Font("Verdana", 0, 15));
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

/*
 * Appframe that holds all the panels
 */
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
        this.add(hp, BorderLayout.WEST);
        this.add(qp, BorderLayout.CENTER);
        this.setVisible(true); // Make visible
    }
}

/*
 * prompts that are added to the history list. Each one encodes a qna
 */
class Prompt extends JPanel {

    TextPane qtext;
    JButton selectButton;
  
    Color gray = new Color(218, 229, 234);
    Color green = new Color(188, 226, 158);
    Color yellow = new Color(229, 239, 193);
  
    private boolean selected;
    private QNA qna;
  
    Prompt(QNA qna) {
      this.qna = qna;
      this.setPreferredSize(new Dimension(200, 75)); // set size of task
      this.setBackground(gray); // set background color of task
  
      this.setLayout(new BorderLayout()); // set layout of task


      qtext = new TextPane(qna.getQuestion(), new Dimension(150, 20), 16, Color.BLACK, yellow);
      this.add(qtext, BorderLayout.WEST);
  
      selectButton = new JButton("Select");
      selectButton.setPreferredSize(new Dimension(50, 20));
      selectButton.setFont(new Font("Verdana", 0, 15));
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
    
    /*
     * Change the color depending on if the prompt is selected
     */
    public void changeState() {
      if(!this.getState()){
        this.qtext.setBackground(green);
        selected = true;
      }
      else{
        this.qtext.setBackground(yellow);
        selected = false;
      }
      revalidate();
    }
}


/*
 * Run the app!
 */
public class App {
    public static void main(String[] args) throws Exception {
        new AppFrame();
    }
}
