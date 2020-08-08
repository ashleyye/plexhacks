package equiscan;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
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

		List<MatOfPoint> boxes = new ArrayList<>();


		Imgproc.findContours(binary, cntrs, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
		
		List<Rect> boxCnt = new ArrayList<>();
		List<MatOfPoint2f> fixedCntrsPoly = new ArrayList<>();
		
		Rect[] boundRect = new Rect[cntrs.size()];
		MatOfPoint2f[] cntrsPoly  = new MatOfPoint2f[cntrs.size()];

		for(int i = 0; i < cntrs.size(); i++){
			cntrsPoly[i] = new MatOfPoint2f();
			Imgproc.approxPolyDP(new MatOfPoint2f(cntrs.get(i).toArray()), cntrsPoly[i], 3, true);
			boundRect[i] = Imgproc.boundingRect(new MatOfPoint(cntrsPoly[i].toArray()));
			
			float ar = boundRect[i].width/(float)boundRect[i].height;
			if(boundRect[i].width > 60 && boundRect[i].height > 60 && ar <= 1.2 && ar >= 0.8){
				boxCnt.add(boundRect[i]);
				fixedCntrsPoly.add(cntrsPoly[i]);
			}
		}
		Mat cannyOutput = new Mat();
		Imgproc.Canny(gray, cannyOutput, 100, 100 * 2);

		Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);
		List<MatOfPoint> contoursPolyList = new ArrayList<>(fixedCntrsPoly.size());
		for (MatOfPoint2f poly : fixedCntrsPoly) {
			contoursPolyList.add(new MatOfPoint(poly.toArray()));
		}
		for (int i = 0; i < fixedCntrsPoly.size(); i++) {
			Scalar color = new Scalar(0, 255,0);
			Imgproc.drawContours(src, contoursPolyList, i, color);
			Imgproc.rectangle(src, boxCnt.get(i).tl(), boxCnt.get(i).br(), color, 2);
		}
		
		//
		//		Scalar color = new Scalar(0, 0, 255);
		//		Imgproc.drawContours(src, cntrs, -1, color, 2, Imgproc.LINE_8, hierarchy, 2, new Point());

		Imgproc.resize(src, src,  new Size(src.cols()/4, src.rows()/4), 0, 0, Imgproc.INTER_AREA);
		Imgproc.resize(binary, binary,  new Size(binary.cols()/4, binary.rows()/4), 0, 0, Imgproc.INTER_AREA);
		Imgproc.resize(drawing, drawing,  new Size(drawing.cols()/4, drawing.rows()/4), 0, 0, Imgproc.INTER_AREA);

		HighGui.imshow("Source", src);

		HighGui.waitKey();
		//System.exit(0);
	}

	public static void main(String[] args) {
		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		run(args);
	}
}
