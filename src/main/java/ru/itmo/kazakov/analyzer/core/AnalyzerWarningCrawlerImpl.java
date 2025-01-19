package ru.itmo.kazakov.analyzer.core;

import com.github.javaparser.JavaParser;
import com.github.javaparser.JavaParserAdapter;
import com.github.javaparser.ast.CompilationUnit;

import javax.annotation.Nonnull;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class AnalyzerWarningCrawlerImpl implements AnalyzerWarningCrawler {

    private final StaticAnalyzerImpl staticAnalyzer;
    private final SourceCrawlerImpl sourceCrawler;
    private final JavaParserAdapter javaParserAdapter;

    public AnalyzerWarningCrawlerImpl(StaticAnalyzerImpl staticAnalyzer,
                                      SourceCrawlerImpl sourceCrawler,
                                      JavaParser javaParser) {
        this.staticAnalyzer = staticAnalyzer;
        this.sourceCrawler = sourceCrawler;
        this.javaParserAdapter = new JavaParserAdapter(javaParser);
    }

    @Override
    public Stream<SourceAwareAnalyzerWarning> crawl(@Nonnull Path rootPath) throws IOException {
        return sourceCrawler
                .crawlSources(rootPath)
                .flatMap(file -> {
                    final CompilationUnit compiledFile;
                    try {
                        compiledFile = javaParserAdapter.parse(file);
                    } catch (FileNotFoundException e) {
                        System.err.println("Crawled file " + file.getAbsolutePath() + " but could not open it");
                        e.printStackTrace(System.err);
                        return Stream.empty();
                    }

                    Stream<AnalyzerWarning> warnings = staticAnalyzer.analyze(compiledFile);
                    return warnings.map(warning -> new SourceAwareAnalyzerWarning(warning, file.toPath()));
                });
    }
}
