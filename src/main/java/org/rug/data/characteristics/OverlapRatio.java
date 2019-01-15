package org.rug.data.characteristics;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.ArchitecturalSmell;
import org.rug.data.smells.CDSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

import java.util.HashSet;
import java.util.Set;

public class OverlapRatio extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected OverlapRatio() {
        super("overlapRatio");
    }

    @Override
    public double calculate(CDSmell smell) {
        return calculateOverlapRatio(smell.getAffectedElements(), smell);
    }

    @Override
    public double calculate(HLSmell smell) {
        Set<Vertex> vertices = new HashSet<>();
        vertices.addAll(smell.getInDep());
        vertices.addAll(smell.getOutDep());
        vertices.add(smell.getCentre());
        return calculateOverlapRatio(vertices, smell);
    }

    @Override
    public double calculate(UDSmell smell) {
        Set<Vertex> vertices = new HashSet<>();
        vertices.add(smell.getCentre());
        vertices.addAll(smell.getBadDep());
        return calculateOverlapRatio(vertices, smell);
    }

    /**
     * Calculates the overlap ratio of the given vertices (assumed to be of the given smell) with other smells.
     * @param vertices the vertices to consider as affected by other smells
     * @param smell the reference smell
     * @return the ratio of affected elements from vertices by more than one smell
     */
    private double calculateOverlapRatio(Set<Vertex> vertices, ArchitecturalSmell smell){
        double elementsAffectedByMoreThanOneSmell = 0;
        GraphTraversalSource g = smell.getTraversalSource();
        for (Vertex c : vertices) {
            if(g.V(c).in().hasLabel(VertexLabel.SMELL.toString())
                    .is(P.not(P.within(smell.getSmellNodes())))
                    .count().next() > 0)
                elementsAffectedByMoreThanOneSmell++;
        }
        return elementsAffectedByMoreThanOneSmell / vertices.size();
    }
}