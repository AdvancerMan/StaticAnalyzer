package ru.itmo.kazakov.analyzer.core;

import com.github.javaparser.Position;

import javax.annotation.Nonnull;
import java.util.Optional;

public record AnalyzerWarning(
        @Nonnull String message,
        @Nonnull Optional<Position> beginPosition,
        @Nonnull Optional<Position> endPosition
) {
    // no methods
}
