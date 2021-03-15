import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

public class TesserActTest
{

    @Test
    public void uploadSingleImage()
    {
        final Tesseract tesseract = new Tesseract();
        tesseract.setDatapath("/usr/local/Cellar/tesseract/4.1.1/share/tessdata");
        tesseract.setLanguage("eng");

        String output = null;

        try
        {
            final String userRunDir = System.getProperties().getProperty("user.dir");
            final File file = new File(userRunDir + "/src/test/resources/frame_3.png");
            output = tesseract.doOCR(file);
            System.out.println(output);
        }
        catch (TesseractException e)
        {
            e.printStackTrace();
        }

        Assert.assertTrue(StringUtils.containsIgnoreCase(output, "career-defining"));
    }

}
