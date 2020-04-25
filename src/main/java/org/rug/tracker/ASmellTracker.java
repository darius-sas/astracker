package org.rug.tracker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.data.project.IVersion;
import org.rug.data.smells.ArchitecturalSmell;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.*;

/**
 * Tracks incrementally the architectural smells and saves them internally.
 */
public class ASmellTracker implements Serializable{

    public static final String NAME = "name";
    public static final String SMELL = "smell";
    public static final String VERSION = "version";
    public static final String VERSION_INDEX = "versionIndex";
    public static final String VERSION_DATE = "versionDate";
    public static final String SMELL_OBJECT = "smellObject";
    public static final String LATEST_VERSION = "latestVersion";
    public static final String EVOLVED_FROM = "evolvedFrom";
    public static final String UNIQUE_SMELL_ID = "uniqueSmellID";
    public static final String REAPPEARED = "reappeared";
    public static final String SIMILARITY = "similarity";
    public static final String CHARACTERISTIC = "characteristic";
    public static final String HAS_CHARACTERISTIC = "hasCharacteristic";
    public static final String COMPONENT = "component";
    public static final String AFFECTS = "affects";
    public static final String SMELL_TYPE = "smellType";
    public static final String AGE = "age";
    public static final String NA = "NA";
    public static final String FIRST_APPEARED = "firstAppeared";
    public static final String LAST_DETECTED = "lastDetected";
    public static final String FIRST_APPEARED_INDEX = "firstAppearedIndex";
    public static final String FIRST_APPEARED_DATE = "firstAppearedDate";
    public static final String LAST_DETECTED_INDEX  = "lastDetectedIndex";
    public static final String LAST_DETECTED_DATE = "lastDetectedDate";
    public static final String SMELL_ID = "smellId";
    public static final String COMPONENT_TYPE = "componentType";
    public static final String TAIL = "tail";
    public static final String COMPONENT_CHARACTERISTIC = "componentCharacteristic";
    public static final String LATEST_VERSION_INDEX = "latestVersionIndex";

    private transient Graph trackGraph;
    private transient Graph condensedGraph;
    private final transient Map<Long, Vertex> uniqueSmellsMap = new HashMap<>(5000);
    private final transient Map<String, CachedVertex> updatedAffectedElementsCache = new HashMap<>(5000);
    private transient Vertex tail;
    private long uniqueSmellID;
    private final ISimilarityLinker scorer;
    private final DecimalFormat decimal;

    private final boolean trackNonConsecutiveVersions;

    private final static Logger logger = LogManager.getLogger(ASmellTracker.class);

    /**
     * Builds an instance of this tracker.
     * @param trackNonConsecutiveVersions whether to track a smell through non-consecutive versions.
     *                                    This adds the possibility to track reappearing smells.
     */
    public ASmellTracker(ISimilarityLinker scorer, boolean trackNonConsecutiveVersions){
        this.trackGraph = TinkerGraph.open();
        this.tail = trackGraph.traversal().addV(TAIL).next();
        this.condensedGraph = TinkerGraph.open();
        this.uniqueSmellID = 1L;
        this.trackNonConsecutiveVersions = trackNonConsecutiveVersions;
        this.scorer = scorer;
        this.decimal = new DecimalFormat("0.0#");
    }


    /**
     * Builds an instance of this tracker that does not tracks smells through non-consecutive versions.
     * A JaccardSimilarityLinker is used to select the single successor of the given smell.
     */
    public ASmellTracker(){
        this(new JaccardSimilarityLinker(), false);
    }

    /**
     * Computes the tracking algorithm on the given system and saves internally the results
     * @param smellsInVersion the architectural smells identified in version
     * @param version the version of the given system
     */
    public void track(List<ArchitecturalSmell> smellsInVersion, IVersion version){
        List<ArchitecturalSmell> nextVersionSmells = new ArrayList<>(smellsInVersion);

        GraphTraversalSource g1 = trackGraph.traversal();

        if (g1.V(tail).outE().hasNext()) {
            List<ArchitecturalSmell> currentVersionSmells;
            if (trackNonConsecutiveVersions) {
                currentVersionSmells = g1.V(tail).out().values(SMELL_OBJECT)
                        .toStream().map(o -> (ArchitecturalSmell) o).collect(Collectors.toList());
            }else {
                currentVersionSmells = g1.V(tail).out().has(VERSION, tail.value(LATEST_VERSION).toString()).values(SMELL_OBJECT)
                        .toStream().map(o -> (ArchitecturalSmell) o).collect(Collectors.toList());
            }

            Set<LinkScoreTriple> bestMatch = scorer.bestMatch(currentVersionSmells, nextVersionSmells);
            logger.debug("Matching complete for {} pairs.", bestMatch.size());
            bestMatch.forEach(t -> {
                // If this fails it means that a successor has already been found, which should never happen!
                Vertex predecessor = g1.V(tail).out().has(SMELL_OBJECT, t.getA()).next();
                Vertex successor = g1.addV(SMELL)
                        .property(VERSION, version.getVersionString())
                        .property(VERSION_INDEX, version.getVersionIndex())
                        .property(VERSION_DATE, version.getVersionDate())
                        .property(SMELL_ID, t.getB().getId())
                        .property(SMELL_OBJECT, t.getB())
                        .property(UNIQUE_SMELL_ID, predecessor.value(UNIQUE_SMELL_ID))
                        .next();
                g1.V(tail).outE().where(otherV().is(predecessor)).drop().iterate();
                String eLabel = tail.value(LATEST_VERSION).equals(predecessor.value(VERSION)) ? EVOLVED_FROM : REAPPEARED;
                g1.addE(eLabel).property(SIMILARITY, decimal.format(t.getC())).from(successor).to(predecessor).next();
                g1.addE(LATEST_VERSION).from(tail).to(successor).next();
                currentVersionSmells.remove(t.getA());
                nextVersionSmells.remove(t.getB());

                g1.V(predecessor).drop().iterate();
            });
            if (!trackNonConsecutiveVersions) {
                currentVersionSmells.forEach(this::endDynasty);
            }

        }
        nextVersionSmells.forEach(s -> addNewDynasty(s, version));
        tail.property(LATEST_VERSION, version.getVersionString());
        tail.property(LATEST_VERSION_INDEX, version.getVersionIndex());
        logger.debug("Updating condensed graph.");
        updateCondensedGraph();
    }

    /**
     * Begins a new dynasty for the given AS at the given starting version
     * @param s the starter of the dynasty
     * @param version the version
     */
    private void addNewDynasty(ArchitecturalSmell s, IVersion version) {
        GraphTraversalSource g = trackGraph.traversal();
        Vertex successor = g.addV(SMELL)
                .property(VERSION, version.getVersionString())
                .property(VERSION_INDEX, version.getVersionIndex())
                .property(VERSION_DATE, version.getVersionDate())
                .property(SMELL_ID, s.getId())
                .property(SMELL_OBJECT, s)
                .property(UNIQUE_SMELL_ID, uniqueSmellID++)
                .next();
        g.addE(LATEST_VERSION).from(tail).to(successor).next();
    }

    /**
     * Concludes the dynasty of the given smell (last smell in the dynasty)
     * @param smell the smell
     */
    private void endDynasty(ArchitecturalSmell smell){
        GraphTraversalSource g = trackGraph.traversal();
        Vertex lastHeir = g.V().has(SMELL_OBJECT, smell).next();
        g.V(tail).outE().where(otherV().is(lastHeir)).drop().iterate();
        g.V(lastHeir).drop().iterate();
    }

    /**
     * Get the scorer used to instantiate this instance.
     * @return The scorer used to link the smells between versions.
     */
    public ISimilarityLinker getScorer() {
        return scorer;
    }

    /**
     * Returns the latest version of update of this tracker.
     * @return a string representing the version or {@link #NA} if no current version is available.
     */
    public String currentVersion(){
        return tail.property(LATEST_VERSION).orElse(NA).toString();
    }

    /**
     * Updates the condensed graph from the current state of trackGraph.
     */
    private void updateCondensedGraph(){
        GraphTraversalSource gt = trackGraph.traversal();
        GraphTraversalSource gs = condensedGraph.traversal();

        Set<Vertex> smellsInVersion = gt.V().hasLabel(TAIL).out().toSet();

        logger.debug("Updating {} smells and affected components into the condensed graph.", smellsInVersion.size());

        for(var smellVertex : smellsInVersion){
            Long smellUID = smellVertex.value(UNIQUE_SMELL_ID);
            var smellVersionString = smellVertex.value(VERSION);
            var smellVersionIndex = smellVertex.value(VERSION_INDEX);
            var smellVersionDate = smellVertex.value(VERSION_DATE);
            ArchitecturalSmell smellObject = smellVertex.value(SMELL_OBJECT);
            String affectedComponentType = smellObject.getLevel().toString();

            var condensedSmell = uniqueSmellsMap.computeIfAbsent(smellUID, (sUIDKey) -> gs.addV(SMELL)
                            .property(UNIQUE_SMELL_ID, smellUID)
                            .property(FIRST_APPEARED, smellVersionString)
                            .property(FIRST_APPEARED_INDEX, smellVersionIndex)
                            .property(FIRST_APPEARED_DATE, smellVersionDate).next());

            if (!condensedSmell.property(SMELL_TYPE).isPresent()){
                condensedSmell.property(SMELL_TYPE, smellObject.getType().toString());
            }
            Vertex characteristics = gs.addV(CHARACTERISTIC).next();
            smellObject.getCharacteristicsMap().forEach(characteristics::property);

            gs.addE(HAS_CHARACTERISTIC).from(condensedSmell).to(characteristics)
                    .property(VERSION, smellVersionString)
                    .property(VERSION_INDEX, smellVersionIndex)
                    .property(VERSION_DATE, smellVersionDate)
                    .property(SMELL_ID, smellObject.getId()).next();
            
            for(var affectedComp : smellObject.getAffectedElements()) {
                String name = affectedComp.value(NAME);
                var component = updatedAffectedElementsCache.computeIfAbsent(name, (key) -> new CachedVertex(gs.addV(COMPONENT)
                        .property(NAME, name)
                        .property(COMPONENT_TYPE, affectedComponentType).next()));
                if (!component.updated){
                    var cce = gs.V(component.vertex).outE(HAS_CHARACTERISTIC)
                            .has(VERSION, smellVersionString.toString())
                            .tryNext();
                    if (cce.isEmpty()) {
                        var componentCharacteristic = gs.addV(COMPONENT_CHARACTERISTIC).next();
                        affectedComp.keys().stream().filter(k -> !k.equals(NAME)).forEach(k ->
                                componentCharacteristic.property(k, affectedComp.value(k))
                        );
                        gs.addE(HAS_CHARACTERISTIC).from(component.vertex).to(componentCharacteristic)
                                .property(VERSION, smellVersionString)
                                .property(VERSION_INDEX, smellVersionIndex)
                                .property(VERSION_DATE, smellVersionDate)
                                .next();
                    }
                    component.updated = true;
                }
                gs.addE(AFFECTS).from(condensedSmell).to(component.vertex)
                        .property(VERSION, smellVersionString)
                        .property(VERSION_INDEX, smellVersionIndex)
                        .property(VERSION_DATE, smellVersionDate)
                        .next();
            }

            long age = condensedSmell.<Long>property(AGE).orElse(0L);
            condensedSmell.property(AGE, ++age);
            condensedSmell.property(LAST_DETECTED, smellVersionString);
            condensedSmell.property(LAST_DETECTED_INDEX, smellVersionIndex);
            condensedSmell.property(LAST_DETECTED_DATE, smellVersionDate);
        }
        updatedAffectedElementsCache.clear(); // reset cache for next version
    }

    /**
     * Retrieves the condensed graph.
     * @return the graph representing the tracked smells including their characteristics and components affected
     * with their own characteristics.
     */
    public Graph getCondensedGraph(){
        return condensedGraph;
    }

    /**
     * Get the graph object used to perform the tracking.
     * @return the track graph.
     */
    public Graph getTrackGraph(){
        return trackGraph;
    }

    /**
     * Returns the number of smells linked in the current iteration.
     * @return the number of smells linked.
     */
    public long smellsLinked(){
        return this.getScorer().bestMatch().size();
    }

    public void setTrackGraph(Graph trackGraph) {
        this.trackGraph = trackGraph;
    }

    public void setCondensedGraph(Graph condensedGraph) {
        this.condensedGraph = condensedGraph;
    }

    public void setTail(Vertex tail) {
        this.tail = tail;
    }

    /**
     * Return the map that contains all the unique smell objects from the condensed graph.
     * @return a map where the keys are the UNIQUE_SMELL_ID and the values are the smell vertices with the
     * corresponding UNIQUE_SMELL_ID.
     */
    public Map<Long, Vertex> getUniqueSmellsMap() {
        return uniqueSmellsMap;
    }

    /**
     * Encapsulates a vertex to quickly check whether it was updated or not.
     */
    private static class CachedVertex {
        boolean updated;
        Vertex vertex;

        public CachedVertex(Vertex vertex) {
            this.vertex = vertex;
            updated = false;
        }

        @Override
        public boolean equals(Object o) {
            return this.vertex.equals(o);
        }

        @Override
        public int hashCode() {
            return vertex.hashCode();
        }
    }
}
