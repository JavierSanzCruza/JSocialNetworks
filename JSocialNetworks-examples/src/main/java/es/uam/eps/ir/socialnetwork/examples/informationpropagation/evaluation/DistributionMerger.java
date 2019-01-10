/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.informationpropagation.evaluation;

import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Combines a set of distribution files obtained from simulation in a single, individual file
 * @author Javier Sanz-Cruzado Puig
 * 
 * The format of the original file is the following for each line:
 * 
 * param1 \t param2 \t param3 \t ... \t paramN \t value
 * 
 * where paramX represents the parameter value for the element X in the distribution,
 * and value represents the value of the distribution for those parameters.
 * 
 * @author Javier Sanz-Cruzado Puig
 */
public class DistributionMerger 
{
    public static void main(String[] args) throws IOException
    {
        if(args.length < 2)
        {
            System.err.println("Error: Bad arguments");
            System.err.println("Usage: <folder> <outputFile>");
            return;
        }
        
        String directory = args[0];
        String output = args[1];
        
        File f = new File(directory);
        String[] list = f.list();
        if(!f.exists() || !f.isDirectory() || list.length == 0)
        {
            System.err.println("Error: Nothing to merge!");
        }
        
        Object2IntMap<String> counter = new Object2IntOpenHashMap<>();
        counter.defaultReturnValue(0);
        
        Object2DoubleMap<String> values = new Object2DoubleOpenHashMap<>();
        values.defaultReturnValue(0.0);
        
        // Combine the different distributions into one
        for(String file : list)
        {
            // Read the individual file
            try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(directory + file))))
            {
                String line;
                while((line = br.readLine()) != null)
                {
                    // Generate a parameter, by combining the different possible values, using all the parameters as a String
                    String[] split = line.split("\t");
                    String param = split[0];
                    for(int i = 1; i < split.length - 1; ++i)
                    {
                        param += "\t" + split[i];
                    }
                    
                    int currentCount = counter.get(param);
                    counter.put(param, currentCount+1);
                    
                    double val = new Double(split[split.length-1]);
                    val += values.get(param);
                    values.put(param, val);
                    
                }
            }
        }
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            for(String param : values.keySet())
            {
                double value = values.get(param) / (counter.get(param) + 0.0);
                bw.write(param + "\t" + value);
            }
        }
    }
}
