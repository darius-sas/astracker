package org.rug.data.characteristics.smells;

import org.rug.data.smells.CDSmell;
import org.rug.data.smells.GCSmell;
import org.rug.data.smells.HLSmell;
import org.rug.data.smells.UDSmell;

public class CentreComponent extends AbstractSmellCharacteristic{

    public CentreComponent() {
        super("centreComponent");
    }

    @Override
    public String visit(CDSmell smell) {
        return "";
    }

    @Override
    public String visit(HLSmell smell) {
        return smell.getCentreName();
    }

    @Override
    public String visit(UDSmell smell) {
        return smell.getCentreName();
    }

    @Override
    public String visit(GCSmell smell) {
        return smell.getCentreName();
    }
}
