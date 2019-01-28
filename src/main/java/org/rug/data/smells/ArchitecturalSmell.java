package org.rug.data.smells;

import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.rug.data.SmellVisitor;
import org.rug.data.labels.VertexLabel;
import org.rug.data.characteristics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstraction of an AS. A smell is composed by the nodes that represent the smell (label
 * <code>VertexLabel.SMELL</code>), and the nodes that are affected by the smell (label
 * <code>VertexLabel.PACKAGE || VertexLabel.CLASS</code>.
 */
public abstract class ArchitecturalSmell {

    private final static Logger logger = LoggerFactory.getLogger(ArchitecturalSmell.class);

    private long id;
    protected Set<Vertex> smellNodes;
    protected Set<Vertex> affectedElements;
    protected Graph affectedGraph;

    protected Map<String, String> characteristicsMap;

    private Type type;
    private Level level;

    /**
     * Initializes this smell instance starting from the smell node
     * @param smell the smell that characterizes this instance.
     */
    protected ArchitecturalSmell(Vertex smell, Type type){
        assert smell.label().equals(VertexLabel.SMELL.toString());
        this.id = Long.parseLong(smell.id().toString());
        this.type = type;
        this.characteristicsMap = new HashMap<>();
        this.affectedGraph = smell.graph();
        setLevel(smell);
        setAffectedElements(smell);
        setSmellNodes(smell);
    }

    /**
     * Returns the id of the node representing this smell.
     * @return The long value representing the id of this smell in the Graph of the system.
     */
    public long getId() {
        return id;
    }

    /**
     * Get the vertices that describe this smell.
     * @return An unmodifiable set of vertices.
     */
    public Set<Vertex> getSmellNodes() {
        return Collections.unmodifiableSet(smellNodes);
    }

    /**
     * Set the smell nodes that describe this smell.
     * @param smellNodes the vertices that describe the nodes
     */
    protected void setSmellNodes(Set<Vertex> smellNodes) {
        this.smellNodes = smellNodes;
    }

    /**
     * Get the vertices that are affected by this smell.
     * @return an unmodifiable set of the affected elements.
     */
    public Set<Vertex> getAffectedElements() {
        return affectedElements;
    }

    /**
     * Sets the affected elements of this smell.
     * @param affectedElements the elements affected by this smell.
     */
    protected void setAffectedElements(Set<Vertex> affectedElements) {
        this.affectedElements = affectedElements;
    }

    /**
     * Sets the affected elements of the smell from a <code>VertexLabel.SMELL</code> vertex.
     * @param smell the starting node
     */
    protected abstract void setAffectedElements(Vertex smell);

    /**
     * Sets the smell nodes that characterize this instance
     * @param smell the starting smell node. This will be mostly the only element in this set.
     */
    protected abstract void setSmellNodes(Vertex smell);

    /**
     * Triggers the calculation of each characteristic using the correct implementation of CharacteristicsSet for the
     * current smell type. The results of the calculation are saved internally in a map
     * retrievable using <code>getCharacteristicsMap()</code>.
     */
    public void calculateCharacteristics(){
        Set<ISmellCharacteristic> characteristicsSets = this.type.getCharacteristicsSet();
        for (ISmellCharacteristic characteristic : characteristicsSets){
            String value = this.accept(characteristic);
            characteristicsMap.put(characteristic.getName(), value);
        }
    }

    /**
     * Accepts a smell visitor.
     * @param visitor the visitor to accept.
     * @return the eventual value returned by this visitor
     */
    public abstract <T> T accept(SmellVisitor<T> visitor);

    /**
     * Get the map of the currently computed characteristics.
     * @return an unmodifiable map containing the results of the characteristics.
     * The keys of the map are the name of the characteristics whereas the values is the computed value for that key.
     */
    public Map<String, String> getCharacteristicsMap() {
        return characteristicsMap;
    }

    /**
     * Gets the type of components that this smell affects (i.e. class or package).
     * @return The type of components affected by the smell.
     */
    public Level getLevel() {
        return level;
    }

    /**
     * Set the type of components this smell affects.
     * @param level the level.
     */
    protected void setLevel(Level level) {
        this.level = level;
    }

    /**
     * Gets the type of this smell.
     * @return The type of this smell.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of the smell
     * @param type the type of the smell.
     */
    protected void setType(Type type) {
        this.type = type;
    }

    /**
     * Sets the level of this smell starting from the vertex describing it.
     * @param smell the vertex that describes it.
     */
    protected void setLevel(Vertex smell){
        setLevel(Level.fromString(smell.value("vertexType")));
    }

    /**
     * Returns the traversal of the graph affected by this smell.
     * @return the traversal
     */
    public GraphTraversalSource getTraversalSource(){
        return getAffectedGraph().traversal();
    }

    /**
     * Returns the graph affected by this smell.
     * @return the graph
     */
    public Graph getAffectedGraph() {
        return affectedGraph;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("ID:         " + id); builder.append(System.lineSeparator());
        builder.append("SmellNodes: " + smellNodes); builder.append(System.lineSeparator());
        builder.append("Affected:   " + affectedElements.stream().map(v -> v.property("name").value().toString()).sorted().collect(Collectors.toList())); builder.append(System.lineSeparator());
        builder.append("Type:       " + type);  builder.append(System.lineSeparator());
        if (this instanceof CDSmell) {
            builder.append("Shape:      " + ((CDSmell) this).getShape());
            builder.append(System.lineSeparator());
        }

        return builder.toString();
    }

    /**
     * Two smells are identical if they affect the same elements, have the same type. (Further checks may be made by subclasses)
     * @param o the other smell to check
     * @return true if this smell is equal to the other smell.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof ArchitecturalSmell))
            return false;

        ArchitecturalSmell other = (ArchitecturalSmell)o;

        if (this == other)
            return true;

        if (this.type != other.type)
            return false;

        return this.affectedElements.equals(other.affectedElements);
    }

    @Override
    public int hashCode() {
        return (int)id + smellNodes.hashCode() ^ 2 + affectedElements.hashCode() ^ 3 + super.hashCode() ^ 4;
    }


    /**
     * Represents a type of AS and maps them to their instantiation and characteristics set.
     */
    public enum Type {
        CD("cyclicDep", CDSmell::new, new CDCharacteristicsSet()),
        UD("unstableDep", UDSmell::new, new UDCharacteristicsSet()),
        HL("hubLikeDep", HLSmell::new, new HLCharacteristicsSet()),
        ICPD("ixpDep", vertex -> null, null),
        MAS("multipleAS", vertex -> null, null),
        ;

        private String value;
        private Function<Vertex, ArchitecturalSmell> smellInstantiator;
        private CharacteristicsSet characteristicsSet;

        Type(String value, Function<Vertex, ArchitecturalSmell> smellInstantiator, CharacteristicsSet characteristicsSet){
            this.value = value;
            this.smellInstantiator = smellInstantiator;
            this.characteristicsSet = characteristicsSet;
        }

        public ArchitecturalSmell getInstance(Vertex vertex){
            return this.smellInstantiator.apply(vertex);
        }

        /**
         * Returns the CharacteristicsSet instance of the current type.
         * @return the correct instance of CharacteristicsSet that can be used to compute a smell's characteristics.
         */
        public Set<ISmellCharacteristic> getCharacteristicsSet() {
            return characteristicsSet.getCharacteristicSet();
        }

        @Override
        public String toString() {
            return value;
        }

        /**
         * Retrieves the Type of starting from the given string. A lookup table is used to enhance performance.
         * @param name The type of the smell as a string.
         * @return The type of this smell or null if the string is not present in the lookup table and in this enum.
         */
        public static Type fromString(String name){
            return lookup.get(name);
        }

        private static final Map<String, Type> lookup = new HashMap<>();

        static
        {
            for(Type type : Type.values())
            {
                lookup.put(type.value, type);
            }
        }

    }

    /**
     * Describes the level at which a smell is detected: class or package.
     */
    public enum Level {
        CLASS("class"),
        PACKAGE("package");

        private final String level;

        Level(String level){
            this.level = level;
        }

        public static Level fromString(String name){
            return lookup.get(name);
        }

        private static final Map<String, Level> lookup = new HashMap<>();

        static
        {
            for(Level type : Level.values())
            {
                lookup.put(type.level, type);
            }
        }

        @Override
        public String toString() {
            return level;
        }
    }
}
