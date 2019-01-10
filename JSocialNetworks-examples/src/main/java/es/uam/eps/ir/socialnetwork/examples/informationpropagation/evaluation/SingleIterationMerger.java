/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.informationpropagation.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ranksys.formats.parsing.Parsers;

/**
 * Given a set of evaluation files, combines them all in a single file, 
 * containing the metric values for a single iteration. It is assumed that all files
 * have the same distribution of metrics.
 * @author Javier Sanz-Cruzado Puig
 */
public class SingleIterationMerger 
{
    /**
     * Given a set of evaluation files, combines them all in a single file,
     * showing the metric values for a single iteration.
     * @param args Execution arguments:
     * <ol>
     *  <li><b>Directory:</b> Location of the metric files.</li>
     *  <li><b>Output:</b> Output file</li>
     *  <li><b>Iter:</b> Number of the iteration</li>
     * </ol>
     * @throws IOException if something fails while reading the metric files,
     * or writing the output file.
     */
    public static void main(String args[]) throws IOException
    {
        // Read execution arguments
        if(args.length < 3)
        {
            System.err.println("ERROR: Invalid arguments");
            System.err.println("Usage: <directory> <output> <iter>");
            return;
        }
        
        String input = args[0];
        String output = args[1];
        int iter = Parsers.ip.parse(args[2]);
        
        // Obtain the names of the files.
        File directory = new File(input);
        String[] files = directory.list();
        if(!directory.isDirectory() || files == null)
        {
            System.err.println("Nothing to merge! Select a proper directory.");
        }
        
        // Initialize the list of metrics.
        List<String> metrics = new ArrayList<>();
        boolean header = false;
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            // Order the names of the files alphabetically.
            List<String> fileList = Arrays.asList(files);
            Collections.sort(fileList);
            
            bw.write("Simulation");
            for(String file : files)
            {
                File aux = new File(input + file);
                if(aux.isDirectory()) // Ignore directories
                {
                    continue;
                }
                
                try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(input + file))))
                {
                    List<String> currentFields = new ArrayList<>();
                    Map<String, Double> iterationValues = new HashMap<>();
                    String line = br.readLine();
                    if(!header) // The header has to be read, in order to store the metrics we will write in the file.
                    {
                        String[] fields = line.split("\t");
                        
                        for(int i = 1; i < fields.length; ++i)
                        {
                            metrics.add(fields[i]);
                            currentFields.add(fields[i]);
                            bw.write("\t" + fields[i]);
                        }
                        bw.write("\n");
                        header = true;
                    }
                    else // Read the header to establish the order of the metrics in this file
                    {
                        String[] fields = line.split("\t");
                        for(int i = 1; i < fields.length; ++i)
                        {
                            currentFields.add(fields[i]);
                        }
                    }
                    
                    // Skip until the desired iteration is found.               
                    for(int i = 0; i < iter; ++i)
                    {
                        line = br.readLine();
                    }
                    
                    if(line == null) // If the iteration does not exist...
                    {
                        System.err.println("ERROR: File " + file + " has no iteration " + iter);
                        continue;
                    }
                    
                    // Otherwise, store the different values.
                    String[] metricSplit = line.split("\t");
                    for(int i = 1; i < currentFields.size(); ++i)
                    {
                        iterationValues.put(currentFields.get(i-1), Parsers.dp.parse(metricSplit[i]));
                    }
                    
                    // Obtain the name (without extension) of the file.
                    String name = "";
                    String[] splitName = file.split("\\.");
                    for(int i = 0; i < splitName.length - 1; ++i)
                    {
                        if(i > 0)
                            name += ".";
                        name += splitName[i];
                    }

                    // Write the different values in the file.
                    bw.write(name);
                    
                    for(String metric : metrics)
                    {
                        bw.write("\t" + iterationValues.get(metric));
                    }
                    bw.write("\n");
                }
            }
        }
    }
}
