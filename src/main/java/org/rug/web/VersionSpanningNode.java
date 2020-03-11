package org.rug.web;

import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;

/**
 * Represents a node that is present in one or multiple versions of the system.
 */
public abstract class VersionSpanningNode {

    protected final TreeSet<Long> spanningVersions;
    protected final Map<Long, Map<String, String>> characteristics;

    public VersionSpanningNode() {
        this.spanningVersions = new TreeSet<>();
        this.characteristics  = new TreeMap<>();
    }

    /**
     * The last version this element affects.
     * @return a version index.
     */
    public long getLastVersion(){
        return spanningVersions.last();
    }

    /**
     * The first version this element affects.
     * @return a version index.
     */
    public long getFirstVersion(){
        return spanningVersions.first();
    }

    /**
     * The versions where this element is present.
     * @return a list of version indexes.
     */
    public TreeSet<Long> getSpanningVersions() {
        return spanningVersions;
    }

    /**
     * A map of maps. The keys of the main map are the version indexes whereas each map has the names of the characteristics as keys and their respective values as values.
     * For example, an entry could have the following structure when written as a JSON object:
     * {"23": {"characteristicName1": "0.231", "characteristicName2": "420"}, "24": ...}
     * @return a map of maps.
     */
    public Map<Long, Map<String, String>> getCharacteristics() {
        characteristics.values().forEach(map ->
            characteristicsLabels.forEach((k, v) -> {
                var oldValue = map.remove(k);
                if (!filteredCharacteristics.contains(k)) {
                    map.put(v, oldValue);
                }
            }));
        return characteristics;
    }

    /**
     * Utility method that copies all properties in a separated map.
     * @param vertex the vertex to extract the properties from
     * @return a map with the properties of vertex.
     */
    protected Map<String, String> propertiesToMap(Vertex vertex){
        var map = new HashMap<String, String>();
        vertex.keys().forEach(k -> map.put(k, vertex.value(k).toString()));
        return map;
    }


    private final static Map<String, String> characteristicsLabels;
    private final static List<String> filteredCharacteristics;

    static {
        characteristicsLabels = new HashMap<>();
        characteristicsLabels.put("pageRankAvrg", "Average PageRank");
        characteristicsLabels.put("shape", "Shape");
        characteristicsLabels.put("avrgEdgeWeight", "Average Edge Weight");
        characteristicsLabels.put("overlapRatio", "Overlap ratio");
        characteristicsLabels.put("numOfPrivateUseEdges", "Number of Private Edges");
        characteristicsLabels.put("pageRankMax", "Max. PageRank");
        characteristicsLabels.put("numOfInheritanceEdges", "Number of Inheritance Edges");
        characteristicsLabels.put("numOfEdges", "Number of Edges");
        characteristicsLabels.put("affectedDesignLevel", "Affected Level");
        characteristicsLabels.put("size", "Number of components");
        characteristicsLabels.put("affectedComponentType", "Affected Component Type");
        characteristicsLabels.put("avrgNumOfChanges", "Average # of Changes");
        characteristicsLabels.put("numOfPublicUseEdges", "Number of public edges");
        characteristicsLabels.put("avrgInternalPathLength", "Average Internal Path Length");
        characteristicsLabels.put("strength", "Strength");
        characteristicsLabels.put("instabilityGap", "Instability Gap");

        filteredCharacteristics = new ArrayList<>();
        filteredCharacteristics.add("numOfPrivateUseEdges");
        filteredCharacteristics.add("numOfPublicUseEdges");
        filteredCharacteristics.add("overlapRatiocyclicDep");
        filteredCharacteristics.add("overlapRatiounstableDep");
        filteredCharacteristics.add("overlapRatiohublikeDep");

    }

    private String toLabel(String name){
        return characteristicsLabels.getOrDefault(name, name);
    }
}
