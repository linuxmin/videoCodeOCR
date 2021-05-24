import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import at.javaprofi.ocr.frame.backend.service.FrameExtractorServiceImpl;
import at.javaprofi.ocr.io.api.StorageProperties;
import at.javaprofi.ocr.io.backend.service.FileServiceImpl;

public class PathTest
{

    public static final String UPLOAD_DIR = "io-dir";
    public static final String EXTRACTED_DIR = "extracted-dir";

    @Test
    public void testPathOperations()
    {

        /*
        Paths.get retrieves a path from given strings or URI within the root directory the application is running
        example with application started from IDE with project root /Users/brian/OCR
        Path uploadDir = Paths.get("io-dir")
        uploadDir.toString() evaluates to "/Users/brian/OCR/io-dir"
         */
        final Path uploadDir = Paths.get(UPLOAD_DIR);
        final Path extractedDir = Paths.get(EXTRACTED_DIR);

        assertEquals(UPLOAD_DIR, uploadDir.toString());
        assertEquals(EXTRACTED_DIR, extractedDir.toString());

        FrameExtractorServiceImpl frameExtractorService =
            new FrameExtractorServiceImpl(new FileServiceImpl(new StorageProperties()));
        //     frameExtractorService.extractCodeFromVideo("file");

        /*
        if Paths.get(String names...) is called with an empty string, then the resulting absolute path is the running path
        the system property 'user.dir' is the string representation of this absolute path
         */
        final Path currentRunningDir = Paths.get("");
        assertEquals(currentRunningDir.toAbsolutePath(), Paths.get(System.getProperty("user.dir")));

        /*
        The call: Path resolvedPath = basePath.resolve(otherPath) produces the resolvedPath which is the new base path of the other path
        e.g. the basePath is 'brian/downloads' and other path is 'documents/pictures' then the resolvedPath is 'brian/downloads/documents/pictures'
         */
        final Path resolvedPicturePath = Paths.get("brian", "downloads", "documents", "pictures");
        final Path basePath = Paths.get("brian", "downloads");
        final Path picturePath = Paths.get("documents", "pictures");

        assertEquals(basePath.resolve(picturePath), resolvedPicturePath);
        assertEquals(currentRunningDir.toAbsolutePath().resolve(uploadDir), uploadDir.toAbsolutePath());

    }

}

