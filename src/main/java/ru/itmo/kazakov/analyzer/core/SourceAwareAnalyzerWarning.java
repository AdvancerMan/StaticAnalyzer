package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;
import java.nio.file.Path;

public record SourceAwareAnalyzerWarning(
        @Nonnull AnalyzerWarning warning,
        @Nonnull Path sourceFilePath
) {
    // no methods
}
