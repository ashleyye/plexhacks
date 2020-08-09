package equiscan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		String defaultFile = "lucinda.jpg";
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
		Imgproc.threshold(gray, binary, 130, 255, Imgproc.THRESH_BINARY_INV);

		//find contours
		List<MatOfPoint> cntrs = new ArrayList<>();
		Mat hierarchy = new Mat();
		Imgproc.findContours(binary, cntrs, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

		List<MatOfPoint> fixedCntrs = new ArrayList<>();
		List<Rect> fixedBoundRect = new ArrayList<>();
		List<MatOfPoint2f> fixedCntrsPoly = new ArrayList<>();

		Rect[] boundRect = new Rect[cntrs.size()];
		MatOfPoint2f[] cntrsPoly  = new MatOfPoint2f[cntrs.size()];

		for(int i = 0; i < cntrs.size(); i++){
			cntrsPoly[i] = new MatOfPoint2f();
			Imgproc.approxPolyDP(new MatOfPoint2f(cntrs.get(i).toArray()), cntrsPoly[i], 3, true);
			boundRect[i] = Imgproc.boundingRect(new MatOfPoint(cntrsPoly[i].toArray())); //create bounding rectangle

			float ar = boundRect[i].width/(float)boundRect[i].height;
			if(boundRect[i].width > 60 && boundRect[i].height > 60 && ar <= 1.2 && ar >= 0.8 && boundRect[i].tl().y > 300){ //checks if are big enough and aspect ratio is close to 1, not header
				fixedBoundRect.add(boundRect[i]);
				fixedCntrsPoly.add(cntrsPoly[i]);
				fixedCntrs.add(cntrs.get(i));
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
			Scalar color = new Scalar(0, 0, 255);
			Imgproc.drawContours(src, contoursPolyList, i, color);
			Imgproc.rectangle(src, fixedBoundRect.get(i).tl(), fixedBoundRect.get(i).br(), color, 2); //draws sorted contours
		}

		//sort Contours, t/b, l/r
		Collections.sort(fixedCntrs, new Comparator<MatOfPoint>() {
			@Override
			public int compare(MatOfPoint o1, MatOfPoint o2) {
				Rect r1 = Imgproc.boundingRect(o1);
				Rect r2 = Imgproc.boundingRect(o2);
				int result = Double.compare(r1.tl().y, r2.tl().y);
				return result;
			}
		} );

		//parse through top/bot sorted list, add to final list
		char[] answers = new char[30];
		for(int i = 0; i < answers.length; i++){
			answers[i] = 'n';
		}
		for(int i = 0; i < 15; i++){
			List<MatOfPoint> tempCntrs = new ArrayList<>();
			for(int j = 0; j < 10; j++){
				tempCntrs.add(fixedCntrs.get(i*10+j));
			}
			Collections.sort(tempCntrs, new Comparator<MatOfPoint>(){
				@Override
				public int compare(MatOfPoint o1, MatOfPoint o2){
					Rect r1 = Imgproc.boundingRect(o1);
					Rect r2 = Imgproc.boundingRect(o2);
					int result = 0;
					double total = r1.tl().y/r2.tl().y;
					if (total >= 0.9 && total <= 1.4 ){
						result = Double.compare(r1.tl().x, r2.tl().x);
					}

					return result;
				}
			});

			for(int j = 0; j < 2; j++){
				double max = 0;
				int maxPos = 5;
				for(int k = 0; k < 5; k++){
					Rect rect = Imgproc.boundingRect(tempCntrs.get(j*5+k));
					Mat mask = binary.submat(rect);
					int total = Core.countNonZero(mask);
					double pixel = total/Imgproc.contourArea(tempCntrs.get(j*5+k))*100;
					if(pixel > max && pixel > 30){
						max = pixel;
						maxPos = k;
					}
				}

				char val = 'n';
				switch(maxPos) {
				case 0:
					val = 'a';
					break;
				case 1:
					val = 'b';
					break;
				case 2:
					val = 'c';
					break;
				case 3:
					val = 'd';
					break;
				case 4:
					val = 'e';
					break;
				}

				answers[i+j*15] = val;

			}

		}

		for(int i = 0; i < answers.length; i++){
			System.out.print(answers[i]+" ");
		}

	
	}
	
	

	public static void main(String[] args) {
		// Load the native library.
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		run(args);
		
	}
}
