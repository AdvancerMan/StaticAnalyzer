package ru.itmo.kazakov.analyzer.core;

import com.github.javaparser.ast.CompilationUnit;

import javax.annotation.Nonnull;
import java.util.stream.Stream;

public interface StaticAnalyzer {

    @Nonnull
    Stream<AnalyzerWarning> analyze(@Nonnull CompilationUnit compiledFile);
}
