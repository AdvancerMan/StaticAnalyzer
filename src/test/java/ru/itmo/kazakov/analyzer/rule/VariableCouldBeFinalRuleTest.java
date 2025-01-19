package ru.itmo.kazakov.analyzer.rule;

import com.github.javaparser.Position;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;
import ru.itmo.kazakov.analyzer.core.AnalyzerWarning;
import ru.itmo.kazakov.analyzer.core.StaticAnalyzerRuleState;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("OptionalGetWithoutIsPresent")
class VariableCouldBeFinalRuleTest {

    private static void assertWarningPositions(
            @Nonnull Set<Position> expectedPositions,
            @Nonnull StaticAnalyzerRuleState actualRuleState
    ) {
        Set<Position> positions = actualRuleState
                .getWarnings()
                .stream()
                .map(AnalyzerWarning::beginPosition)
                .map(Optional::get)
                .collect(Collectors.toSet());

        assertEquals(
                expectedPositions,
                Set.copyOf(positions)
        );
    }

    @Test
    public void simpleNoWarningsCorrectFinalDeclaration() {
        CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                final int x = 0;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Collections.emptySet(),
                ruleState
        );
    }

    @Test
    public void declarationAndAssignmentOnOneLine() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x = 0;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9)),
                ruleState
        );
    }

    @Test
    public void declarationAndAssignmentOnMultiLine() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x;
                                int y;
                                x = 0;
                                y = 1;
                                y = 2;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9)),
                ruleState
        );
    }

    @Test
    public void declarationAndAssignmentOnOneLineMultiDeclaration() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x = 0, y = 1, z = 2;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9)),
                ruleState
        );
    }

    @Test
    public void declarationAndAssignmentOnMultiLineMultiDeclaration() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x = 0, y, z = 2;
                                y = 1;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9)),
                ruleState
        );
    }

    @Test
    public void declarationAndAssignmentOnMultiLineMultiDeclarationNoWarning() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x = 0, y, z = 2;
                                y = 1;
                                x = 3;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Collections.emptySet(),
                ruleState
        );
    }

    @Test
    public void correctAndIncorrectDeclaration() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x = 0;
                                final int y = 1;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9)),
                ruleState
        );
    }

    @Test
    public void parametersShouldBeFinal() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(String[] args) throws Exception {
                                // no operations
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(3, 29)),
                ruleState
        );
    }

    @Test
    public void multipleParametersShouldBeFinal() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(String[] args, String arg1, String arg2) throws Exception {
                                // no operations
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(3, 29),
                        new Position(3, 44),
                        new Position(3, 57)),
                ruleState
        );
    }

    @Test
    public void parameterShouldNotBeFinal() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(String[] args) throws Exception {
                                args = null;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Collections.emptySet(),
                ruleState
        );
    }

    @Test
    public void multipleAssignmentIsCorrectlyDetected() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                final int x;
                                int y;
                                int z;
                                z = y = x = z = 1;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(5, 9)),
                ruleState
        );
    }

    @Test
    public void forCycleShouldCancelFinalDeclaration() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x;
                                for (int i = 0; i < 10; i++) {
                                    x = 1;
                                }
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Collections.emptySet(),
                ruleState
        );
    }

    @Test
    public void forCycleParameterCanBeFinal() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                for (int i = 0;;) {
                                    // no operations
                                }
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 14)),
                ruleState
        );
    }

    @Test
    public void whileCycleShouldCancelFinalDeclaration() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int y;
                                int x;
                                while (true) {
                                    x = 1;
                                }
                                y = 2;
                            }
                        }
                        """
        );


        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9)),
                ruleState
        );
    }

    @Test
    public void doWhileCycleShouldCancelFinalDeclaration() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x;
                                do {
                                    x = 1;
                                } while (true);
                            }
                        }
                        """
        );


        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Collections.emptySet(),
                ruleState
        );
    }

    @Test
    public void forEachCycleShouldCancelFinalDeclaration() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                int x;
                                for (final String arg : args) {
                                    x = 1;
                                }
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Collections.emptySet(),
                ruleState
        );
    }

    @Test
    public void forEachCycleParameterCanBeFinal() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                for (String arg : args) {
                                    // no operations
                                }
                                for (String arg : args) {
                                    arg = "1";
                                }
                                for (String arg : args) {
                                    arg = "1";
                                    arg = "2";
                                }
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 14)),
                ruleState
        );
    }

    @Test
    public void multipleCodeBlocksDeclareDifferentVariables() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                {
                                    int x;
                                    x = 1;
                                    x = 2;
                                }
                                {
                                    int x;
                                    x = 3;
                                }
                                int x;
                                x = 4;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(10, 13),
                        new Position(13, 9)),
                ruleState
        );
    }

    @Test
    public void testLambdaFunction() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public static void main(final String[] args) throws Exception {
                                Function<Integer, Integer> x = (somethingVariable) -> {
                                    int y = 1;
                                    int z = 2;
                                    z = 3;
                                    somethingVariable = 4;
                                    return somethingVariable;
                                };
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9),
                        new Position(5, 13)),
                ruleState
        );
    }

    @Test
    public void testConstructor() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public Main(String[] args) throws Exception {
                                int x = 1;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(3, 17),
                        new Position(4, 9)),
                ruleState
        );
    }

    @Test
    public void testInitializers() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            {
                                int x;
                                x = 1;
                            }
                            static {
                                int x;
                                x = 1;
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(4, 9),
                        new Position(8, 9)),
                ruleState
        );
    }

    @Test
    public void testNestedBlocksParentShouldNotBeFinal() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public Main(final String[] args) throws Exception {
                                {
                                    int x;
                                    x = 1;
                                    {
                                        int y;
                                        y = 1;
                                    }
                                    x = 1;
                                }
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(8, 17)),
                ruleState
        );
    }

    @Test
    public void testNestedBlocksAllShouldBeFinal() {
        final CompilationUnit compiledSource = StaticJavaParser.parse(
                """
                        package ru.itmo.kazakov.analyzer;
                        public class Main {
                            public Main(final String[] args) throws Exception {
                                {
                                    int x;
                                    x = 1;
                                    {
                                        int y;
                                        y = 1;
                                    }
                                }
                            }
                        }
                        """
        );

        VariableCouldBeFinalRule.State ruleState = new VariableCouldBeFinalRule().analyze(compiledSource);

        assertWarningPositions(
                Set.of(new Position(5, 13),
                        new Position(8, 17)),
                ruleState
        );
    }
}
