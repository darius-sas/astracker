package org.rug.data.smells.factories;

import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

public class StarCDEvolver extends CDEvolver {

    public int leaves;

    /**
     * Builds a StarCDEvolver that with default leaves set to 3;
     * @param system the system to use
     */
    public StarCDEvolver(Graph system) {
        this(system, 4);
    }

    /**
     * Builds a StarCDEvolver that creates star-shaped smells with the given amount of leaves
     * @param system the system to use
     * @param elements the number of elements
     */
    public StarCDEvolver(Graph system, int elements){
        super(system);
        this.leaves = elements - 1;
        if (leaves < 3 || leaves + 1 > g.V().count().next())
            throw new IllegalArgumentException("The number of leaves of a star must be higher than 3 or the elements of the containing system must be enough to support its creation.");

    }

    /**
     * Adds a smell to the given set of vertices. The type of the smell depends on the implementation
     *
     * @param vertices the vertices target
     */
    @Override
    public void addSmell(Set<Vertex> vertices) {
        if (vertices.size() < leaves + 1)
            throw new IllegalArgumentException("The number of elements in the given set of vertices does not allow to create a smell with the given number of leaves.");

        Vertex centre = new ArrayList<>(vertices).get(rng.nextInt(vertices.size()));

        Vertex star = g.addV(VertexLabel.CYCLESHAPE.toString())
                .property("shapeType", CDSmell.Shape.STAR.toString())
                .property("smellId", rng.nextInt())
                .next();

        Set<Vertex> leafVertices = vertices.stream().filter(v -> !v.equals(centre)).collect(Collectors.toSet());
        addLeaves(centre, star, leafVertices);
        g.addE(EdgeLabel.ISCENTREOFSTAR.toString()).from(star).to(centre).next();
    }

    private void addLeaves(Vertex centre, Vertex star, Set<Vertex> leafVertices) {
        for (Vertex leaf : leafVertices){
            g.addE(EdgeLabel.DEPENDSON.toString()).from(centre).to(leaf).next();
            g.addE(EdgeLabel.DEPENDSON.toString()).from(leaf).to(centre).next();
            Vertex smell = g.addV(VertexLabel.SMELL.toString())
                    .property("smellType", ArchitecturalSmell.Type.CD.toString())
                    .property("vertexType", ArchitecturalSmell.Level.PACKAGE.toString()).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(leaf).next();
            g.addE(EdgeLabel.PARTOFCYCLE.toString()).from(smell).to(centre).next();
            g.addE(EdgeLabel.PARTOFSTAR.toString()).from(star).to(smell).next();
        }
    }

    /**
     * Add the given amount of elements to the given smell.
     *
     * @param smell the smell to enlarge
     * @param n     the number of nodes to add. Some smell types might support addition to multiple parts.
     */
    @Override
    public void addElements(ArchitecturalSmell smell, int... n) {
        Vertex shape = g.V(smell.getSmellNodes()).in(EdgeLabel.PARTOFSTAR.toString()).next();
        Vertex centre = g.V(shape).out(EdgeLabel.ISCENTREOFSTAR.toString()).next();
        addLeaves(centre, shape, getVerticesNotAffectedBySmell(n[0]));
    }


    public void setLeaves(int leaves) {
        this.leaves = leaves;
    }

    public int getLeaves() {
        return leaves;
    }
}
