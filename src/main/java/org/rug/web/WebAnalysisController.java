package org.rug.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
     * @param requestParameters
     * @param response
     * @return String
     * @throws IOException
     */
    @RequestMapping(value = {"/analyse"}, method = RequestMethod.GET,  produces={ "application/json"})
    public ResultResponse analyse(
            @RequestParam Map<String,String> requestParameters,
            HttpServletResponse response) {

        var runner = new ASTrackerWebRunner(requestParameters);
        ResultResponse result = new ResultResponse();
        try {
            long start = java.lang.System.nanoTime();
            runner.run();
            long end = java.lang.System.nanoTime();
            response.setStatus(HttpServletResponse.SC_OK);
            result.setResult(Result.SUCCESS);
            result.setProject(runner.getProjectName());
            result.setTimeElapsed(end - start);
            result.setMessage("No message.");
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            result.setResult(Result.FAILED);
            result.setMessage(e.getMessage());
            result.setTimeElapsed(0);
        }
        return result;
    }

    /**
     * Can be accessed to see what CLI arguments have been mapped by the request arguments.
     *
     * @param requestParameters
     * @param response
     * @return String
     * @throws IOException
     */
    @RequestMapping(value = {"/getCLIargs"}, method = RequestMethod.GET,  produces={ "application/json"})
    public String getCLIargs(
            @RequestParam Map<String,String> requestParameters,
            HttpServletResponse response){
        var runner = new ASTrackerWebRunner(requestParameters);
        return runner.getCLIArgs();
    }

    public final class ResultResponse{
        String project;
        Result result;
        long timeElapsed;
        String message;

        public ResultResponse(){}

        public ResultResponse(String project, Result result, String message, long timeElapsed) {
            this.project = project;
            this.result = result;
            this.timeElapsed = timeElapsed;
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
        CANCELLED
    }

}