package org.rug.api;

import org.rug.api.helpers.ArgumentMapper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Class used as the controller for the API. Will handle the mapping to specific URLs.
 */
@RestController
public class Controller extends HttpServlet {

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
    public String runASTracker(
            @RequestParam Map<String,String> requestParameters,
            HttpServletResponse response
    ) throws IOException {
        var runner = new ASTrackerRunner(new ArgumentMapper(requestParameters));
        var result = runner.run();
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        response.setStatus(HttpServletResponse.SC_OK);
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
    public String runASTrackerString(
            @RequestParam Map<String,String> requestParameters,
            HttpServletResponse response
    ) throws IOException {
        var runner = new ASTrackerRunner(new ArgumentMapper(requestParameters));
        var result = runner.getCLIArgs();
        return result;
    }
}