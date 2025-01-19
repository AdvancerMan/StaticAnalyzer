package ru.itmo.kazakov.analyzer.core;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.SKIP_SUBTREE;

public class SourceCrawlerImpl implements SourceCrawler {

    private static final Pattern JAVA_IDENTIFIER =
            Pattern.compile("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*");

    @Override
    public Stream<File> crawlSources(@Nonnull Path rootPath) throws IOException {
        final List<File> sources = new ArrayList<>();

        Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (!attrs.isDirectory() && file.toString().endsWith(".java")) {
                    sources.add(file.toFile());
                }
                return CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return isSensibleDirectoryToEnter(dir) ? CONTINUE : SKIP_SUBTREE;
            }
        });

        return sources.stream();
    }

    private boolean isSensibleDirectoryToEnter(Path directory) throws IOException {
        final String directoryToEnter = directory.getFileName().toString();
        final boolean directoryIsAValidJavaIdentifier =
                JAVA_IDENTIFIER.matcher(directoryToEnter).matches();

        return !Files.isHidden(directory) && directoryIsAValidJavaIdentifier;
    }
}
