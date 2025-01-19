package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;
import java.util.List;

public interface StaticAnalyzerRuleState {

    @Nonnull
    List<AnalyzerWarning> getWarnings();
}
