package org.rug.web;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.rug.tracker.ASmellTracker;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents the data within a condensed graph.
 */
public class System {

    private final TreeMap<Long, String> versions   = new TreeMap<>();
    private final List<Smell>       smells     = new ArrayList<>(100);
    private final List<Component>   components = new ArrayList<>(100);

    private final Graph graph;

    /**
     * Build the system starting from the given condensed graph.
     * @param graph a condensed graph.
     */
    public System(Graph graph){
        this.graph = graph;
        this.graph.traversal().E()
                .has(ASmellTracker.VERSION)
                .has(ASmellTracker.VERSION_INDEX)
                .forEachRemaining(e -> versions.put(e.value(ASmellTracker.VERSION_INDEX), e.value(ASmellTracker.VERSION)));
    }

    /**
     * All the smells in this system throughout all the versions.
     * @return a list of smells.
     */
    public List<Smell> getSmells() {
        if (smells.isEmpty()){
            graph.traversal().V().hasLabel(ASmellTracker.SMELL).forEachRemaining(v -> smells.add(new Smell(v)));
        }
        return smells;
    }

    /**
     * All the smells in this system throughout all the versions.
     *
     * @return a TreeMap wth a list of smells.
     */
    public TreeMap<String, List<Smell>> getSystemSmells() {
        var map = new TreeMap<String, List<Smell>>();
        map.put("smells", getSmells());
        return map;
    }

    /**
     * All the smells in this system throughout the mentioned versions.
     *
     * @return a TreeMap wth a list of smells.
     */
    public TreeMap<String, List<Smell>> getSystemSmells(long startIndex, long endIndex) {
        var map = new TreeMap<String, List<Smell>>();
        map.put("smells", getSmells(startIndex, endIndex));
        return map;
    }

    /**
     * Retrieve the smells that affect at least one version that is contained in the interval [startIndex, endIndex]
     * where startIndex <= endIndex.
     * @param startIndex the version index where the interval starts
     * @param endIndex the version index where the interval ends
     * @return a list of smells affecting versions in the given interval or an empty list if no smells
     * that respect such condition exist.
     */
    public List<Smell> getSmells(long startIndex, long endIndex){
        return filterByInterval(getSmells(), startIndex, endIndex);
    }

    /**
     * Return all the all-time components in this system.
     * @return a list of components.
     */
    public List<Component> getComponents() {
        if (components.isEmpty()){
            graph.traversal().V().hasLabel(ASmellTracker.COMPONENT).forEachRemaining(v -> components.add(new Component(v)));
        }
        return components;
    }

    /**
     * Retrieve the components that exist in at least one version that is contained in the interval [startIndex, endIndex]
     * where startIndex <= endIndex.
     * @param startIndex the version index where the interval starts
     * @param endIndex the version index where the interval ends
     * @return a list of components existing in the versions contained in the given interval or an empty list if no such
     * components exist.
     */
    public List<Component> getComponents(long startIndex, long endIndex){
        return filterByInterval(getComponents(), startIndex, endIndex);
    }

    /**
     * The versions of this system as a map where the keys are the indexes and the values are the string representation.
     * @return a map of versions indexes and names of versions.
     */
    public TreeMap<Long, String> getVersions(){
        return versions;
    }

    /**
     * Returns a reasonably recent starting index for this system.
     * This method can be used to avoid returning too many irrelevant smells/components with {@link #getComponents(long, long)}
     * or {@link #getSmells(long, long)}.
     * @return a version index.
     */
    public long getRecentStartingIndex(){
        if (versions.size() == 0) {
            return 0;
        }
        var recentKey = versions.lastKey();
        recentKey = versions.ceilingKey((long)(recentKey * 0.85));
        return Math.max(recentKey, versions.ceilingKey(versions.lastKey() - 25));
    }

    /**
     * Filter a list of VersionSpanningNode based whether they fit in the given interval or not.
     * @param list the list to filter
     * @param startIndex the starting index
     * @param endIndex the end index
     * @param <T> a type extending VersionSpanningNode
     * @return an empty list of no such elements are contained in list or such elements if they are present in the list.
     */
    private <T extends VersionSpanningNode> List<T> filterByInterval(List<T> list, long startIndex, long endIndex){
        if (startIndex > endIndex){
            return Collections.emptyList();
        }
        return list.stream().filter(s -> (endIndex - s.getFirstVersion()) * (s.getLastVersion() - startIndex) >= 0).collect(Collectors.toList());
    }

}
