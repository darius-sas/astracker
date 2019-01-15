package org.rug.data.characteristics;

import org.rug.data.smells.UDSmell;

public class InstabilityGap extends AbstractSmellCharacteristic {
    /**
     * Sets up the name of this smell characteristic.
     */
    protected InstabilityGap() {
        super("instabilityGap");
    }

    @Override
    public double calculate(UDSmell smell) {
        double centreInstability = smell.getCentre().value("instability");
        double badDepAvrgInstability = smell.getTraversalSource().V(smell.getBadDep())
                .values("instability")
                .toStream()
                .mapToDouble(o -> Double.parseDouble(o.toString()))
                .average().orElse(0);
        return centreInstability - badDepAvrgInstability;
    }
}