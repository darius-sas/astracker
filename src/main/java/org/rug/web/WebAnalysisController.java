package org.rug.web;

import org.rug.web.credentials.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Class used as the controller for the API. Will handle the mapping to specific URLs.
 */
@RestController
public class WebAnalysisController {

    private final Logger logger = LoggerFactory.getLogger(WebAnalysisController.class);

    /**
     * Can be used to perform an analysis on a GitHub repository. Required parameters are
     * 'project' -> the link to the GitHub repository (ending in .git)
     * 'language' -> the language of the project (eg. java)
     *
     */
    @RequestMapping(value = {"/analyse"}, method = {RequestMethod.GET, RequestMethod.POST},  produces={ "application/json"})
    public ResultResponse analyse(
            @RequestParam Map<String,String> requestParameters,
            @RequestBody(required = false) Credentials credentials,
            HttpServletResponse response) {
        if (credentials == null){
            credentials = Credentials.noCredentials();
        }
        var runner = new ASTrackerWebRunner(requestParameters, credentials);
        ResultResponse result = new ResultResponse();
        try {
            long start = java.lang.System.nanoTime();
            var analysisResult = runner.run();
            long end = java.lang.System.nanoTime();
            response.setStatus(HttpServletResponse.SC_OK);
            result.setResult(analysisResult);
            result.setProject(runner.getProjectName());
            result.setTimeElapsed(end - start);
            result.setMessage("No message.");
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.setResult(Result.FAILED);
            result.setMessage(e.getMessage());
            result.setTimeElapsed(0);
        }
        return result;
    }

    public static final class ResultResponse{
        String project;
        Result result;
        long timeElapsed;
        String message;

        public ResultResponse(){}

        public ResultResponse(String project, Result result, String message, long timeElapsed) {
            this.project = project;
            this.result = result;
            this.timeElapsed = timeElapsed;
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public ResultResponse setProject(String project) {
            this.project = project;
            return this;
        }

        public ResultResponse setResult(Result result) {
            this.result = result;
            return this;
        }

        public ResultResponse setTimeElapsed(long timeElapsed) {
            this.timeElapsed = timeElapsed;
            return this;
        }

        public String getProject() {
            return project;
        }

        public Result getResult() {
            return result;
        }

        public String getTimeElapsed() {
            double elapsedMinutes = (timeElapsed * 1e-9) / 60d;
            long minutes = Math.round(Math.floor(elapsedMinutes));
            long seconds = Math.abs(Math.round((elapsedMinutes - minutes) * 100 * 0.6d));
            return String.format("%d minutes %s seconds", minutes, seconds);
        }
    }

    public enum Result{
        SUCCESS,
        FAILED,
        CANCELLED,
        SKIPPED
    }

}