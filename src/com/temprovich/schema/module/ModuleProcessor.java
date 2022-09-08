package com.temprovich.schema.module;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.temprovich.schema.Preprocessor;
import com.temprovich.schema.Schema;
import com.temprovich.schema.report.ReportLibrary;

public class ModuleProcessor implements Preprocessor {

    private String path;
    private String sourceFileName;
    private Set<String> modules;

    public ModuleProcessor(Path path) {
        String fqf = path.toFile().getAbsoluteFile().toString();
        int idx = fqf.lastIndexOf("/") + 1;
        this.path = fqf.substring(0, idx);
        this.sourceFileName = fqf.substring(idx, fqf.length());
        this.modules = new HashSet<String>();
        modules.add(sourceFileName);
    }

    @Override
    public String process() {
        StringBuilder lines = new StringBuilder();

        try (var br = new BufferedReader(new FileReader(path + sourceFileName))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("use")) {
                    String[] modules = line.substring(4, line.length() - 1).split(",");
                    Arrays.stream(modules).forEach(m -> {
                        String module = m.trim();
                        if (this.modules.contains(module)) {
                            Schema.reporter.error(ReportLibrary.DUPLICATE_MODULE, module);
                        } else {
                            this.modules.add(module);
                            lines.append(processModule(module));
                        }
                    });
                    continue;
                }
                
                lines.append(line);
                lines.append(System.lineSeparator());
            }
        } catch (IOException e) {
            Schema.reporter.error(ReportLibrary.NON_EXISTENT_FILE, path);
            System.exit(Schema.EXIT_CODE__ERROR);
        }

        return lines.toString();
    }

    private String processModule(String module) {
        if (!module.contains(".")) {
            module += Schema.EXTENSIONS[0];
        }

        StringBuilder lines = new StringBuilder();
        try (var br = new BufferedReader(new FileReader(path + module))) {
            String line;
        
            while ((line = br.readLine()) != null) {
                lines.append(line);
                lines.append(System.lineSeparator());
            }
        } catch (IOException e) {
            Schema.reporter.error(ReportLibrary.NON_EXISTENT_FILE, path);
            System.exit(Schema.EXIT_CODE__ERROR);
        }
        return lines.toString();
    }
}
