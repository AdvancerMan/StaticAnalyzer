package ru.itmo.kazakov.analyzer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import ru.itmo.kazakov.analyzer.core.*;
import ru.itmo.kazakov.analyzer.rule.StaticAnalyzerRule;
import ru.itmo.kazakov.analyzer.rule.VariableCouldBeFinalRule;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(final String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Expected 1 argument: root sources path");
            return;
        }
        final Path rootPath = Path.of(args[0]);

        final List<StaticAnalyzerRule<? extends StaticAnalyzerRuleState>> analyzerRules = List.of(
                new VariableCouldBeFinalRule()
        );

        final StaticAnalyzerImpl staticAnalyzer = new StaticAnalyzerImpl(analyzerRules);
        final SourceCrawlerImpl sourceCrawler = new SourceCrawlerImpl();
        final JavaParser javaParser = new JavaParser();
        javaParser
                .getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21);

        final AnalyzerWarningCrawlerImpl analyzerWarningCrawler = new AnalyzerWarningCrawlerImpl(
                staticAnalyzer,
                sourceCrawler,
                javaParser
        );

        final Stream<SourceAwareAnalyzerWarning> warningsStream = analyzerWarningCrawler.crawl(rootPath);

        final AnalyzerWarningsPrettyPrinterImpl analyzerWarningsPrettyPrinter = new AnalyzerWarningsPrettyPrinterImpl();
        final LongSummaryStatistics totalWarnings = warningsStream.collect(Collectors.summarizingLong(warning -> {
            analyzerWarningsPrettyPrinter.prettyPrint(warning);
            return 1;
        }));
        analyzerWarningsPrettyPrinter.prettyPrint(new AnalyzerWarningStatistics(totalWarnings.getSum()));

        if (totalWarnings.getSum() > 0) {
            throw new RuntimeException("Static analysis found warnings");
        }
    }
}
