package org.rug.simpletests.web;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.rug.web.WebAnalysisController;
import org.springframework.util.FileSystemUtils;

import javax.servlet.http.HttpServletResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.springframework.mock.web.MockHttpServletResponse;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WebAnalysisControllerTest {
    WebAnalysisController webAnalysisController;

    @Test
    void testWebAnalysisControllerPyne() {
        var requestParameters = new HashMap<String, String>();
        requestParameters.put("project", "https://github.com/darius-sas/pyne.git");
        requestParameters.put("language", "java");
        requestParameters.put("singleVersion", "true");
        this.webAnalysisController = new WebAnalysisController();

        FileSystemUtils.deleteRecursively(Paths.get("./states/pyne").toFile());

        var response = new MockHttpServletResponse();
        var resultResponse = webAnalysisController.analyse(requestParameters, response);
        assertEquals(WebAnalysisController.Result.SUCCESS, resultResponse.getResult());
        assertEquals("No message.", resultResponse.getMessage());

        var secondResultResponse = webAnalysisController.analyse(requestParameters, response);
        assertEquals(WebAnalysisController.Result.SKIPPED, secondResultResponse.getResult());
        assertEquals("No message.", resultResponse.getMessage());

        assertTrue(
                FileSystemUtils.deleteRecursively(Paths.get("./states/pyne").toFile()),
                "The folder was not deleted after performing the analysis."
        );
    }
}