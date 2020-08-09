package com.example.fileUpload;

        import java.io.File;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.util.ArrayList;
        import java.util.Collections;
        import java.util.Comparator;
        import java.util.List;
        import java.util.stream.Collectors;

        import org.opencv.core.*;
        import org.opencv.highgui.HighGui;
        import org.opencv.imgcodecs.Imgcodecs;
        import org.opencv.imgproc.Imgproc;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.core.io.FileSystemResource;
        import org.springframework.core.io.Resource;
        import org.springframework.http.HttpHeaders;
        import org.springframework.http.ResponseEntity;
        import org.springframework.stereotype.Controller;
        import org.springframework.ui.Model;
        import org.springframework.web.bind.annotation.*;
        import org.springframework.web.multipart.MultipartFile;
        import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
        import org.springframework.web.servlet.mvc.support.RedirectAttributes;

        import com.example.fileUpload.storage.StorageFileNotFoundException;
        import com.example.fileUpload.storage.StorageService;

@Controller
public class FileUploadController {

    private final StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @GetMapping("/")
    public String boot() throws IOException {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        return "equiscanHome";
    }



    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {

        Resource file = storageService.loadAsResource(filename);
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + file.getFilename() + "\"").body(file);


    }

//    @PostMapping("/listUploadedFiles")
//    public String handleFileUpload(@RequestParam("file") MultipartFile file,
//                                   RedirectAttributes redirectAttributes) {
//
//        storageService.store(file);
//        redirectAttributes.addFlashAttribute("message",
//                "You successfully uploaded " + file.getOriginalFilename() + "!");
//
//        return "redirect:/";
//    }

    @ExceptionHandler(StorageFileNotFoundException.class)
    public ResponseEntity<?> handleStorageFileNotFound(StorageFileNotFoundException exc) {
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/uploadForm")
    public String uploadForm() {
        return "uploadForm";
    }

    @GetMapping("/submit")
    public String submit(Model model) throws IOException {

        model.addAttribute("files", storageService.loadAll().map(
                path -> MvcUriComponentsBuilder.fromMethodName(FileUploadController.class,
                        "serveFile", path.getFileName().toString()).build().toUri().toString())
                .collect(Collectors.toList()));



        return "submit";
    }

    @PostMapping("/submit")
    public String submitPost(@RequestParam("file") MultipartFile[] multipleFiles) throws IOException {
        for (MultipartFile file: multipleFiles) {
            storageService.store(file);
            File convFile = new File(file.getOriginalFilename());
            convFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(convFile);
            fos.write(file.getBytes());
            fos.close();

            String defaultFile = file.getOriginalFilename();
            //System.out.println(convFile.getAbsolutePath());
            Mat src = Imgcodecs.imread(convFile.getAbsolutePath());
            //check if loaded properly
            if(src.empty()) {
                System.out.println("Error opening image.");
                System.out.println("Program Arguments: [image_name -- default "  + defaultFile +"] \n");
                System.exit(-1);
            }
            //System.out.println("2");

            //create binary image (inverted, b/w, two tone)
            //create binary image (inverted, b/w, two tone)
            Mat gray = new Mat(src.rows(), src.cols(), src.type());
            Imgproc.cvtColor(src, gray, Imgproc.COLOR_BGR2GRAY);
            Mat binary = new Mat(src.rows(), src.cols(), src.type(), new Scalar(0));
            Imgproc.threshold(gray, binary, 150, 255, Imgproc.THRESH_BINARY_INV);

            //find contours
            List<MatOfPoint> cntrs = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(binary, cntrs, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            //sort through contours to find the ones that actually are answer boxes
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
                if(boundRect[i].width > 60 && boundRect[i].height > 60 && ar <= 1.2 && ar >= 0.8){ //checks if are big enough and aspect ratio is close to 1
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
                    int maxPos = 0;
                    for(int k = 0; k < 5; k++){
                        Rect rect = Imgproc.boundingRect(tempCntrs.get(j*5+k));
                        Mat mask = binary.submat(rect);
                        int total = Core.countNonZero(mask);
                        double pixel = total/Imgproc.contourArea(tempCntrs.get(j*5+k))*100;
                        if(pixel > max){
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
                //			for(int k = 0; k < tempCntrs.size(); k++){
                //				Rect rect = Imgproc.boundingRect(tempCntrs.get(k));
                //				System.out.println(rect.x + ", "+rect.y);
                //			}
                //			System.out.println();

            }

            for(int i = 0; i < answers.length; i++){
                System.out.print(answers[i]+" ");
            }



            Imgproc.resize(src, src,  new Size(src.cols()/4, src.rows()/4), 0, 0, Imgproc.INTER_AREA);
            Imgproc.resize(binary, binary,  new Size(binary.cols()/4, binary.rows()/4), 0, 0, Imgproc.INTER_AREA);
            Imgproc.resize(drawing, drawing,  new Size(drawing.cols()/4, drawing.rows()/4), 0, 0, Imgproc.INTER_AREA);

//            HighGui.waitKey();
//
//            System.out.println("10");

            //System.exit(0);

        }



        return "submit";
        //backend
    }

    @GetMapping("/home")
    public String home() {
        return "equiscanHome";
    }
}