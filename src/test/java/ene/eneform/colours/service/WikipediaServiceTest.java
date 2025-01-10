package ene.eneform.colours.service;

import ene.eneform.colours.ENEColoursDBLibrary2025Application;
import ene.eneform.colours.repository.WikipediaImageRepository;
import ene.eneform.mero.service.MeroService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.awt.*;
import java.io.IOException;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ENEColoursDBLibrary2025Application.class)
public class WikipediaServiceTest {
    private static String COLOURS = "Red, blue star, green cap, white diamonds";
    private static String OWNER_NAME="Mr K Abdulla";

    @Autowired
    private WikipediaService service;

    @Test
    void getOwnerFileName() {
        String output = service.getOwnerFileName(OWNER_NAME);
        log.info(output);
    }
    @Test
    void createImageContent() {
        try {
            String output = service.createImageContent(COLOURS, "en", false);
            log.info(output);
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }

}
