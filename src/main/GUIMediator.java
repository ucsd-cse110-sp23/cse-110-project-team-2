public class GUIMediator {
    private HistoryList historyList;
    private QnaDisplay qnaDisplay;


    public GUIMediator(){

    }

    public void setHistoryList(HistoryList hl){
        historyList = hl;
    }

    public void setQnaDisplay(QnaDisplay qd){
        qnaDisplay = qd;
    }

    public void changeQnaDisplayText(QNA qna){
        qnaDisplay.setQNASection(qna);
    }

    public void addHistoryListPrompt(Prompt prompt){
        historyList.add(prompt);
    }

}