package org.rug.api.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class that can be used to map the request parameters to CLI arguments.
 * Contains all mapping logic.
 */
public class ArgumentMapper {

    private Map<String, String> requestParameters;
    private ArrayList<String> array;

    public ArgumentMapper(Map<String,String> requestParameters) {
        this.requestParameters = requestParameters;
    }
    /**
     * Translates between the ArrayList of strings to an array of strings
     * needed by the JcCommander interface.
     *
     * @return String[]
     */
    public String[] getArgumentsMapping() {
        if (!this.requestParameters.containsKey("project") ||
            !this.requestParameters.containsKey("language")
        ) {
            return new String[]{"project and language are required fields."};
        }

        var array = this.mapParameters();
        var args = new String[array.size()];
        for (int i=0; i < args.length; i++){
            args[i] = array.get(i);
        }

        return args;
    }

    /**
     * Does the actual mapping from a Map of parameters to the arguments needed in the CLI
     *
     * @return ArrayList<String>
     */
    private ArrayList<String> mapParameters() {
        // To store  the args as an ArrayList first
        array = new ArrayList<String>();
        array.add("-i");
        array.add("C:\\Users\\anaro\\OneDrive\\Desktop\\ResearchInternship\\astracker\\test-data\\output\\arcanOutput\\antlr");
        //input would be a git repo
        array.add("-o");
        array.add("C:\\Users\\anaro\\OneDrive\\Desktop\\ResearchInternship\\astracker\\output-folder");

        for (String key : requestParameters.keySet()) {
            switch (key) {
                case "help":
                case "h":
                    array.add("--help");
                    break;

                case "printSimilarity":
                case "similarity":
                    array.add("-pSimilarity");
                    break;

                case "pCharacteristics":
                case "printCharacteristics":
                case "characteristics":
                case "chars":
                case "printChars":
                    array.add("-pCharacteristics");
                    break;

                case "runProjectSize":
                    array.add("-runProjectSize");
                    break;

                case "nonConsecutive":
                case "nonConsec":
                    array.add("-enableNonConsec");
                    break;

                case "componentCharacteristics":
                    array.add("-pCompoCharact");
                    break;

                case "language":
                    switch (requestParameters.get("language")) {
                        case "Java":
                        case "java":
                            array.add("-javaProject");
                            break;
                        case "jar":
                        case "Jar":
                            array.add("-jarProject");
                            break;
                        case "c":
                            array.add("-cProject");
                            break;
                        case "cpp":
                        case "c++":
                            array.add("-cppProject");
                            break;
                    }
                    break;

                // Link to GitHub
                case "project":
                    var git_link = requestParameters.get("project");
                    var name = this.extractProjectName(git_link);
                    if (name == null) {
                        System.out.println("The name could not be extracted");
                        break;
                    }

                    array.add("-gitLink");
                    array.add(git_link);
                    array.add("-projectName");
                    array.add(name);
                    array.add("-runArcan");
                    array.add("./Arcan-1.4.0-SNAPSHOT/Arcan-1.4.0-SNAPSHOT.jar");
                    break;

                // Link to local repository
                case "gitRepo":
                    array.add("-gitRepo");
                    array.add(requestParameters.get("gitRepo"));
                    break;

                case "runTracker": //can be either true or false - added only when false
                    if (requestParameters.get("runTracker").equals("false")) {
                        array.add("-doNotRunTracker");
                    }
                    break;
            }
        }
        return array;
    }

    private String extractProjectName(String git_link) {
        System.out.println("Project link: " + git_link);
        Pattern pattern = Pattern.compile(
                "https:\\/\\/github\\.com\\/.*\\/(.*)\\.git"
        );
        Matcher matcher = pattern.matcher(git_link);
        if(matcher.find()) {
            var name = matcher.group(1);
            System.out.println("Project name: " + name);
            return name;
        }
        return null;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        String json = "";
        try {
            json = mapper.writeValueAsString(this.array);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return json;
    }
}
