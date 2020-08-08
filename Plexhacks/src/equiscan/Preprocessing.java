package equiscan;

import java.util.*;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

public class Preprocessing {

	public static void run(String[] args){

		//load image file (args or default)
		String defaultFile = "shrestha2.jpeg";
		String filename = ((args.length > 0) ? args[0] : defaultFile);
		Mat src = Imgcodecs.imread(filename);
		//check if loaded properly
		if(src.empty()) {
			System.out.println("Error opening image.");
			System.out.println("Program Arguments: [image_name -- default "  + defaultFile +"] \n");
			System.exit(-1);
		}
		Imgproc.GaussianBlur(src, src, new Size(5, 5), 0, 0);

		Mat gray = new Mat(src.rows(), src.cols(), src.type());
		Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
		Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
		Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
		// Edge detection
		//		Imgproc.Canny(src, src, 50, 200, 3, false);

		List<MatOfPoint> contours = new ArrayList<>();
		Mat hierarchy = new Mat();

		Imgproc.findContours(binary, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
		
		
		if(contours.size() > 0){
			System.out.println(contours.size());
		}
		Scalar color = new Scalar(0, 0, 255);
		Imgproc.drawContours(src, contours, -1, color, 2, Imgproc.LINE_8, hierarchy, 2, new Point() ) ;
		Mat lines = new Mat();

		Imgproc.resize(src, src,  new Size(src.cols()/4, src.rows()/4), 0, 0, Imgproc.INTER_AREA);

		HighGui.imshow("Source", src);

		HighGui.waitKey();
		System.exit(0);

	}

	public static void runHough(String[] args) {
		// Declare the output variables
		Mat dst = new Mat(), cdst = new Mat(), cdstP;
		String default_file = "tableborder2.jpg";
		String filename = ((args.length > 0) ? args[0] : default_file);
		// Load an image
		Mat src = Imgcodecs.imread(filename, Imgcodecs.IMREAD_GRAYSCALE);
		// Check if image is loaded fine
		if( src.empty() ) {
			System.out.println("Error opening image!");
			System.out.println("Program Arguments: [image_name -- default "
					+ default_file +"] \n");
			System.exit(-1);
		}
		// Edge detection
		Imgproc.Canny(src, src, 50, 200, 3, false);
		// Copy edges to the images that will display the results in BGR
		Imgproc.cvtColor(src, cdst, Imgproc.COLOR_GRAY2BGR);
		cdstP = cdst.clone();
		// Standard Hough Line Transform
		Mat lines = new Mat(); // will hold the results of the detection
		Imgproc.HoughLines(src, lines, 1, Math.PI/180, 150); // runs the actual detection
		// Draw the lines
		for (int x = 0; x < lines.rows(); x++) {
			double rho = lines.get(x, 0)[0],
					theta = lines.get(x, 0)[1];
			double a = Math.cos(theta), b = Math.sin(theta);
			double x0 = a*rho, y0 = b*rho;
			Point pt1 = new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000*(a)));
			Point pt2 = new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000*(a)));
			Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
		}
		// Probabilistic Line Transform
		Mat linesP = new Mat(); // will hold the results of the detection
		Imgproc.HoughLinesP(src, linesP, 1, Math.PI/180, 40, 40, 10); // runs the actual detection		

		// Draw the lines
		for (int x = 0; x < linesP.rows(); x++) {
			double[] l = linesP.get(x, 0);
			Imgproc.line(cdstP, new Point(l[0], l[1]), new Point(l[2], l[3]), new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
		}
		// Show results

		//		Imgproc.resize(src, src,  new Size(src.cols()/4, src.rows()/4), 0, 0, Imgproc.INTER_AREA);
		//		Imgproc.resize(cdst, cdst,  new Size(cdst.cols()/4, cdst.rows()/4), 0, 0, Imgproc.INTER_AREA);
		//		Imgproc.resize(cdstP, cdstP,  new Size(cdstP.cols()/4, cdstP.rows()/4), 0, 0, Imgproc.INTER_AREA);

		HighGui.imshow("Source", src);
		//HighGui.imshow("Detected Lines (in red) - Standard Hough Line Transform", cdst);
		HighGui.imshow("Detected Lines (in red) - Probabilistic Line Transform", cdstP);


		// Wait and Exit
		HighGui.waitKey();
		System.exit(0);
	}

	public static void main(String[] args) {
		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		run(args);
		//Loading the OpenCV core library
		//	      System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
		//	      String file ="shrestha.jpg";
		//	      Mat src = Imgcodecs.imread(file);
		//	      //Converting the source image to binary
		//	      Mat gray = new Mat(src.rows(), src.cols(), src.type());
		//	      Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
		//	      Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
		//	      Imgproc.threshold(gray, binary, 100, 255, Imgproc.THRESH_BINARY_INV);
		//	      //Finding Contours
		//	      List<MatOfPoint> contours = new ArrayList<>();
		//	      Mat hierarchey = new Mat();
		//	      Imgproc.findContours(binary, contours, hierarchey, Imgproc.RETR_TREE,
		//	      Imgproc.CHAIN_APPROX_SIMPLE);
		//	      //Drawing the Contours
		//	      Scalar color = new Scalar(0, 0, 255);
		//	      Imgproc.drawContours(src, contours, -1, color, 2, Imgproc.LINE_8,
		//	      hierarchey, 2, new Point() ) ;
		//	      
		//	      Imgproc.resize(src, src,  new Size(src.cols()/4, src.rows()/4), 0, 0, Imgproc.INTER_AREA);
		//	      HighGui.imshow("Drawing Contours", src);
		//	      
		//	      HighGui.waitKey();
	}
}
