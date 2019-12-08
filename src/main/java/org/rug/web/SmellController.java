package org.rug.web;

import org.apache.tinkerpop.gremlin.process.traversal.IO;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.rug.tracker.ASmellTracker;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SmellController {

    @RequestMapping(value = "/smell-list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public SmellList smellList(@RequestParam(value="system", defaultValue="") String system){
        var graphFile = "./test-data/output/trackASOutput/antlr/condensed-graph-consecOnly.graphml";
        var graph = TinkerGraph.open();
        graph.traversal().io(graphFile).read().with(IO.reader, IO.graphml).iterate();
        return new SmellList(graph);
    }
}
