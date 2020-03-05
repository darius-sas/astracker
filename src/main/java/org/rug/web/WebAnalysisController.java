package org.rug.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
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
     * @param requestParameters
     * @param response
     * @return String
     * @throws IOException
     */
    @RequestMapping(value = {"/analyse"}, method = RequestMethod.GET,  produces={ "application/json"})
    public String analyse(
            @RequestParam Map<String,String> requestParameters,
            HttpServletResponse response) {
        var runner = new ASTrackerWebRunner(requestParameters);
        String result = null;
        try {
            result = runner.run();
            response.setStatus(HttpServletResponse.SC_OK);
            if (result == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            logger.error("Internal server error: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
}