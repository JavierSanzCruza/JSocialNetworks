/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.linkprediction.patterns;

import es.uam.eps.ir.socialnetwork.recommendation.ml.attributes.AttrType;
import es.uam.eps.ir.socialnetwork.recommendation.ml.Pattern;
import es.uam.eps.ir.socialnetwork.recommendation.ml.PatternSet;
import es.uam.eps.ir.socialnetwork.recommendation.ml.classbalancer.Balancer;
import es.uam.eps.ir.socialnetwork.recommendation.ml.classbalancer.SMOTEBalancer;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Class that generates new patterns using the SMOTE method.
 * @author Javier Sanz-Cruzado Puig
 */
public class SMOTEPatternGenerator 
{
    /**
     * Program for generating a new dataset using SMOTE
     * @param args Execution arguments
     * <ul>
     *  <li><b>Input file:</b> File containing the original set of patterns</li>
     *  <li><b>Output file:</b> File to store the new set of patterns</li>
     * </ul>
     * @throws IOException If something fails while reading or writing the patterns.
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 2)
        {
            System.err.println("Usage: <inputFile> <outputFile>");
            return;
        }
        
        long a = System.currentTimeMillis();
        String input = args[0];
        String output = args[1];
        
        PatternSet ps = PatternSet.read(input, "\t", true);
        
        long b = System.currentTimeMillis();
        System.out.println("Patterns read (" + (b-a) + " ms.)");
        Balancer balancer = new SMOTEBalancer(10);
        PatternSet smoted = balancer.balance(ps);
        
        b = System.currentTimeMillis();
        System.out.println("SMOTE finished (" + (b-a) + " ms.)");
        // Write the new pattern set
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            // Write the header
            List<Tuple2oo<String, AttrType>> attributes = smoted.getAttributes();
            Int2ObjectMap<Object2DoubleMap<String>> dictionaries = smoted.getDictionaries();
            List<String> classes = smoted.getClasses();
            for(int i = 0; i < attributes.size(); ++i)
            {
                bw.write(attributes.get(i).v1() + "\t");
            }
            bw.write("class\n");
            
            for(int i = 0; i < attributes.size(); ++i)
            {
                bw.write(attributes.get(i).v2() + "\t");
            }
            bw.write("class\n");
            
            // Write each one of the patterns
            for(int i = 0; i < smoted.getNumPatterns(); ++i)
            {
                Pattern p = smoted.getPattern(i);
                for(int j = 0; j < attributes.size(); ++j)
                {
                    if(attributes.get(j).v2() == AttrType.CONTINUOUS)
                        bw.write(p.getValue(j) + "\t");
                    else // if (attributes.get(j).getSecond() == AttrType.NOMINAL)
                    {
                        bw.write(smoted.getNominalAttributeStringValue(j, p.getValue(j)) + "\t");
                    }
                }
                bw.write(classes.get(p.getCategory())+"\n");
            }
        }
        
        b = System.currentTimeMillis();
        System.out.println("Patterns written (" + (b-a) + " ms.)");
    }
    
    
}
