package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;

public interface AnalyzerWarningsPrettyPrinter {

    void prettyPrint(@Nonnull SourceAwareAnalyzerWarning analyzerWarning);

    void prettyPrint(@Nonnull AnalyzerWarningStatistics analyzerWarningStatistics);
}
