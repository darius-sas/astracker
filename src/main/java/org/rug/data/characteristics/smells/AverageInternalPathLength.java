package org.rug.data.characteristics.smells;

import org.apache.tinkerpop.gremlin.process.computer.traversal.step.map.ShortestPath;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.smells.HLSmell;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class AverageInternalPathLength extends AbstractSmellCharacteristic {
    /**
     * Sets up this smell characteristic.
     *
     */
    public AverageInternalPathLength() {
        super("avrgInternalPathLength");
    }


    @Override
    public String visit(HLSmell smell) {
        var g = smell.getAffectedGraph().traversal();
        var affectedPackage = smell.getAffectedElements().iterator().next();
        if (!affectedPackage.label().equals(VertexLabel.PACKAGE.toString()))
            return "-1";
        var inDep = g.V(smell.getInDep())
                .in(EdgeLabel.BELONGSTO.toString())
                .where(__.out(EdgeLabel.ISAFFERENTOF.toString())
                         .is(affectedPackage))
                .out(EdgeLabel.DEPENDSON.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                        .is(affectedPackage))
                .toSet();
        var outDep = g.V(smell.getOutDep())
                .in(EdgeLabel.BELONGSTO.toString())
                .where(__.out(EdgeLabel.ISEFFERENTOF.toString())
                        .is(affectedPackage))
                .in(EdgeLabel.DEPENDSON.toString())
                .where(__.out(EdgeLabel.BELONGSTO.toString())
                        .is(affectedPackage))
                .toSet();

        var paths = g.withComputer().V(inDep).
                shortestPath()
                .with(ShortestPath.target, __.is(P.within(outDep)))
                .with(ShortestPath.includeEdges, false)
                .with(ShortestPath.edges, __.outE(EdgeLabel.DEPENDSON.toString()))
                .toList();

        var averageLength = paths.stream().mapToDouble(Path::size).average().orElse(0d);
        return String.format("%.2f", averageLength);
    }
}
