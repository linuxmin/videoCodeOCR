package at.javaprofi.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import at.javaprofi.ocr.filestorage.api.StorageProperties;
import at.javaprofi.ocr.filestorage.api.service.FileStorageService;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class OCRApplication
{
    private static final Logger LOG = LoggerFactory.getLogger(OCRApplication.class);

    public static void main(String[] args)
    {
        SpringApplication.run(OCRApplication.class, args);
        LOG.info("videoCodeOCR successfully started!");

    }

    @Bean
    CommandLineRunner init(FileStorageService fileStorageService)
    {
        return (args) -> {
            fileStorageService.init();
        };
    }
}
