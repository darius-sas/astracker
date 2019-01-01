package org.rug.tracker;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.EdgeLabel;
import org.rug.data.Pair;
import org.rug.data.VSetPair;
import org.rug.data.VertexLabel;

import java.util.*;

@SuppressWarnings("unchecked")
public class ASTracker {

    /**
     * Track CD smells from the first graph to the second one.
     * @param graphV1 The dependency graph of the system at v1 (current version)
     * @param graphV2 The dependency graph of the system at v2 (next version)
     * @return A map containing for each shape of smell (key) another map as a value. The second map stores, for each CD
     * smell in the system as key, a list of pairs of sets, where the first set of the pair contains the vertices of the smell
     * at the current version (v1) whereas the second pair contains the pairs of vertices of the identified smell.
     * In case a smell splits, multiple pairs will be contained in the list.
     */
    public static Map<String, Map<Vertex, List<VSetPair>>> trackCD(Graph graphV1, Graph graphV2) {
        // Forward pass
        GraphTraversalSource g1 = graphV1.traversal();
        GraphTraversalSource g2 = graphV2.traversal();

        Map<String, List<Vertex>> smellsInTheSystem = (Map<String, List<Vertex>>) (Map<?, ?>) g1.V()
                .hasLabel(VertexLabel.CYCLESHAPE.toString())
                .group()
                .by("shapeType").next();

        Map<String, Map<Vertex, List<VSetPair>>> smellMappingsPerType = new HashMap<>();

        for (Map.Entry<String, List<Vertex>> entry : smellsInTheSystem.entrySet()) {
            String shape = entry.getKey();
            List<Vertex> vertices = entry.getValue();
            switch (shape) {
                case "star":
                    smellMappingsPerType.put(shape, getMapping(g1, g2, vertices, EdgeLabel.PARTOFSTAR));
                    break;
                default:
                    break;
            }
        }
        // TODO return results as pairs of smell ids

        return smellMappingsPerType;
        // Backward pass
    }

    /**
     * Computes what vertices of g1 are present in g1 using the "name" property at package or class level.
     *
     * @param g1            the first graph to extract the smells from
     * @param g2            the second graph to extract the smells from. It is assumed that this is the evolution of g1
     * @param smellVertices the smells of the system
     * @param cdType        the edge label that describe the smell
     * @return A map where the keys is the smell vertex in smellVertices and the value is a Pair of sets,
     * with the first element containing vertices from g1 and the second element the elements from g2
     * part of the same smell.
     */
    private static Map<Vertex, List<VSetPair>> getMapping(GraphTraversalSource g1,
                                                          GraphTraversalSource g2,
                                                          List<Vertex> smellVertices,
                                                          EdgeLabel cdType) {

        Map<Vertex, List<VSetPair>> versionsMapping = new HashMap<>();
        for (Vertex smell : smellVertices) {
            List<VSetPair> values = new ArrayList<>();
            Set<String> vNames = (Set<String>) (Set<?>)
                    g1.V(smell)
                            .out(cdType.toString())
                            .out(EdgeLabel.PARTOFCYCLE.toString())
                            .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                            .values("name").toSet();
            Set<Vertex> v1 = g1.V()
                    .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                    .has("name", P.within(vNames))
                    .toSet();

            g2.V()
                    .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString()))
                    .has("name", P.within(vNames))
                    .in(EdgeLabel.PARTOFCYCLE.toString())
                    .in(cdType.toString())
                    .hasLabel(P.within(VertexLabel.CYCLESHAPE.toString()))
                    .forEachRemaining(smellVertex -> {
                        Set<Vertex> vn = g2.V(smellVertex)
                                .out(cdType.toString())
                                .out(EdgeLabel.PARTOFCYCLE.toString())
                                .hasLabel(P.within(VertexLabel.PACKAGE.toString(), VertexLabel.CLASS.toString())).toSet();
                        values.add(new VSetPair(v1, vn)); // this pair can also be substituted with smell id pairs
                    });
            versionsMapping.put(smell, values);
        }
        return versionsMapping;
    }
}
