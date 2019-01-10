/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml;

import es.uam.eps.ir.socialnetwork.recommendation.ml.attributes.AttrType;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.openide.util.Exceptions;

/**
 * Class that represents a set of patterns for a certain graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class GraphPatternSet<U> extends PatternSet 
{
    /**
     * Map between users and the patterns for each adjacent graph.
     */
    private final Object2ObjectMap<U, Object2IntMap<U>> adjacentPatterns;
    /**
     * Map between users and the patterns for each incident graph.
     */
    private final Object2ObjectMap<U, Object2IntMap<U>> incidentPatterns;
    /**
     * The list of pairs of users represented by those edges.
     */
    private final List<Pair<U>> edges;
    
    /**
     * Constructor
     * @param numAttr Number of attributes of each individual pattern.
     * @param attrNames Names of the attributes.
     * @param attrTypes Types of the attributes.
     * @param dictionaries Relation between nominal values and double values.
     * @param patterns The pattern set.
     * @param graph The graph.
     * @param nodePairs Corresponding pairs of node for each instance.
     */
    public GraphPatternSet(int numAttr, List<String> attrNames, List<AttrType> attrTypes, Int2ObjectMap<Object2DoubleMap<String>> dictionaries, List<List<Double>> patterns, Graph<U> graph, List<Pair<U>> nodePairs) 
    {
        super(numAttr, attrNames, attrTypes, dictionaries, patterns);
        
        this.edges = nodePairs;
        this.adjacentPatterns = new Object2ObjectOpenHashMap<>();
        this.incidentPatterns = new Object2ObjectOpenHashMap<>();
        
        graph.getAllNodes().forEach(u -> {
            adjacentPatterns.put(u, new Object2IntOpenHashMap<>());
            incidentPatterns.put(u, new Object2IntOpenHashMap<>());
        });
        
        for(int i = 0; i < nodePairs.size(); ++i)
        {
            Tuple2oo<U,U> pair = nodePairs.get(i);
            
            this.adjacentPatterns.get(pair.v1()).put(pair.v2(), i);
            this.incidentPatterns.get(pair.v2()).put(pair.v1(), i);
        }
    }
    
    /**
     * Constructor. This constructor is used when there is no information about the classes of a certain set of patterns.
     * @param numAttr Number of attributes of each individual pattern.
     * @param attrNames Names of the attributes.
     * @param attrTypes Types of the attributes.
     * @param classes The different possible classes for the patterns.
     * @param dictionaries Relation between nominal values and double values.
     * @param patterns The attributes values for each individual pattern.
     * @param patClasses The classes for each individual pattern.
     * @param nodePairs Corresponding pairs of node for each instance.
     * @param graph The graph.
     */    
    public GraphPatternSet(int numAttr, List<String> attrNames, List<AttrType> attrTypes, Set<String> classes, Int2ObjectMap<Object2DoubleMap<String>> dictionaries, List<List<Double>> patterns, List<String> patClasses, Graph<U> graph,  List<Pair<U>> nodePairs) 
    {
        super(numAttr, attrNames, attrTypes, classes, dictionaries, patterns, patClasses);
        
        this.edges = nodePairs;
        this.adjacentPatterns = new Object2ObjectOpenHashMap<>();
        this.incidentPatterns = new Object2ObjectOpenHashMap<>();
        
        graph.getAllNodes().forEach(u -> {
            adjacentPatterns.put(u, new Object2IntOpenHashMap<>());
            incidentPatterns.put(u, new Object2IntOpenHashMap<>());
        });
        
        for(int i = 0; i < nodePairs.size(); ++i)
        {
            Tuple2oo<U,U> pair = nodePairs.get(i);
            
            this.adjacentPatterns.get(pair.v1()).put(pair.v2(), i);
            this.incidentPatterns.get(pair.v2()).put(pair.v1(), i);
        }
        
        System.out.println("dOne");
    }
    
    /**
     * Adds a pattern.
     * @param attributes The attribute values for the example.
     * @param pair The pair of nodes the pattern is related to. Both nodes had to previously belong to the graph.
     * @return true if everything went OK, false if not.
     */
    protected boolean addPattern(List<Double> attributes, Pair<U> pair)
    {
        U u = pair.v1();
        U v = pair.v2();
        
        boolean add = true;
        if(this.adjacentPatterns.containsKey(u) && this.incidentPatterns.containsKey(v))
        {
            if(this.adjacentPatterns.get(u).containsKey(v) || this.incidentPatterns.get(v).containsKey(u))
            {
                add = false;
            }
        }
        else
        {
            add = false;
        }
        
        if(add)
        {
            this.adjacentPatterns.get(u).put(v, this.getNumPatterns());
            this.incidentPatterns.get(v).put(u, this.getNumPatterns());
            this.edges.add(pair);
            return this.addPattern(attributes);
        }
        
        return false;
    }
    
    /**
     * Adds a pattern.
     * @param attributes The attribute values for the example.
     * @param category The class of the pattern
     * @param pair The pair of nodes the pattern is related to. Both nodes had to previously belong to the graph.
     * @return true if everything went OK, false if not.
     */
    protected boolean addPattern(List<Double> attributes, String category, Pair<U> pair)
    {
        U u = pair.v1();
        U v = pair.v2();
        
        boolean add = true;
        if(this.adjacentPatterns.containsKey(u) && this.incidentPatterns.containsKey(v))
        {
            if(this.adjacentPatterns.get(u).containsKey(v) || this.incidentPatterns.get(v).containsKey(u))
            {
                add = false;
            }
        }
        else
        {
            add = false;
        }
        
        if(add)
        {
            if(this.addPattern(attributes, category))
            {
                this.adjacentPatterns.get(u).put(v, this.getNumPatterns()-1);
                this.incidentPatterns.get(v).put(u, this.getNumPatterns()-1);
                this.edges.add(pair);
            }
        }
        
        return false;
    }
    
    /**
     * Gets the patterns for each adjacent nodes to a given one.
     * @param u The given node
     * @return The patterns for each adjacent node to the given one.
     */
    public Stream<Tuple2oo<U,Pattern>> getAdjacentPatterns(U u)
    {
        if(this.adjacentPatterns.containsKey(u))
        {
            return this.adjacentPatterns.get(u).object2IntEntrySet().stream().map(entry -> new Tuple2oo<>(entry.getKey(), this.getPattern(entry.getIntValue())));
        }
        else
            return Stream.empty();
    }
    
    /**
     * Gets all the patterns, including the edge they are refering to.
     * @return all the patterns, indexed by the pair of users the pattern refers to.
     */
    public Stream<Tuple2oo<Pair<U>, Pattern>> getAllPatterns()
    {
        List<Tuple2oo<Pair<U>, Pattern>> patterns = new ArrayList<>();
        
        this.adjacentPatterns.entrySet().stream().forEach(uEntry -> 
        {
            uEntry.getValue().object2IntEntrySet().stream().forEach(vEntry -> 
            {
                Pair<U> pair = new Pair<>(uEntry.getKey(), vEntry.getKey());
                patterns.add(new Tuple2oo<>(pair, this.getPattern(vEntry.getIntValue())));
            });
        });
        
        return patterns.stream();
    }
    
    /**
     * Reads a pattern set from a file.
     * <b>File Format:</b>
     * <ul>
     *  <li>Line 1: Names of the attributes</li>
     *  <li>Line 2: Types of the attributes. 3 possible values: nominal (Nominal attributes), continuous (Continuous attributes), class </li>
     *  <li>Line 3-X: NodeA \t NodeB \t Patterns</li>
     * </ul>
     * @param inputFile File that contains the pattern set.
     * 
     * @param separator Separator for the different attributes.
     * @param readClasses indicates if the classes for patterns have to be stored or not. if they exist, they will be stored, independently of this value.
     * @param directed if the graph is directed.
     * @param weighted if the graph is weighted.
     * @return the pattern set if everything goes OK, null if not
     * Possible causes of error:
     * <ul>
     *  <li>Different number of types and names</li>
     *  <li>More than one class attribute</li>
     *  <li>No class and the reader has been set to read the different classes</li>
     *  <li>Invalid attribute type</li>
     *  <li>Incomplete pattern</li>
     * </ul>
     */
    public static GraphPatternSet read(String inputFile, String separator, boolean readClasses, boolean directed, boolean weighted)
    {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))))
        {
            GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
            gg.configure(directed, weighted);
            Graph<Long> graph = gg.generate();
            
            // Read the headers
            
            // First line shows the names of some attributes.
            String namesLine = br.readLine();
            String[] namesSplit = namesLine.split(separator);
            
            // Second line shows the types of the attributes
            String typesLine = br.readLine();
            String[] typesSplit = typesLine.split(separator);
            
            if(namesSplit.length != typesSplit.length) // ERROR: The number of names and types has to be the same
                return null;
            
            
            // Initializing variables
            // The dictionaries to translate from nominal patterns to numbers
            Int2ObjectMap<Object2DoubleMap<String>> dictionaries = new Int2ObjectOpenHashMap<>();
            dictionaries.defaultReturnValue(new Object2DoubleOpenHashMap<>());
            
            // List of attribute names
            List<String> attrNames = new ArrayList<>();
            // List of attribute types.
            List<AttrType> attrTypes = new ArrayList<>();
            
            int classIndex = -1;
            
            // Configure the types and the names of the attributes
            for(int i = 0; i < typesSplit.length; ++i)
            {
                if(typesSplit[i].equalsIgnoreCase(AttrType.CLASS.name()))
                {
                    if(classIndex != -1) // ERROR: Only a class may exist.
                    {
                        return null;
                    }
                    
                    classIndex = i;
                }
                else
                {
                    attrNames.add(namesSplit[i]);
                    if(typesSplit[i].equalsIgnoreCase(AttrType.NOMINAL.name()))
                    {
                        attrTypes.add(AttrType.NOMINAL);
                        dictionaries.put(i, new Object2DoubleOpenHashMap<>());
                    }
                    else if(typesSplit[i].equalsIgnoreCase(AttrType.CONTINUOUS.name()))
                    {
                        attrTypes.add(AttrType.CONTINUOUS);
                    }
                    else // ERROR: Invalid type
                    {
                        return null;
                    }
                }
            }
            
            int numAttr = attrNames.size();
            
            if(readClasses && classIndex == -1) // ERROR: The class does not exist
            {
                return null;
            }
            
            Set<String> classes = new HashSet<>();

            // Read each one of the patterns
            List<List<Double>> patValues = new ArrayList<>();
            List<String> patClass = new ArrayList<>();
            List<Tuple2oo<Long,Long>> edges = new ArrayList<>();
            
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split(separator);
                if(split.length != namesSplit.length + 2) // Error: Incomplete pattern
                    return null;
                
                List<Double> pattern = new ArrayList<>();
                Long u = new Long(split[0]);
                Long v = new Long(split[1]);
                edges.add(new Tuple2oo<>(u,v));
                graph.addEdge(u, v);
                
                for(int i = 2; i < split.length; ++i)
                {
                    int j = (classIndex != -1 && classIndex < (i-2)) ? (i-3) : (i-2);

                    if(classIndex == (i-2)) // Read the class
                    {
                        classes.add(split[i]);
                        patClass.add(split[i]);
                    }
                    else if(attrTypes.get(j).equals(AttrType.NOMINAL)) // Read the nominal attribute
                    {
                        if(!dictionaries.get(j).containsKey(split[i]))
                        {
                            dictionaries.get(j).put(split[i], dictionaries.get(j).size()+0.0);
                        }
                        
                        double value = dictionaries.get(j).getDouble(split[i]); 
                        pattern.add(value);
                    }
                    else // Read the continuous attribute
                    {
                        pattern.add(new Double(split[i]));
                    }
                }
                
                patValues.add(pattern);
            }
            
            if(readClasses || classIndex != -1)
            {
                return new GraphPatternSet(numAttr, attrNames, attrTypes, classes, dictionaries, patValues, patClass, graph, edges);
            }
            else
            {
                return new GraphPatternSet(numAttr, attrNames, attrTypes, dictionaries, patValues, graph, edges);
            }
            
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
    
}
