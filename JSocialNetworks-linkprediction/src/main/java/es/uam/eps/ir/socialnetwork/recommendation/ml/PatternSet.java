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
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import it.unimi.dsi.fastutil.doubles.Double2ObjectMap;
import it.unimi.dsi.fastutil.doubles.Double2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.openide.util.Exceptions;
import org.ranksys.core.util.Stats;

/**
 * Represents a machine learning dataset, formed by a set of different patterns.
 * @author Javier Sanz-Cruzado Puig
 */
public class PatternSet 
{
    /**
     * Number of attributes for each pattern.
     */
    private final int numAttrs;
    /**
     * Mapping between attribute names an their position in the attributes list.
     */
    private final Object2IntMap<String> attrNames;
    /**
     * Types of the attributes. Only two possible values are considered: Nominal and Continuous. Classes
     * are differently treated.
     */
    private final List<AttrType> attrTypes;
    /**
     * Mapping between the different values for the class, and their integer identifier.
     */
    private final Object2IntMap<String> classes;
    /**
     * Mappings between the different nominal values of nominal attributes and doubles.
     */
    private final Int2ObjectMap<Object2DoubleMap<String>> dictionaries;
    /**
     * Inverse mappings
     * 
     */
    private final Int2ObjectMap<Double2ObjectMap<String>> invDictionaries;
    /**
     * Pattern set. Contains the attributes and the classes values for each individual pattern.
     */
    private final List<Pattern> data;
    /**
     * Number of classes
     */
    private final int numClasses;
    /**
     * Number of patterns
     */
    private int numPatterns;

    /**
     * Statistics about each pattern.
     */
    private final List<Stats> stats;
    /**
     * Constructor. This constructor is used when there is no information about the classes of a certain set of patterns.
     * @param numAttr Number of attributes of each individual pattern.
     * @param attrNames Names of the attributes.
     * @param attrTypes Types of the attributes.
     * @param dictionaries Relation between nominal values and double values.
     * @param patterns The pattern set.
     */
    public PatternSet(int numAttr, List<String> attrNames, List<AttrType> attrTypes, Int2ObjectMap<Object2DoubleMap<String>> dictionaries, List<List<Double>> patterns)
    {
        this(numAttr, attrNames, attrTypes, new HashSet<>(), dictionaries, patterns, new ArrayList<>());
    }
    
    /**
     * Constructor. This constructor is used when there is no information about the classes of a certain set of patterns.
     * @param numAttr Number of attributes of each individual pattern.
     * @param attrNames Names of the attributes.
     * @param attrTypes Types of the attributes.
     * @param classes The different possible classes for the patterns.
     * @param dictionaries Relation between nominal values and double values.
     * @param patterns The attributes values for each individual pattern.
     * @param patClass The classes for each individual pattern.
     */
    public PatternSet(int numAttr, List<String> attrNames, List<AttrType> attrTypes, Set<String> classes, Int2ObjectMap<Object2DoubleMap<String>> dictionaries, List<List<Double>> patterns, List<String> patClass)
    {
        // Initialize the number of attributes
        this.numAttrs = numAttr;
        
        // Initialize the list of attribute names
        this.attrNames = new Object2IntOpenHashMap<>();
        this.stats = new ArrayList<>();
        this.attrNames.defaultReturnValue(-1);
        for(int i = 0; i < attrNames.size(); ++i)
        {
            this.attrNames.put(attrNames.get(i), i);
            this.stats.add(new Stats());
        }
        
        // Initialize the list of attribute types
        this.attrTypes = attrTypes;
        
        // Initialize the list of classes
        this.classes = new Object2IntOpenHashMap<>();
        this.classes.defaultReturnValue(Pattern.ERROR_CLASS);
        if(!classes.isEmpty())
        {
            int i = 0;
            for(String category : classes)
            {
                this.classes.put(category, i);
                ++i;
            }
            this.numClasses = i;
        }
        else
        {
            this.numClasses = 0;
        }
          
        this.dictionaries = dictionaries;
        this.invDictionaries = new Int2ObjectOpenHashMap<>();
        
        this.dictionaries.keySet().stream().forEach(key -> {
            this.invDictionaries.put((int)key, new Double2ObjectOpenHashMap<>());
            this.dictionaries.get((int)key).object2DoubleEntrySet().forEach(entry -> this.invDictionaries.get((int)key).put(entry.getDoubleValue(), entry.getKey()));
        });
        
        
        // Initialize the patterns
        this.data = new ArrayList<>();
        
        this.numPatterns = 0;
        if(patClass.isEmpty() || classes.isEmpty()) //If classes are not present
        {
            for(int i = 0; i < patterns.size(); ++i)
            {
                this.addPattern(patterns.get(i));
                for(int j = 0; j < this.numAttrs; ++j)
                {
                    this.stats.get(j).accept(patterns.get(i).get(j));
                }
            }
            
        }
        else //If they are present
        {
            for(int i = 0; i < patterns.size(); ++i)
            {
                this.addPattern(patterns.get(i), patClass.get(i));
                for(int j = 0; j < this.numAttrs; ++j)
                {
                    this.stats.get(j).accept(patterns.get(i).get(j));
                }
            }
        }
        
    }
    
    /**
     * Adds a pattern to the set. The class is considered to be undetermined or unknown.
     * @param pattern The list of attributes.
     * @return true if everything went OK, false if not.
     */
    protected final boolean addPattern(List<Double> pattern)
    {
        Pattern pat = new Pattern(pattern);
        this.data.add(pat);
        this.numPatterns++;
        return true;
    }
    
    /**
     * Adds a pattern to the set
     * @param pattern The list of attribute values.
     * @param category The category the pattern belongs to.
     * @return true if the pattern was correctly added, false if not.
     */
    protected final boolean addPattern(List<Double> pattern, String category)
    {
        if(this.classes.containsKey(category))
        {
            Pattern pat = new Pattern(pattern, this.classes.getInt(category));
            this.data.add(pat);
            this.numPatterns++;
            return true;
        }
        return false;
    }
    
    /**
     * Obtains the basic information about the attributes of the patterns: their name and their type.
     * @return A list containing tuples formed by the name and type of each attribute.
     */
    public List<Tuple2oo<String, AttrType>> getAttributes()
    {
        List<Tuple2oo<String, AttrType>> attributes = new ArrayList<>();
        
        for(int i = 0; i < this.numAttrs; ++i)
        {
            attributes.add(new Tuple2oo(1,AttrType.CLASS));
        }
        
        this.attrNames.object2IntEntrySet().stream()
                .forEach(entry -> attributes.set(entry.getIntValue(), new Tuple2oo<>(entry.getKey(), this.attrTypes.get(entry.getIntValue()))));
        
        return attributes;
    }
    
    /**
     * Obtains the possible values for the class attribute.
     * @return the different possible classes.
     */
    public List<String> getClasses()
    {
        List<String> categories = new ArrayList<>(this.classes.keySet());
        return categories;
    }
    
    /**
     * Gets the number of patterns in the dataset.
     * @return the number of patterns.
     */
    public int getNumPatterns()
    {
        return this.numPatterns;
    }
    
    /**
     * Gets the number of attributes of each pattern (class not included).
     * @return the number of attributes.
     */
    public int getNumAttrs()
    {
        return this.numAttrs;
    }
    
    /**
     * Gets the number of different classes.
     * @return the number of different classes.
     */
    public int getNumClasses()
    {
        return this.numClasses;
    }
    
    /**
     * Gets an specific pattern.
     * @param id the identifier.
     * @return the pattern.
     */
    public Pattern getPattern(int id)
    {
        if(id >= 0 && id < this.numPatterns)
            return this.data.get(id);
        return null;
    }
 
    /**
     * Gets all the patterns.
     * @return a stream containing all the patters.
     */
    public Stream<Pattern> getPatterns()
    {
        return this.data.stream();
    }
    
    /**
     * Obtains the number of different values that a nominal attribute has.
     * @param attribute the name of the nominal attribute.
     * @return the number of different values if the attributes exists and it is nominal, 
     * 0 if it is continuous, -1 if it does not even exist
     */
    public int numValues(String attribute)
    {
        if(!this.attrNames.containsKey(attribute))
            return -1;
        else if(this.attrTypes.get(this.attrNames.getInt(attribute)) == AttrType.CONTINUOUS)
            return 0;
        else
            return this.dictionaries.get(this.attrNames.getInt(attribute)).keySet().size();
    }
    
    /**
     * Obtains the statistics for all the attributes
     * @return a list containing the statistics.
     */
    public List<Stats> getStats()
    {
        return stats;
    }
    
    /**
     * Reads a pattern set from a file.
     * <ul>
     *  <li>Line 1: Names of the attributes</li>
     *  <li>Line 2: Types of the attributes. 3 possible values: nominal (Nominal attributes), continuous (Continuous attributes), class </li>
     *  <li>Line 3-X: Patterns</li>
     * </ul>
     * @param inputFile File that contains the pattern set.
     * 
     * @param separator Separator for the different attributes.
     * @param readClasses indicates if the classes for patterns have to be stored or not. if they exist, they will be stored, independently of this value.
     * @return the pattern set if everything goes OK, null if not
     Possible causes of error:
     * <ul>
     *  <li>Different number of types and names</li>
     *  <li>More than one class attribute</li>
     *  <li>No class and the reader has been set to read the different classes</li>
     *  <li>Invalid attribute type</li>
     *  <li>Incomplete pattern</li>
     * </ul>
     */
    public static PatternSet read(String inputFile, String separator, boolean readClasses)
    {
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputFile))))
        {
            
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

            
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split(separator);
                if(split.length != namesSplit.length) // Error: Incomplete pattern
                    return null;
                
                List<Double> pattern = new ArrayList<>();
                for(int i = 0; i < split.length; ++i)
                {
                    int j = (classIndex != -1 && classIndex < i) ? (i-1) : i;

                    if(classIndex == i) // Read the class
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
                return new PatternSet(numAttr, attrNames, attrTypes, classes, dictionaries, patValues, patClass);
            }
            else
            {
                return new PatternSet(numAttr, attrNames, attrTypes, dictionaries, patValues);
            }
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Int2ObjectMap<Object2DoubleMap<String>> getDictionaries() {
        return this.dictionaries;
    }
    
    public double getNominalAttributeDoubleValue(int numAttr, String value)
    {
        if(this.attrTypes.size() < numAttr || numAttr < 0 || this.attrTypes.get(numAttr).equals(AttrType.CONTINUOUS))
            return Double.NaN;
        else if(!this.dictionaries.get(numAttr).containsKey(value))
            return Double.NaN;
        else
            return this.dictionaries.get(numAttr).getDouble(value);
    }
    
    public String getNominalAttributeStringValue(int numAttr, double value)
    {
        if(this.attrTypes.size() < numAttr || numAttr < 0 || this.attrTypes.get(numAttr).equals(AttrType.CONTINUOUS))
            return null;
        else if(!this.invDictionaries.get(numAttr).containsKey(value))
            return null;
        else
            return this.invDictionaries.get(numAttr).get(value);
    }
    
    
}
