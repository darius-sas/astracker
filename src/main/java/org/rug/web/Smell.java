package org.rug.web;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.tracker.ASmellTracker;

import java.util.*;

/**
 * Represents a smell affecting multiple components in multiple versions.
 */
public class Smell extends VersionSpanningNode{

    private final static Map<String, String> typesLabels;

    static {
        typesLabels = new HashMap<>();
        typesLabels.put("cyclicDep", "Cyclic Dependency");
        typesLabels.put("unstableDep", "Unstable Dependency");
        typesLabels.put("hubLikeDep", "Hublike Dependency");
        typesLabels.put("godComponent", "God Component");
    }

    private final long id;
    private final String type;
    private final long age;
    private final String firstVersionAppeared;
    private final long firstIndexAppeared;
    private final String firstDateAppeared;
    private final String lastVersionDetected;
    private final String lastDateDetected;
    private final long lastIndexDetected;
    private final Map<Long, List<String>> affectedComponents;

    /**
     * Build this smell starting from a vertex representing a smell in the condensed graph.
     * @param smell the vertex.
     */
    public Smell(Vertex smell) {
        this.id = smell.value(ASmellTracker.UNIQUE_SMELL_ID);
        this.type = toLabel(smell.value(ASmellTracker.SMELL_TYPE));
        this.age = smell.value(ASmellTracker.AGE);
        this.firstVersionAppeared = shortenVersion(smell.value(ASmellTracker.FIRST_APPEARED));
        this.firstIndexAppeared = smell.value(ASmellTracker.FIRST_APPEARED_INDEX);
        this.lastVersionDetected = shortenVersion(smell.value(ASmellTracker.LAST_DETECTED));
        this.lastIndexDetected = smell.value(ASmellTracker.LAST_DETECTED_INDEX);
        this.lastDateDetected = smell.value(ASmellTracker.LAST_DETECTED_DATE);
        this.firstDateAppeared = smell.value(ASmellTracker.FIRST_APPEARED_DATE);
        this.affectedComponents = new TreeMap<>();
        setAffectedComponents(smell);
        setCharacteristics(smell);
    }

    /**
     * The unique smell identifier of this smell.
     * @return a long.
     */
    public long getId() {
        return id;
    }

    /**
     * The type of the smell (cycle, unstable, etc.) as a string.
     * @return a string
     */
    public String getType() {
        return type;
    }

    /**
     * The number of versions this smell was detected in.
     * @return the age of the smell.
     */
    public Long getAge() {
        return age;
    }

    /**
     * The string name of the first version this smell was detected in.
     * @return the version where this smell was first detected.
     */
    public String getFirstVersionAppeared() {
        return firstVersionAppeared;
    }

    /**
     * The string name of the last version this smell was detected in.
     * @return the version where this smell was last detected.
     */
    public String getLastVersionDetected() {
        return lastVersionDetected;
    }

    /**
     * The version index where this smell first appeared;
     */
    public long getFirstIndexAppeared() {
        return firstIndexAppeared;
    }

    /**
     * The version index where this smell was last detected;
     */
    public long getLastIndexDetected() {
        return lastIndexDetected;
    }

    /**
     * The date the smell first appeared.
     * @return a string representing a date in the following format %dd-%mm-%yyyy
     */
    public String getFirstDateAppeared() {
        return firstDateAppeared;
    }

    /**
     * The date the smell first detected.
     * @return a string representing a date in the following format %dd-%mm-%yyyy
     */
    public String getLastDateDetected() {
        return lastDateDetected;
    }

    /**
     * A map where the keys are the version indexes this smell has affected and the values
     * are a list of components name affected in that version.
     * @return a map as described above.
     */
    public Map<Long, List<String>> getAffectedComponents() {
        return affectedComponents;
    }

    private String toLabel(String smellType){
        return typesLabels.getOrDefault(smellType, smellType);
    }

    private String shortenVersion(String version){
        var lastDash = version.lastIndexOf("-");
        var undashed = version;
        if (lastDash >= 0 && lastDash + 1 < version.length()){
            undashed = version.substring(lastDash + 1);
        }
        return undashed.length() < 8 ? undashed : undashed.substring(0, 8);
    }

    private void setCharacteristics(Vertex smell){
        var graph = smell.graph();
        var hasCharactEdges = graph.traversal().V(smell).outE(ASmellTracker.HAS_CHARACTERISTIC).toSet();
        for (Edge edge : hasCharactEdges) {
            long affectedVersion = edge.value(ASmellTracker.VERSION_INDEX);
            spanningVersions.add(affectedVersion);
            characteristics.put(affectedVersion, propertiesToMap(edge.inVertex()));
        }
    }

    private void setAffectedComponents(Vertex smell){
        var graph = smell.graph();
        var hasCharactEdges = graph.traversal().V(smell).outE(ASmellTracker.AFFECTS).toSet();
        for (Edge edge : hasCharactEdges) {
            long affectedVersion = edge.value(ASmellTracker.VERSION_INDEX);
            spanningVersions.add(affectedVersion);
            affectedComponents.compute(affectedVersion, (k, v) -> {
                if (v == null){
                    v = new ArrayList<>();
                }
                v.add(edge.inVertex().value(ASmellTracker.NAME));
                return v;
            });
        }
    }

}
