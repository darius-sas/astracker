package org.rug.web.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;

/**
 * Helper class that can be used to map the request parameters to CLI arguments.
 * Contains all mapping logic.
 */
public class ArgumentMapper {

    private Map<String, String> requestParameters;
    private ArrayList<String> array;
    private RemoteProjectFetcher fetcher;
    private String projectName;

    private Path arcanJavaJarPath;
    private Path arcanCJarPath;
    private Path outputFolderPath;

    public ArgumentMapper(Path arcanJavaJarPath,
                          Path arcanCJarPath,
                          Path outputFolderPath,
                          Path clonedRepoDirectory,
                          Map<String,String> requestParameters) {
        this.arcanJavaJarPath = arcanJavaJarPath;
        this.arcanCJarPath = arcanCJarPath;
        this.outputFolderPath = outputFolderPath;
        this.fetcher = new RemoteProjectFetcher(clonedRepoDirectory);
        this.requestParameters = requestParameters;
        array = new ArrayList<>();
    }

    /**
     * Translates between the ArrayList of strings to an array of strings
     * needed by the JcCommander interface.
     *
     * @return String[]
     */
    public String[] getArgumentsMapping() throws IOException, GitAPIException {
        if (!this.requestParameters.containsKey("project") ||
            !this.requestParameters.containsKey("language")) {
            throw new IllegalArgumentException("project and language are required fields.");
        }

        return mapParameters().toArray(String[]::new);
    }

    /**
     * Does the actual mapping from a Map of parameters to the arguments needed in the CLI
     *
     * @return ArrayList<String>
     */
    private ArrayList<String> mapParameters() throws IOException, GitAPIException {
        // To store  the args as an ArrayList first
        array.clear();

        array.add("-o");
        array.add(outputFolderPath.toString());

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
                    var linkOrName = requestParameters.get("project");
                    projectName = fetcher.getProjectName(linkOrName);
                    var inputDir = Paths.get(outputFolderPath.toString(), "arcanOutput", projectName);
                    inputDir.toFile().mkdir();
                    array.add("-i");
                    array.add(inputDir.toString());
                    array.add("-projectName");
                    array.add(projectName);
                    array.add("-runArcan");
                    array.add(requestParameters.get("language")
                            .equalsIgnoreCase("java")
                            ? arcanJavaJarPath.toString()
                            : arcanCJarPath.toString());
                    var projectInputDir = fetcher.getProjectPath(linkOrName);
                    array.add("-gitRepo");
                    array.add(projectInputDir.toString());
                    break;

                // Used to analyse only a single version with Arcan
                case "singleVersion":
                    array.add("-singleVersion");
                    break;

                case "runTracker":
                    if (requestParameters.get("runTracker").equals("false")) {
                        array.add("-doNotRunTracker");
                    }
                    break;

                case "branch":
                    array.add("-branch");
                    array.add(requestParameters.get(key));
                    break;

                case "startDate":
                    array.add("-startDate");
                    array.add(requestParameters.get(key));
                    break;

            }
        }
        return array;
    }

    public String getProjectName(){
        return projectName;
    }

    /**
     * Can be used to return a Json representation of all the arguments used in the analysis.
     * @return String
     */
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
