package org.rug.web;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.fasterxml.jackson.databind.util.JSONWrappedObject;
import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.text.html.Option;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Defines the controller that manages the requests to retrieve data about a system.
 */
@RestController
@Scope("session")
public class SystemController {

    private final Logger logger = LoggerFactory.getLogger(SystemController.class);

    private static final int MAX_CACHED_SYSTEMS = 5;
    private final Map<String, System> cachedSystems = new LinkedHashMap<>();

    @RequestMapping(value = "/projects", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONWrappedObject projects(){
        Path projectsDir = Paths.get("./output-folder/trackASOutput/");
        try {

            var list = Files.list(projectsDir)
                    .filter(Objects::nonNull)
                    .map(Path::toFile)
                    .filter(File::isDirectory)
                    .map(File::getName).collect(Collectors.toList());

            return wrapToJson("projects", list);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return wrapToJson("projects", List.of());
    }

    /**
     * Return a map of versions for this system. The keys represent the index of the version and the values
     * the actual "name" of the version.
     * @param system the name of the system analysed.
     * @return a map with the versions of this system or an empty map if the given system does not exist.
     */
    @RequestMapping(value = "/versions", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public Map<Long, String> versions(@RequestParam(value="system") String system) {
        var sysOpt = getSystem(system);
        if (sysOpt.isPresent()){
            return sysOpt.get().getVersions();
        }
        return Collections.emptyMap();
    }

    /**
     * Returns a list of components (classes, packages, headers, etc.) of this system.
     * A range of starting and ending version index can be provided to limit the results only to components
     * that were present in the version indexes included in the given range.
     * @param system the name of the system of interest.
     * @param fromVersionIndex the starting version index that defines the range of this query. If negative, or not provided,
     *                         the system will limit the results to few versions back in time.
     * @param toVersionIndex the ending version index that defines the range of this query. If not provided, the latest version index is used.
     * @return a list of components or an empty list of the given system does not exist.
     */
    @RequestMapping(value = "/components", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONWrappedObject components(@RequestParam(value="system") String system,
                                      @RequestParam(value="lastVersion", defaultValue = "true") boolean lastVersion,
                                      @RequestParam(value="start", defaultValue = "-1", required = false) long fromVersionIndex,
                                      @RequestParam(value="end", defaultValue = "4294967296", required = false) long toVersionIndex){
        var sysOpt = getSystem(system);
        if (sysOpt.isPresent()) {
            System sys = sysOpt.get();
            if (lastVersion) {
                fromVersionIndex = sys.getVersions().lastKey();
                toVersionIndex = sys.getVersions().lastKey();
            } else if (fromVersionIndex < 0) {
                fromVersionIndex = sys.getRecentStartingIndex();
            }

            logger.debug("Using fromVersionIndex={}", fromVersionIndex);
            return wrapToJson("components", sys.getComponents(fromVersionIndex, toVersionIndex));
        }
        return wrapToJson("components", Collections.emptyList());
    }

    /**
     * Returns a list of smells detected in the given system.
     * A range of starting and ending version index can be provided to limit the results only to smells
     * that were detected in the version indexes included in the given range.
     * @param system the name of the system of interest.
     * @param fromVersionIndex the starting version index that defines the range of this query. If negative, or not provided,
     *                         the system will limit the results to few versions back in time.
     * @param toVersionIndex the ending version index that defines the range of this query. If not provided, the latest version index is used.
     * @return a list of smells.
     */
    @RequestMapping(value = "/smells", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONWrappedObject smells(@RequestParam(value="system") String system,
                              @RequestParam(value="lastVersion", defaultValue = "true") boolean lastVersion,
                              @RequestParam(value="start", defaultValue = "-1", required = false) long fromVersionIndex,
                              @RequestParam(value="end", defaultValue = "4294967296", required = false) long toVersionIndex){
        var sysOpt = getSystem(system);
        if (sysOpt.isPresent()) {
            System sys = sysOpt.get();
            if (lastVersion) {
                fromVersionIndex = sys.getVersions().lastKey();
                toVersionIndex = sys.getVersions().lastKey();
            } else if (fromVersionIndex < 0) {
                fromVersionIndex = sys.getRecentStartingIndex();
            }
            logger.debug("Using fromVersionIndex={}", fromVersionIndex);
            return wrapToJson("smells", sys.getSmells(fromVersionIndex, toVersionIndex));
        }
        return wrapToJson("smells", Collections.emptyList());
    }

    /**
     * Returns the complete System object with all the smells and components belonging to the requested system.
     * @param system the system name.
     * @return a System object containing all versions of all smells and components. An empty system is returned
     * if no system with such a name exists.
     */
    @RequestMapping(value = "/system", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public JSONWrappedObject system(@RequestParam(value="system") String system){
        var sysOpt = getSystem(system);
        return sysOpt.map(value -> wrapToJson("system", value))
                .orElseGet(() -> wrapToJson("system", System.empty()));
    }


    /**
     * Get the system object with the given name. In case the system has not been cached already, load it before returning it to the caller.
     * @param systemName the name of the system to return.
     * @return a System object containing the data of the system.
     */
    private Optional<System> getSystem(String systemName){
        if (cachedSystems.size() > MAX_CACHED_SYSTEMS){
            cachedSystems.remove(cachedSystems.keySet().iterator().next());
        }
        if (!cachedSystems.containsKey(systemName)){
            var graphFile = Paths.get(String.format("./output-folder/trackASOutput/%s/condensed-graph-consecOnly.graphml", systemName));

            if (!Files.exists(graphFile)){
                return Optional.empty();
            }

            var graph = TinkerGraph.open();
            graph.traversal().io(graphFile.toAbsolutePath().toString()).read().with(IO.reader, IO.graphml).iterate();
            cachedSystems.put(systemName, new System(systemName, graph));
            logger.debug("Successfully loaded {}", systemName);
        }
        return Optional.of(cachedSystems.get(systemName));
    }


    private JSONWrappedObject wrapToJson(String name, Object o){
        return new JSONWrappedObject(String.format("{\"%s\":", name), "}", o);
    }
}
