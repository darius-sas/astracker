package org.rug.persistence;

import java.util.List;

public class EdgeCountGenerator extends CSVDataGenerator<List<List<String>>> {

    public EdgeCountGenerator(String outputFile) {
        super(outputFile);
    }

    @Override
    public String[] getHeader() {
        return new String[]{"project", "version", "versionDate", "versionIndex", "name", "fanIn", "fanOut"};
    }

    @Override
    public void accept(List<List<String>> object) {
        records.addAll(object);
    }
}
