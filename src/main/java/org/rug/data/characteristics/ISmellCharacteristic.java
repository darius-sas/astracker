package org.rug.data.characteristics;

import org.rug.data.SmellVisitor;

/**
 * Models a SmellCharacteristics that returns a value of type R
 */
public interface ISmellCharacteristic extends SmellVisitor<String> {

    /**
     * Returns the name of this characteristic.
     * @return the name.
     */
    String getName();

}
