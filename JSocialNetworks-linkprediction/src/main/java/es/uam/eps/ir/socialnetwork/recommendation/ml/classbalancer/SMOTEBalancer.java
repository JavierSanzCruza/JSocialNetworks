/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml.classbalancer;

import es.uam.eps.ir.socialnetwork.recommendation.ml.attributes.AttrType;
import static es.uam.eps.ir.socialnetwork.recommendation.ml.attributes.AttrType.CONTINUOUS;
import es.uam.eps.ir.socialnetwork.recommendation.ml.Pattern;
import es.uam.eps.ir.socialnetwork.recommendation.ml.PatternSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import org.ranksys.core.util.tuples.Tuple2id;

/**
 * Balances a dataset using the Synthetic Minority Over-Sampling Technique (SMOTE)
 * 
 * Chawla, N.V, Bowyer, K.W., Hall, L.O., Kegelmeyer, W.P. SMOTE: Synthetic Minority Over-sampling Technique. 
 * Journal of Artificial Intelligence Research 16 (2002),pp. 321-357.
 * 
 * @author Javier Sanz-Cruzado Puig
 */
public class SMOTEBalancer implements Balancer 
{
    
    /**
     * Number of neighbours.
     */
    private final int k;
    /**
     * Constructor
     * @param k Number of neighbours.
     */
    public SMOTEBalancer(int k)
    {
        this.k = k;
    }
    
    @Override
    public PatternSet balance(PatternSet original) 
    {
        Map<String, List<Pattern>> classpatterns = new HashMap<>();
        List<String> classes = original.getClasses();
        Set<String> categories = new HashSet<>(classes);
        classes.forEach(clase -> classpatterns.put(clase, new ArrayList<>()));
        
        original.getPatterns().forEach(pat -> classpatterns.get(classes.get(pat.getCategory())).add(pat));

        // Compute the maximum number of patterns.
        Integer max = Integer.MIN_VALUE;
        for(String cl : classes)
        {
            int listsize = classpatterns.get(cl).size();
            if(listsize > max)
            {
                max = listsize;
            }
        }
        
        
        int numAttr = original.getNumAttrs();
        List<String> attrNames = new ArrayList<>();
        List<AttrType> types = new ArrayList<>();
        
        original.getAttributes().stream().forEach(pair -> 
        {
            attrNames.add(pair.v1()); 
            types.add(pair.v2());
        });
        
        Int2ObjectMap<Object2DoubleMap<String>> dictionaries = original.getDictionaries();
        
        List<List<Double>> patterns = new ArrayList<>();
        List<String> patClass = new ArrayList<>();
        for(String cl : classes)
        {
            // First, we store the already existent patterns
            classpatterns.get(cl).forEach(pat -> {
                patterns.add(pat.getValues());
                patClass.add(cl);
            });
            
            // Second, we generate the extra patterns
            if(classpatterns.get(cl).size() < max)
            {
                int numNewPatterns = max - classpatterns.get(cl).size();
                List<List<Double>> newPatterns = this.generateNewPatterns(numNewPatterns, this.k, classpatterns.get(cl), types);
                patterns.addAll(newPatterns);
                
                for(int i = 0; i < newPatterns.size(); ++i)
                {
                    patClass.add(cl);
                }
            }
            
        }
        
        return new PatternSet(numAttr, attrNames, types, categories, dictionaries, patterns, patClass);
    }
    
    
    /**
     * Finds a list containing the extra patterns
     * @param numNewPatterns Number of new patterns
     * @param k Number of the nearest neighbours
     * @param minPatterns Minoritary class patterns
     * @param types Attribute types for each pattern
     * @return the list with synthetized patterns
     */
    private List<List<Double>> generateNewPatterns(int numNewPatterns, int k, List<Pattern> minPatterns, List<AttrType> types)
    {
        Random r = new Random();
        List<List<Double>> newpatterns = new ArrayList<>();
        
        if(numNewPatterns <= 0) //If no new set of patterns has to be computed.
            return newpatterns;
        
        double[][] distances = new double[minPatterns.size()][minPatterns.size()];
        
        for(int i = 0; i < minPatterns.size(); ++i)
        {
            for(int j = 0; j <= i; ++j)
            {
                distances[i][j] = this.distance(minPatterns.get(i), minPatterns.get(j), types);
                distances[j][i] = distances[i][j];
            }
        }
    
        double percent = (numNewPatterns + 0.0) / (minPatterns.size() + 0.0);
        int numExtra = new Double(Math.ceil(percent)).intValue();
        for(int i = 0; i < minPatterns.size(); ++i)
        {
            if(percent < 1.0)
            {
                if(r.nextDouble() >= percent)
                {
                    continue;
                }
            }
            
            // Compute the neighborhood
            Queue<Tuple2id> queue = new PriorityQueue<>(k, (Tuple2id a, Tuple2id b) -> {
                if(a.v2 < b.v2)
                    return -1;
                else if(a.v2 > b.v2)
                    return 1;
                else
                    return 0;
            });
            
            for(int j = 0; j < minPatterns.size(); ++j)
            {
                if(i != j)
                    queue.add(new Tuple2id(j, distances[i][j]));
            }
            
            int n = 0;
            List<Pattern> neighbourhood = new ArrayList<>();
            while(n < k && !queue.isEmpty())
            {
                neighbourhood.add(minPatterns.get(queue.poll().v1));
                ++n;
            }
            
            List<List<Double>> extra = this.populate(numExtra, minPatterns.get(i), neighbourhood, types);
            newpatterns.addAll(extra);
        }
        return newpatterns;
    }
      
    /**
     * Given a pattern and its neighbour, generates a new pattern.
     * @param numExtra Number of extra examples to compute.
     * @param p the pattern
     * @param neighbourhood the neighbourhood of the pattern
     * @param types types of the attributes.
     * @return the number of attributes.
     */
    private List<List<Double>> populate(int numExtra, Pattern p, List<Pattern> neighbourhood, List<AttrType> types)
    {
        Random r = new Random();
        
        List<List<Double>> newpatterns = new ArrayList<>();
        for(int i = numExtra; i > 0; --i)
        {
            int selection = r.nextInt(neighbourhood.size());
            Pattern q = neighbourhood.get(selection);
            
            List<Double> list = new ArrayList<>();
            for(int j = 0; j < types.size(); ++j)
            {
                double variable;
                if(types.get(j) == CONTINUOUS)
                {
                    double dif = q.getValue(j) - p.getValue(j);
                    double gap = r.nextDouble();
                    variable = p.getValue(j) + gap*dif;
                }
                else
                {
                    variable = (r.nextDouble() > 0.5 ? p.getValue(j) : q.getValue(j));
                }
                
                list.add(variable);
            }
            newpatterns.add(list);
        }
        
        return newpatterns;
    }
    
    /**
     * Computes the distance between two patterns. It is computed using the euclidean
     * distance. In case the attribute is nominal, it is considered that two different
     * values are at distance equal to 1.
     * @param p1 first pattern
     * @param p2 second pattern
     * @param types types of the attributes
     * @return the distance.
     */
    private double distance(Pattern p1, Pattern p2, List<AttrType> types)
    {
        double distance = 0.0;
                
        for(int i = 0; i < types.size(); ++i)
        {
            if(types.get(i) == CONTINUOUS)
            {
                distance += Math.pow(p1.getValue(i) - p2.getValue(i),2.0);
            }
            else // if (types.get(i) == NOMINAL)
            {
                distance += (p1.getValue(i) == p2.getValue(i) ? 0.0 : 1.0);
            }
        }
        
        return Math.sqrt(distance);
    }

    
}
