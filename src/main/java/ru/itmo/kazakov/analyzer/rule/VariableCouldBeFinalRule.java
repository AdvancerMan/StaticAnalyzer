package ru.itmo.kazakov.analyzer.rule;

import com.github.javaparser.Position;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.UnaryExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithParameters;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.*;
import ru.itmo.kazakov.analyzer.core.AnalyzerWarning;
import ru.itmo.kazakov.analyzer.core.StaticAnalyzerRuleState;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class VariableCouldBeFinalRule extends StaticAnalyzerVisitorRule<VariableCouldBeFinalRule.State> {

    @Nonnull
    @Override
    public State createInitialState() {
        return new State();
    }

    private String constructWarningMessage(@Nonnull final List<String> variableNames) {
        if (variableNames.isEmpty()) {
            return "Unknown variable could be final";
        } else if (variableNames.size() == 1) {
            return "Variable " + variableNames.getFirst() + " could be final";
        } else {
            return "Variables " + variableNames + " could be final";
        }
    }

    @Override
    public void visit(final BlockStmt n, final State state) {
        final Set<VariableDeclarationDescription> parentBlockVariables = Set.copyOf(state.currentBlockVariables);
        state.currentBlockVariables.clear();

        state.currentBlockVariables.addAll(state.nextBlockVariables);
        state.nextBlockVariables.clear();

        super.visit(n, state);

        state.currentBlockVariables.forEach(variableDeclarationDescription -> {
            final long maxAssignmentCount = variableDeclarationDescription.names
                    .stream()
                    .map(state.assignmentsForVariable::remove)
                    .map(Optional::ofNullable)
                    .flatMap(Optional::stream)
                    .mapToLong(it -> it)
                    .max()
                    .orElse(0);

            if (maxAssignmentCount > 1) {
                return;
            }

            state.addWarning(new AnalyzerWarning(
                    constructWarningMessage(variableDeclarationDescription.names()),
                    variableDeclarationDescription.beginPosition(),
                    variableDeclarationDescription.endPosition()
            ));
        });

        state.currentBlockVariables.clear();
        state.currentBlockVariables.addAll(parentBlockVariables);
    }

    private void processMethodParameters(final NodeWithParameters<?> nodeWithParameters, final State state) {
        nodeWithParameters.getParameters()
                .stream()
                .filter(Predicate.not(Parameter::isFinal))
                .peek(parameter -> state.assignmentsForVariable.put(parameter.getNameAsString(), 1L))
                .map(parameter ->
                        new VariableDeclarationDescription(
                                List.of(parameter.getNameAsString()),
                                parameter.getBegin(),
                                parameter.getEnd()
                        )
                )
                .forEach(state.nextBlockVariables::add);
    }

    @Override
    public void visit(final MethodDeclaration n, final State state) {
        processMethodParameters(n, state);
        super.visit(n, state);
    }

    @Override
    public void visit(final ConstructorDeclaration n, final State state) {
        processMethodParameters(n, state);
        super.visit(n, state);
    }

    @Override
    public void visit(final VariableDeclarationExpr n, final State state) {
        if (!n.isFinal()) {
            final List<String> variableNames = n.getVariables().stream().map(NodeWithSimpleName::getNameAsString).toList();
            if (n.getParentNode().stream().anyMatch(parent -> parent instanceof ForStmt || parent instanceof ForEachStmt)) {
                state.nextBlockVariables.add(
                        new VariableDeclarationDescription(variableNames, n.getBegin(), n.getEnd())
                );
            } else {
                state.currentBlockVariables.add(
                        new VariableDeclarationDescription(variableNames, n.getBegin(), n.getEnd())
                );
            }

            n.getVariables().forEach(variable -> {
                final String variableName = variable.getNameAsString();
                assert state.assignmentsForVariable.get(variableName) == null;

                state.assignmentsForVariable.put(
                        variableName,
                        variable.getInitializer().isPresent() ||
                                n.getParentNode().stream().anyMatch(parent -> parent instanceof ForEachStmt)
                                ? 1L : 0L);
            });
        }

        super.visit(n, state);
    }

    @Override
    public void visit(final AssignExpr n, final State state) {
        state.assignmentsForVariable.compute(
                n.getTarget().toString(),
                (name, count) -> count == null ? null : count + 1
        );
        super.visit(n, state);
    }

    @Override
    public void visit(final UnaryExpr n, final State state) {
        final Set<UnaryExpr.Operator> assignmentOperators = Set.of(UnaryExpr.Operator.PREFIX_INCREMENT,
                UnaryExpr.Operator.PREFIX_DECREMENT,
                UnaryExpr.Operator.POSTFIX_INCREMENT,
                UnaryExpr.Operator.POSTFIX_DECREMENT);

        if (assignmentOperators.contains(n.getOperator())) {
            state.assignmentsForVariable.compute(
                    n.getExpression().toString(),
                    (name, count) -> count == null ? null : count + 1
            );
        }

        super.visit(n, state);
    }

    public void visitCycleNode(final State state, final Consumer<State> visitInnerNodes) {
        final Map<String, Long> parentVariableCounts = Map.copyOf(state.assignmentsForVariable);
        state.assignmentsForVariable.replaceAll((__, count) -> 0L);

        visitInnerNodes.accept(state);

        state.assignmentsForVariable.replaceAll((__, count) -> count * 2);
        parentVariableCounts.forEach((name, count) -> state.assignmentsForVariable.merge(name, count, Long::sum));
    }

    @Override
    public void visit(final DoStmt n, final State state) {
        visitCycleNode(state, (innerState) -> super.visit(n, innerState));
    }

    @Override
    public void visit(final WhileStmt n, final State state) {
        visitCycleNode(state, (innerState) -> super.visit(n, innerState));
    }

    @Override
    public void visit(final ForStmt n, final State state) {
        visitCycleNode(state, (innerState) -> {
            n.getInitialization().forEach(p -> p.accept(this, innerState));
            n.getCompare().ifPresent(l -> l.accept(this, innerState));
            n.getUpdate().forEach(p -> p.accept(this, innerState));
            n.getComment().ifPresent(l -> l.accept(this, innerState));
            n.getBody().accept(this, innerState);
        });
    }

    @Override
    public void visit(final ForEachStmt n, final State state) {
        visitCycleNode(state, (innerState) -> {
            n.getVariable().accept(this, innerState);
            n.getIterable().accept(this, innerState);
            n.getComment().ifPresent(l -> l.accept(this, innerState));
            n.getBody().accept(this, innerState);
        });
    }

    private record VariableDeclarationDescription(
            List<String> names,
            Optional<Position> beginPosition,
            Optional<Position> endPosition
    ) {
        // no methods
    }

    public static class State implements StaticAnalyzerRuleState {

        private final List<AnalyzerWarning> warnings = new ArrayList<>();
        private final Map<String, Long> assignmentsForVariable = new HashMap<>();
        private final Set<VariableDeclarationDescription> nextBlockVariables = new HashSet<>();
        private final Set<VariableDeclarationDescription> currentBlockVariables = new HashSet<>();

        @Nonnull
        @Override
        public List<AnalyzerWarning> getWarnings() {
            return warnings;
        }

        public void addWarning(@Nonnull final AnalyzerWarning analyzerWarning) {
            warnings.add(analyzerWarning);
        }
    }
}
