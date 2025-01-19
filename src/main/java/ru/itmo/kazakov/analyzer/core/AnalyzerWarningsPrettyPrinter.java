package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;

public interface AnalyzerWarningsPrettyPrinter {

    void prettyPrint(@Nonnull final SourceAwareAnalyzerWarning analyzerWarning);

    void prettyPrint(@Nonnull final AnalyzerWarningStatistics analyzerWarningStatistics);
}
