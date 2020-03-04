package org.rug.api;

import org.rug.api.helpers.ArgumentMapper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Class used as the main controller for the web interface. Will handle the mapping to specific URLs.
 */
@RestController
public class Controller extends HttpServlet {

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

    @RequestMapping(value = {"/getCLIargs"}, method = RequestMethod.GET,  produces={ "application/json"})
    public String runASTrackerString(
            @RequestParam Map<String,String> requestParameters,
            HttpServletResponse response
    ) throws IOException {
        var runner = new ASTrackerRunner(new ArgumentMapper(requestParameters));
        var result = runner.getCLIArgs();
        return result;
    }

    @RequestMapping(value = {"/help"}, method = RequestMethod.GET, produces={ "application/json"})
    public String getHelp(@RequestParam Map<String,String> requestParameters) {
        var runner = new ASTrackerRunner(new ArgumentMapper(requestParameters));
        return runner.getHelp();
    }

    @RequestMapping("/helpMe")
    public String helpMe(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
    {
        return("req.getQueryString() output : ");
    }
}