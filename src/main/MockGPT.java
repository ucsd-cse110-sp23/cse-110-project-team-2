
/**
 * Used for mocking ChatGPT question-asking
 */
public class MockGPT {
    private String[] answerSet;
    private int questionIdx;

    //
    public MockGPT(String[] answerSet) {
        this.answerSet = answerSet;
        this.questionIdx = 0;
    }

    public String askQuestion(String str){
        String ret = answerSet[questionIdx];
        questionIdx = (questionIdx + 1) % answerSet.length;

        return ret;
    }


}
