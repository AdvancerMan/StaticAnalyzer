package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface AnalyzerWarningCrawler {

    Stream<SourceAwareAnalyzerWarning> crawl(@Nonnull Path rootPath) throws IOException;
}
