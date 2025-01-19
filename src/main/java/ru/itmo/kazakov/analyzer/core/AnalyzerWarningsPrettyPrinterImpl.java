package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;

public class AnalyzerWarningsPrettyPrinterImpl implements AnalyzerWarningsPrettyPrinter {

    @Override
    public void prettyPrint(@Nonnull SourceAwareAnalyzerWarning analyzerWarning) {
        System.err.print("Warning in " + analyzerWarning.sourceFilePath().getFileName());
        System.err.print(" starting at " + analyzerWarning.warning().warningBeginPosition());
        analyzerWarning
                .warning()
                .warningEndPosition()
                .ifPresent(endPosition -> System.err.print(" and ending at " + endPosition));

        System.err.println(" with message " + analyzerWarning.warning().message());
    }

    @Override
    public void prettyPrint(@Nonnull AnalyzerWarningStatistics analyzerWarningStatistics) {
        System.err.println("Found " + analyzerWarningStatistics.warningsCount() + " total warnings");
    }
}
