package ene.eneform.colours.service;

import ene.eneform.colours.domain.WikipediaImage;
import ene.eneform.colours.repository.WikipediaImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WikipediaImageService {
    private final WikipediaImageRepository imageRepository;

    public void insertWikipediaImage(String owner, String jacket, String sleeves, String cap, String description, String comments, boolean override) {
        imageRepository.findById(owner).ifPresentOrElse(
                wi -> {
                    if (override) {
                        WikipediaImage save = wi.onUpdate(jacket, sleeves, cap, description, comments);
                        imageRepository.save(save);
                    }
                },
                () -> {
                    WikipediaImage wi = WikipediaImage.onCreate(owner, jacket, sleeves, cap, description, comments);
                    imageRepository.save(wi);
                }
        );
    }
    List<WikipediaImage> selectWikipediaOwners(String[] owners) {
        return imageRepository.findByOwnerIn(Arrays.asList(owners));
    }

    void updateTimestamp(String owner) {
        imageRepository.findById(owner).ifPresentOrElse(
                imageRepository::save
                ,
                () -> {
                    log.error("Update timestamp not found {}", owner);
                }
        );

    }
}
