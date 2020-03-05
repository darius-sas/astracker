package org.rug.runners;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.rug.data.labels.EdgeLabel;
import org.rug.data.labels.VertexLabel;
import org.rug.data.project.IProject;
import org.rug.persistence.EdgeCountGenerator;
import org.rug.persistence.PersistenceHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FanInFanOutCounterRunner extends ToolRunner {

    private Logger logger = LogManager.getLogger(FanInFanOutCounterRunner.class);

    private IProject project;

    public FanInFanOutCounterRunner(IProject project) {
        super(null, null);
        this.project = project;
    }

    @Override
    public int run() {
        int exitCode = 0;
        if (this.project.versions().size() == 0){
            logger.error("Cannot measure size of a project with no versions.");
            exitCode = -1;
        }else {
            project.forEach(version -> {
                var g = version.getGraph().traversal();
                var data = g.V().hasLabel(P.within(VertexLabel.getTypesStrings()))
                        .project("name","fanOutWeightList", "fanInWeightList")
                        .by("name")
                        .by(__.outE().hasLabel(P.within(EdgeLabel.getAllDependencyStrings())).values("Weight").fold())
                        .by(__.inE().hasLabel(P.within(EdgeLabel.getAllDependencyStrings())).values("Weight").fold())
                        .toList();
                var records = new ArrayList<List<String>>();
                for (Map<String, Object> datum : data) {
                    var record = new ArrayList<String>();
                    record.add(project.getName());
                    record.add(version.getVersionString());
                    record.add(version.getVersionDate());
                    record.add(String.valueOf(version.getVersionIndex()));
                    record.add(datum.get("name").toString());
                    var fanIn = ((List<Integer>)datum.get("fanInWeightList")).stream().mapToInt(v -> v).sum();
                    var fanOut = ((List<Integer>)datum.get("fanOutWeightList")).stream().mapToInt(v -> v).sum();
                    record.add(String.valueOf(fanIn));
                    record.add(String.valueOf(fanOut));
                    records.add(record);
                }
                PersistenceHub.sendToAndWrite(EdgeCountGenerator.class, records);
            });
        }

        return exitCode;
    }

    @Override
    protected void preProcess() {

    }

    @Override
    protected void postProcess(Process p) throws IOException {

    }
}
