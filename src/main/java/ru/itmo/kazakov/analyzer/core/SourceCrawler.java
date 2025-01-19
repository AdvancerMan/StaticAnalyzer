package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface SourceCrawler {

    Stream<File> crawlSources(@Nonnull Path rootPath) throws IOException;
}
