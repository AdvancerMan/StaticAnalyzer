package ru.itmo.kazakov.analyzer.rule;

import com.github.javaparser.ast.CompilationUnit;
import ru.itmo.kazakov.analyzer.core.StaticAnalyzerRuleState;

import javax.annotation.Nonnull;

public interface StaticAnalyzerRule<S extends StaticAnalyzerRuleState> {

    @Nonnull
    S analyze(@Nonnull CompilationUnit compiledFile);
}
