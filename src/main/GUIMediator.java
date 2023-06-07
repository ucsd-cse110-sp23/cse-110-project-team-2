
/*
 * GUImediator class that allows GUI components which call on other GUI components to interface with a mediator instead of carrying references to each other
 */
public class GUIMediator {
    private HistoryList historyList;
    private HistoryPanel historyPanel;
    private QnaDisplay qnaDisplay;


    public GUIMediator(){

    }


    public void setHistoryList(HistoryList hl){
        historyList = hl;
    }

    // public void setHistoryPanel(HistoryPanel hp){
    //     historyPanel = hp;
    // }

    public void setQnaDisplay(QnaDisplay qd){
        qnaDisplay = qd;
    }

    public void changeQnaDisplayText(QNA qna){
        qnaDisplay.setQNASection(qna);
    }

    public void addHistoryListPrompt(Prompt prompt){
        historyList.addPrompt(prompt);
    }

    public void clearQNADisplayText(){
        QNA qna = new QNA("","", PromptType.NOCOMMAND);
        qnaDisplay.setQNASection(qna);
    }

    public void deletePrompt(){
        historyList.deletePrompt();
    }

    public void deleteAllPrompts(){
        historyList.deleteAllPrompts();
    }
}