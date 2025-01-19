package ru.itmo.kazakov.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import ru.itmo.kazakov.analyzer.core.*;
import ru.itmo.kazakov.analyzer.rule.StaticAnalyzerRule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Expected 1 argument: root sources path");
            return;
        }
        Path rootPath = Path.of(args[0]);

        List<StaticAnalyzerRule<? extends StaticAnalyzerRuleState>> analyzerRules = List.of(
        );

        StaticAnalyzerImpl staticAnalyzer = new StaticAnalyzerImpl(analyzerRules);
        SourceCrawlerImpl sourceCrawler = new SourceCrawlerImpl();
        JavaParser javaParser = new JavaParser();
        javaParser
                .getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        AnalyzerWarningCrawlerImpl analyzerWarningCrawler = new AnalyzerWarningCrawlerImpl(
                staticAnalyzer,
                sourceCrawler,
                javaParser
        );

        Stream<SourceAwareAnalyzerWarning> warningsStream = analyzerWarningCrawler.crawl(rootPath);

        AnalyzerWarningsPrettyPrinterImpl analyzerWarningsPrettyPrinter = new AnalyzerWarningsPrettyPrinterImpl();
        LongSummaryStatistics totalWarnings = warningsStream.collect(Collectors.summarizingLong(warning -> {
            analyzerWarningsPrettyPrinter.prettyPrint(warning);
            return 1;
        }));
        analyzerWarningsPrettyPrinter.prettyPrint(new AnalyzerWarningStatistics(totalWarnings.getSum()));

        if (totalWarnings.getSum() > 0) {
            throw new RuntimeException("Static analysis found warnings");
        }
    }
}
