/*
 * Copyright 2022-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springsource.restbucks;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

/**
 * Verifying modulithic structure and creating documentation for {@link ApplicationModules}.
 *
 * @author Oliver Drotbohm
 */

@Slf4j
class ModularityTests {

    ApplicationModules modules = ApplicationModules.of(Restbucks.class);

    @Test
    void verifiesArchitecture() {

        // System.out.println(modules);

        modules.verify();
    }

    @Test
    void createDocumentation() {
        new Documenter(modules).writeDocumentation();
    }

    @SneakyThrows
    @Test
    void writeSummary() throws IOException {

        String docsPathName = "target/spring-modulith-docs";
        String summaryFileName = "all-docs.adoc";

        Path docsPath = Paths.get(docsPathName);
        Map<String, StringBuilder> fileMap = new TreeMap<>();

        try (Stream<Path> files = Files.list(docsPath)) {
            files
                    .filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals(summaryFileName))
                    .forEach(filePath -> {
                        String fileName = filePath.getFileName().toString();

                        String fileHandle = fileName.substring(0, fileName.lastIndexOf('.'));
                        String relativePath = docsPath.relativize(filePath).toString().replace("\\", "/");

                        // Determine the include directive based on file extension
                        String includeDirective = fileName.endsWith(".puml") ? "plantuml::" : "include::";

                        fileMap.computeIfAbsent(fileHandle, k -> new StringBuilder())
                                .append(includeDirective).append(relativePath).append("[]\n");
                    });
        } catch (IOException e) {
            LOG.warn("Skip writing summary: {} {}", e.getClass().getName(), e.getMessage());
            return;
        }

        if (fileMap.isEmpty()) {
            LOG.warn("Skip writing summary: Nothing to summarize in {} ", docsPathName);
            return;
        }

        // Create summary file
        File indexFile = new File(docsPathName + "/" + summaryFileName);
        try (FileWriter writer = new FileWriter(indexFile)) {
            fileMap.forEach((handle, references) -> {
                try {
                    writer.write("== " + handle + "\n");
                    writer.write(references.toString());
                    writer.write("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

    }

}
