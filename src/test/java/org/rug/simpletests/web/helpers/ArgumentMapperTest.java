package org.rug.simpletests.web.helpers;

import org.junit.jupiter.api.Test;
import org.rug.web.helpers.ArgumentMapper;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArgumentMapperTest {

    @Test
    void shouldCreateAllOptions() {
        Map<String, String> args = new HashMap<>();
        args.put("help", "");
        args.put("printSimilarity", "");
        args.put("similarity", "");
        args.put("pCharacteristics", "");
        args.put("printCharacteristics", "");
        args.put("chars", "");
        args.put("printChars", "");
        args.put("runProjectSize", "");
        args.put("nonConsecutive", "");
        args.put("nonConsec", "");
        args.put("componentCharacteristics", "");
        args.put("language", "java");
        args.put("project", "pyne");
        args.put("singleVersion", "");
        args.put("runTracker", "");
        args.put("startDate", "");


        Path arcanPath = Paths.get("fake-path;");
        Path arcanCPath = Paths.get("fake-path;");
        Path outputFolder = Paths.get("fake-path;");
        var inputDir = Paths.get(outputFolder.toString(), "arcanOutput", "pyne");


        ArgumentMapper mapper = new ArgumentMapper(arcanPath, arcanCPath, outputFolder, Paths.get("cloned-projects"), args);
        assertTrue(mapper.getArgumentsMapping().length > 0);
        inputDir.toFile().delete();
        Paths.get(outputFolder.toString(), "arcanOutput").toFile().delete();
        outputFolder.toFile().delete();

    }
}