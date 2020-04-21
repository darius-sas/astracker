package org.rug.simpletests.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.web.ASTrackerWebRunner;
import org.rug.web.WebAnalysisController;
import org.springframework.util.FileSystemUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ASTrackerWebRunnerTest {
    Map<String,String> requestParameters;
    ASTrackerWebRunner asTrackerWebRunner;

    @Test
    void testWebRunnerPyne() throws Exception {
        this.requestParameters = new HashMap<>();
        this.requestParameters.put("project", "https://github.com/darius-sas/pyne.git");
        this.requestParameters.put("language", "java");
        this.asTrackerWebRunner = new ASTrackerWebRunner(this.requestParameters);

        FileSystemUtils.deleteRecursively(Paths.get("./states/pyne").toFile());

        assertEquals(WebAnalysisController.Result.SUCCESS, asTrackerWebRunner.run());
        assertTrue(Files.exists(Paths.get("./states/pyne")));
        assertTrue(Files.exists(Paths.get("./cloned-projects/pyne")));
        assertTrue(Files.exists(Paths.get("./output-folder/arcanOutput/pyne")));
        assertTrue(Files.exists(Paths.get("./output-folder/trackASOutput/pyne")));
        assertEquals("pyne", this.asTrackerWebRunner.getProjectName());

        assertEquals(WebAnalysisController.Result.SKIPPED, asTrackerWebRunner.run());
        assertTrue(
                FileSystemUtils.deleteRecursively(Paths.get("./states/pyne").toFile()),
                "The folder was not deleted after performing the analysis."
        );
    }
}