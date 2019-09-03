package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;
import org.rug.data.labels.EdgeLabel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents an Unstable dependency smell.
 */
public class UDSmell extends SingleElementSmell {

    private Set<Vertex> badDep;

    /**
     * Builds an architectural smell instance of a UD smell starting from the given vertex.
     * @param smell the vertex to use.
     */
    public UDSmell(Vertex smell) {
        super(smell, Type.UD);
        this.badDep = smell.graph().traversal().V(smell).out(EdgeLabel.UDBADDEP.toString()).toSet();
    }
    
    public UDSmell(Vertex smell, Type type) {
        super(smell, type);
        this.badDep = smell.graph().traversal().V(smell).out(EdgeLabel.UDBADDEP.toString()).toSet();
    }

    @Override
    public void setAffectedElements(Vertex smell) {
        this.affectedElements = new HashSet<>();
        this.affectedElements.add(smell.graph().traversal().V(smell).out(EdgeLabel.UDAFFECTED.toString()).next());
    }

    /**
     * Gets the set of outgoing dependencies to the element affected by this smell.
     * @return an unmodifiable set.
     */
    public Set<Vertex> getBadDep() {
        return Collections.unmodifiableSet(badDep);
    }

    /**
     * Gets the set of outgoing dependencies to the element affected by this smell as ma,es.
     * @return a set of strings.
     */
    public Set<String> getBadDepNames(){return getBadDep().stream().map(v -> v.property("name").toString()).collect(Collectors.toSet());}

    /**
     * UD is only defined at package Level, so we set it like that by default
     * @param smell the smell this instance is instantiated from.
     */
    @Override
    protected void setLevel(Vertex smell) {
        setLevel(Level.PACKAGE);
    }

    @Override
    public <R> R accept(SmellVisitor<R> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UDSmell)
            return super.equals(o) && ((UDSmell) o).badDep.equals(badDep);
        else
            return false;
    }
}
