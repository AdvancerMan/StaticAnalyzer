package ru.itmo.kazakov.analyzer.core;

import com.github.javaparser.ast.CompilationUnit;
import ru.itmo.kazakov.analyzer.rule.StaticAnalyzerRule;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.stream.Stream;

public class StaticAnalyzerImpl implements StaticAnalyzer {

    @Nonnull
    final List<StaticAnalyzerRule<? extends StaticAnalyzerRuleState>> staticAnalyzerRules;

    public StaticAnalyzerImpl(
            @Nonnull final List<StaticAnalyzerRule<? extends StaticAnalyzerRuleState>> staticAnalyzerRules
    ) {
        this.staticAnalyzerRules = staticAnalyzerRules;
    }

    @Nonnull
    @Override
    public Stream<AnalyzerWarning> analyze(@Nonnull final CompilationUnit compiledFile) {
        return staticAnalyzerRules
                .stream()
                .flatMap(rule -> rule.analyze(compiledFile).getWarnings().stream());
    }
}
