package com.temprovich.schema.module;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.temprovich.schema.Schema;
import com.temprovich.schema.lexer.Lexer;
import com.temprovich.schema.report.ReportLibrary;

public class ModuleProcessor {

    private String path;
    private String sourceFileName;
    private List<String> modules;
    private StringBuilder source;

    public ModuleProcessor(String path) {
        int idx = path.lastIndexOf("/") + 1;
        if (idx == 0) {
            idx = path.lastIndexOf("\\") + 1;
        }

        this.path = path.substring(0, idx);
        this.sourceFileName = path.substring(idx, path.length());
        this.modules = new ArrayList<String>();
        this.source = new StringBuilder();
    }

    public String process() {
        determineModules(path + sourceFileName);
        modules.add(sourceFileName);

        for (int i = 0; i < modules.size(); i++) {
            String module = modules.get(i);

            try (var br = new BufferedReader(new FileReader(this.path + module))) {
                String line;
    
                while ((line = br.readLine()) != null) {
                    if (!line.startsWith(Lexer.KW_USE)) {
                        source.append(line)
                              .append(System.lineSeparator());
                    }
                }
            } catch (IOException e) {
                Schema.reporter.error(ReportLibrary.NON_EXISTENT_FILE, path + module);
                System.exit(Schema.EXIT_CODE__ERROR);
            }
        }

        return source.toString();
    }

    private void determineModules(String path) {
        if (modules.contains(path)) {
            return;
        }
        
        try (var br = new BufferedReader(new FileReader(path))) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.startsWith(Lexer.KW_USE)) {
                    String[] modules = line.substring(4, line.length() - 1).split(",");

                    for (int i = 0; i < modules.length; i++) {
                        String module = modules[i].trim();
                        if (!module.contains(".")) {
                            module += Schema.EXTENSIONS[0];
                        }

                        determineModules(this.path + module);
                        if (!this.modules.contains(module)) {
                            this.modules.add(module);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Schema.reporter.error(ReportLibrary.NON_EXISTENT_FILE, path);
            System.exit(Schema.EXIT_CODE__ERROR);
        }
    }
}
