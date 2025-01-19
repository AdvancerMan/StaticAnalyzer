package ru.itmo.kazakov.analyzer.rule;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import ru.itmo.kazakov.analyzer.core.StaticAnalyzerRuleState;

import javax.annotation.Nonnull;

public abstract class StaticAnalyzerVisitorRule<S extends StaticAnalyzerRuleState> extends VoidVisitorAdapter<S> implements StaticAnalyzerRule<S> {

    @Nonnull
    public abstract S createInitialState();

    @Nonnull
    public S analyze(@Nonnull final CompilationUnit compiledFile) {
        final S state = createInitialState();
        compiledFile.accept(this, state);
        return state;
    }
}
