package org.rug.simpletests.runners;

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.junit.jupiter.api.Test;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.persistence.EdgeCountGenerator;
import org.rug.persistence.PersistenceHub;
import org.rug.runners.FanInFanOutCounterRunner;

import static org.rug.simpletests.TestData.antlr;

class ComponentEdgesRunnerTest {

    @Test
    void testProjectStep(){
        var g = antlr.getVersionWith(3).getGraph().traversal();
        var s = g.V().hasLabel(P.within(VertexLabel.getTypesStrings()))
                .project("name","fanOutWeightList", "fanInWeightList")
                .by("name")
                .by(__.outE().hasLabel(P.within(EdgeLabel.getAllDependencyStrings())).values("Weight").fold())
                .by(__.inE().hasLabel(P.within(EdgeLabel.getAllDependencyStrings())).values("Weight").fold())
                .toList();

        s.forEach( m -> System.out.println(String.format("%s, %s, %s", m.get("name"), m.get("fanOutWeightList"), m.get("fanInWeightList"))));
    }

    @Test
    void run() {
        PersistenceHub.register(new EdgeCountGenerator("test-data/output/trackASOutput/antlr/edges-count.csv"));
        new FanInFanOutCounterRunner(antlr).run();
        PersistenceHub.closeAll();
    }
}