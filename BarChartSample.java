import java.io.File;

import javax.imageio.ImageIO;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
 
public class BarChartSample extends Application {
	
	public static String [] questions = new String[30]; 
	public static int [] correctAnswers = new int [30];
    
    
    public static void initializingQuestions() {
	    for (int i = 0; i < questions.length; i++) {
	    	questions[i] = "Question " + i;	    
	    }
    }
 
    @Override public void start(Stage stage){
        stage.setTitle("Bar Chart Sample");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String,Number> bc = 
            new BarChart<String,Number>(xAxis,yAxis);
        bc.setTitle("Question Summary");
        xAxis.setLabel("Questions");       
        yAxis.setLabel("Number of answers");
 
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Correct");
        for (int i = 0; i < questions.length; i++) {
        	series1.getData().add(new XYChart.Data(questions[i], correctAnswers[i]));
        }
             
        
        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Incorrect");
        for (int i = 0; i < questions.length; i++) {
        	series2.getData().add(new XYChart.Data(questions[i], questions.length - correctAnswers[i]));
        }
        
        Scene scene = new Scene(new Group(),800,600, Color.WHITE);
        
        bc.getData().addAll(series1, series2);
        stage.setTitle("Analytics");
        stage.setScene(scene);
        stage.show();
        ((Group) scene.getRoot()).getChildren().add(bc);
        WritableImage wim = scene.snapshot(null);
 
        File file = new File("graph.png");


        try {
            ImageIO.write(SwingFXUtils.fromFXImage(wim, null), "PNG", file);
        } catch (Exception s) {
        }
        System.out.println("yeet");
        
    }
 
    public static void main(String[] args) {
    	answerCompare answercompare = new answerCompare();
    	correctAnswers = answercompare.getAnswerStats();
    	String [] mainQuestions = new String[answercompare.getTotalCount()];
    	questions = mainQuestions;
    	initializingQuestions();
        launch(args);
    }
}