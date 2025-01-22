package ru.itmo.kazakov.analyzer.core;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SourceCrawlerImplTest {

    @Test
    public void testCrawl() throws IOException {
        final Path crawlerDirectory = Path.of("src/test/resources/crawlerTest");

        final Set<File> sourceFiles = new SourceCrawlerImpl()
                .crawlSources(crawlerDirectory)
                .collect(Collectors.toSet());

        assertEquals(
                Set.of(
                        Path.of("src/test/resources/crawlerTest/java.java").toFile(),
                        Path.of("src/test/resources/crawlerTest/very/deep/package/java.java").toFile(),
                        Path.of("src/test/resources/crawlerTest/actualPackage/java.java").toFile()
                ),
                sourceFiles
        );
    }
}
