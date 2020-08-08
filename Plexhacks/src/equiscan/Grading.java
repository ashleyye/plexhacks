package equiscan;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Grading {
	public static void run(String[] args){
		String defaultFile = "shrestha2.jpeg";
		String filename = ((args.length > 0) ? args[0] : defaultFile);
		Mat src = Imgcodecs.imread(filename);
		//check if loaded properly
		if(src.empty()) {
			System.out.println("Error opening image.");
			System.out.println("Program Arguments: [image_name -- default "  + defaultFile +"] \n");
			System.exit(-1);
		}
		
		//create binary image (inverted, b/w, two tone)
		Mat gray = new Mat(src.rows(), src.cols(), src.type());
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
		Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
		Imgproc.threshold(gray, binary, 150, 255, Imgproc.THRESH_BINARY_INV);
		
		List<MatOfPoint> cntrs = new ArrayList<>();
		Mat hierarchy = new Mat();
		
		Imgproc.findContours(binary, cntrs, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		//for(int i = 0, i < )
		
		Scalar color = new Scalar(0, 0, 255);
		Imgproc.drawContours(src, cntrs, -1, color, 2, Imgproc.LINE_8, hierarchy, 2, new Point());

		Imgproc.resize(src, src,  new Size(src.cols()/4, src.rows()/4), 0, 0, Imgproc.INTER_AREA);
		Imgproc.resize(binary, binary,  new Size(binary.cols()/4, binary.rows()/4), 0, 0, Imgproc.INTER_AREA);

		HighGui.imshow("Source", src);

		HighGui.waitKey();
		System.exit(0);
	}
	
	public static void main(String[] args) {
		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		run(args);
	}
}
