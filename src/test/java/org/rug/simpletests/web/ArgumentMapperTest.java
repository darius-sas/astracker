package org.rug.simpletests.web;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.web.helpers.ArgumentMapper;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ArgumentMapperTest {
    Map<String,String> requestParameters;

    @BeforeAll
    void init(){
        this.requestParameters = new HashMap<>();
        this.requestParameters.put("project", "https://github.com/darius-sas/pyne.git");
        this.requestParameters.put("language", "java");
    }

    @Test
    void testGetArgumentsPyne() throws IOException, GitAPIException {

        var argumentMapper = new ArgumentMapper(
                Paths.get("arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar"),
                Paths.get("arcan/Arcan-c-1.0.2-RELEASE-jar-with-dependencies.jar"),
                Paths.get("./output-folder"),
                Paths.get("./cloned-projects"),
                this.requestParameters
        );

        String[] expectedMapping = {
                "-o",
                Paths.get("./output-folder").toString(),
                "-i",
                Paths.get("./output-folder/arcanOutput/pyne").toString(),
                "-projectName",
                "pyne",
                "-runArcan",
                Paths.get("arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar").toString(),
                "-gitRepo",
                Paths.get("./cloned-projects/pyne").toAbsolutePath().toString(),
                "-javaProject",
        };

        var mapping = argumentMapper.getArgumentsMapping();
        assertEquals(expectedMapping.length, mapping.length);
        assertLinesMatch(Arrays.asList(expectedMapping), Arrays.asList(mapping));
    }

    @Test
    void testGetArgumentsPyneSingleVersion() throws IOException, GitAPIException {
        var singleVersionParams = new HashMap<>(this.requestParameters);
        singleVersionParams.put("singleVersion", "true");


        var argumentMapper = new ArgumentMapper(
                Paths.get("arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar"),
                Paths.get("arcan/Arcan-c-1.0.2-RELEASE-jar-with-dependencies.jar"),
                Paths.get("./output-folder"),
                Paths.get("./cloned-projects"),
                singleVersionParams
        );
        String[] expectedMapping = {
                "-o",
                Paths.get("./output-folder").toString(),
                "-i",
                Paths.get("./output-folder/arcanOutput/pyne").toString(),
                "-projectName",
                "pyne",
                "-runArcan",
                Paths.get("arcan/Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar").toString(),
                "-gitRepo",
                Paths.get("./cloned-projects/pyne").toAbsolutePath().toString(),
                "-singleVersion",
                "-javaProject",
        };

        var mapping = argumentMapper.getArgumentsMapping();
        assertEquals(expectedMapping.length, mapping.length);
        assertLinesMatch(Arrays.asList(expectedMapping), Arrays.asList(mapping));
    }
}