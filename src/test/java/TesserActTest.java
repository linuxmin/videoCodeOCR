import java.io.File;

import org.junit.Before;
import org.junit.Test;

import at.javaprofi.ocr.frame.api.service.FrameExtractorService;
import at.javaprofi.ocr.frame.backend.service.FrameExtractorServiceImpl;
import at.javaprofi.ocr.io.api.StorageProperties;
import at.javaprofi.ocr.io.api.service.FileService;
import at.javaprofi.ocr.io.backend.service.FileServiceImpl;
import at.javaprofi.ocr.recognition.api.service.RecognitionService;
import at.javaprofi.ocr.recognition.backend.service.RecognitionServiceImpl;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TesserActTest
{

    private FrameExtractorService frameExtractorService;
    private RecognitionService recognitionService;
    private static final String FAST_FILE = "http://localhost:8083/files/fast.mp4";
    private static final String STANDARD_FILE = "http://localhost:8083/files/standard.mp4";
    private static final String BEST_FILE = "http://localhost:8083/files/best.mp4";

    @Before
    public void setUp()
    {
        final FileService fileService = new FileServiceImpl(new StorageProperties());
        frameExtractorService = new FrameExtractorServiceImpl(fileService);
        recognitionService = new RecognitionServiceImpl(fileService);
    }

    @Test
    public void processImage()
    {
        // Rectangle rectangle = new Rectangle(422, 90, 532, 755);
        // frameExtractorService.extractFrames(FILE_NAME, rectangle);

        recognitionService.extractTextFromFramesToJSON(FAST_FILE, true, "fast");
        recognitionService.extractTextFromFramesToJSON(STANDARD_FILE, true, "standard");
        recognitionService.extractTextFromFramesToJSON(BEST_FILE, true, "best");
    }

    @Test
    public void uploadSingleImage()
    {
        final Tesseract tesseract = new Tesseract();
        //   tesseract.setDatapath("/usr/local/Cellar/tesseract/4.1.1/share/tessdata");
        tesseract.setDatapath("/usr/local/Cellar/tesseract/4.1.1/share/traineddata_fast");
        tesseract.setLanguage("eng");
        tesseract.setHocr(true);

        String output = null;

        try
        {
            final String userRunDir = System.getProperties().getProperty("user.dir");
            final File file = new File(userRunDir + "/src/test/resources/screenshot2.png");
            output = tesseract.doOCR(file);
            System.out.println(output);
        }
        catch (TesseractException e)
        {
            e.printStackTrace();
        }
    }

}
