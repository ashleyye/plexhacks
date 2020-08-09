import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class answerCompare{
	public static char [] key = new char[30];
	public static int totalCorrectCount = 0;
	public static int answersChecked = 0;
	//public static ArrayList<Integer> answerStats = new ArrayList<Integer>();
	public static ArrayList<Integer> correctStats = new ArrayList<Integer>();
	private int totalCount = 0;
	private int [] answerStatsPlacehold = new int [30];
	
	public void setTotalCount(int newTotalCount) {
		this.totalCount = newTotalCount;
	}
	
	public int getTotalCount() {
		return totalCount;
	}
	
	public void setAnswerStats(int newAnswerStats[]) {
		answerStatsPlacehold = newAnswerStats;
	}
	
	public int[] getAnswerStats() {
		return answerStatsPlacehold;
	}
	
    public static void main(String[] args){
    	
    	answerCompare answercompare = new answerCompare();
    	
        for (int i = 0; i < 30; i++){
            key[i] = 'n';
        }
        
    	//read in key
        for (int i = 0; i < key.length; i++){
            if (key[i] != 'n'){
            	answercompare.setTotalCount(answercompare.getTotalCount() + 1);
            }
        }
        
        int [] answerStats = new int [answercompare.getTotalCount()];    	
        for (int i = 0; i < answerStats.length; i++){
            answerStats[i] = 0;
        }
    	
        //for each student jpg
        char [] answer = new char[30];
        int correctCount = 0;
        for (int i = 0; i < 30; i++){
            answer[i] = 'n';
        }

        //read in answer sheet

        /* sample input*/

        ArrayList<Character> exKey = new ArrayList<>(Arrays.asList('a', 'd', 'b', 'c', 'c', 'd', 'e', 'b', 'a', 'c', 'd','d','d','a','a','c','e','a','a','e', 'd'));
        ArrayList<Character> exAnswer = new ArrayList<>(Arrays.asList('c', 'b', 'e', 'd', 'a', 'a', 'b', 'n', 'e', 'e', 'c', 'd','a','b','b','b','c','c','e','c','a','b'));

        for (int i = 0; i < exKey.size(); i++){
        	key[i] = exKey.get(i);
            answer[i] = exAnswer.get(i);
        }


        for (int i = 0; i < answercompare.getTotalCount(); i++){
            if (key[i] != 'n'){
                if (answer[i] != 'n' && key[i] == answer[i]) {
                    correctCount++;
                    answerStats[i]++;
                }  
            }
        }
        
        totalCorrectCount += correctCount;
        answersChecked++;
        
        answercompare.setAnswerStats(answerStats);

        
        //results
        DecimalFormat df = new DecimalFormat("#.##");
        df.setRoundingMode(RoundingMode.CEILING);
        Double d = (double) correctCount / answercompare.getTotalCount();
            
        System.out.println("Final score: " + correctCount + "/" + answercompare.getTotalCount());
        System.out.println("Accuracy rate: " + df.format(d * 100) + "%");
        
        for (int element: answerStats) {
        	System.out.println(element);
        }
        
        //stats
        totalCorrectCount /= answersChecked;
        System.out.println("Average score: " + totalCorrectCount + "/" + answercompare.getTotalCount());
        d = (double) totalCorrectCount / answercompare.getTotalCount();
        System.out.println("Average accuracy: " + df.format(d * 100) + "%");
        
        
        
        
    }
}
