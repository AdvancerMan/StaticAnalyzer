package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;

public class AnalyzerWarningsPrettyPrinterImpl implements AnalyzerWarningsPrettyPrinter {

    @Override
    public void prettyPrint(@Nonnull final SourceAwareAnalyzerWarning analyzerWarning) {
        System.err.print("Warning in " + analyzerWarning.sourceFilePath().getFileName());
        analyzerWarning
                .warning()
                .beginPosition()
                .ifPresentOrElse(
                        beginPosition -> System.err.print(" starting at " + beginPosition),
                        () -> System.err.println(" starting at unknown position")
                );

        analyzerWarning
                .warning()
                .endPosition()
                .ifPresent(endPosition -> System.err.print(" and ending at " + endPosition));

        System.err.println(" with message " + analyzerWarning.warning().message());
    }

    @Override
    public void prettyPrint(@Nonnull final AnalyzerWarningStatistics analyzerWarningStatistics) {
        System.err.println("Found " + analyzerWarningStatistics.warningsCount() + " total warnings");
    }
}
