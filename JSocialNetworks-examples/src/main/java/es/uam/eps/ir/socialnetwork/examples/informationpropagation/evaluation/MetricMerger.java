/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.informationpropagation.evaluation;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Combines a set of metric files obtained from a simulation in an only file.
 * The format of the original file is the following for each line (apart from
 * the header):
 *
 * Iteration \t Metric1 \t Metric2 \t Metric3 \t .... \t MetricN
 *
 * The corresponding output file is formatted in the following way:
 *
 * Iteration \t Metric1_avg \t Metric1_var \t Metric2_avg \t Metric2_var \t Metric3_avg \t Metric3_var \t .... \t MetricN_avg \t MetricN_var \t NumSim
 *
 * where NumSim is the number of simulations which has arrived to that number of
 * iterations.
 *
 * @author Javier Sanz-Cruzado Puig
 */
public class MetricMerger
{

    /**
     * Main method. Combines a set of metric files obtained from a simulation in an only file.
     * It computes the average and the variance of the different metrics and values.
     *
     * @param args The necessary arguments for executing the program: <ul>
     * <li>Folder that contains the simulation files</li>
     * <li>Folder that contains the joint files</li>
     * </ul>
     */
    public static void main(String[] args)
    {
        if (args.length < 2)
        {
            System.err.println("Invalid arguments");
            System.err.println("Usage: <Simulation files route>");
            return;
        }

        List<Integer> counter = new ArrayList<>();
        Map<String, List<Double>> metricMap = new HashMap<>();
        List<String> metricList = new ArrayList<>();
        
        // Maximum number of detected iterations in a simulation.
        int currentMax = -1;
        String route = args[0];
        String outputFile = args[1];

        File dir = new File(route);
        String[] files = dir.list();
        if (dir.isDirectory() == false || files == null)
        {
            System.err.println("Invalid route");
            System.err.println("Usage: <Simulation files route> <Output file>");
            return;
        }

        Map<String, List<String>> fileNames = new HashMap<>();
        for(String file : files)
        {
            String[] split = file.split("-");
            String root = split[0];
            for(int i = 1; i < split.length - 1; ++i)
            {
                root += "-" + split[i];
            }
            
            if(!fileNames.containsKey(root))
            {
                fileNames.put(root, new ArrayList<>());
            }
            fileNames.get(root).add(file);
        }
        
        for(String root : fileNames.keySet())
        {
            List<String> simulationFiles = fileNames.get(root);
            metricList.clear();
            metricMap.clear();
            currentMax = -1;
            counter.clear();
            
            boolean header = false;
            
            for(String simulation : simulationFiles)
            {
                // Read the file
                try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(route + simulation))))
                {
                    String line = br.readLine();
                    if (!header) //header must be read and variables have to be initialized.
                    {
                        String[] split = line.split("\t");
                        for (int i = 1; i < split.length; ++i)
                        {
                            metricList.add(split[i]);
                            metricMap.put(split[i], new ArrayList<>());
                        }
                        
                        header = true;
                    }

                    int i = 0;

                    // reading each iteration of the algorithm
                    while (line != null && !line.equals(""))
                    {
                        line = br.readLine();
                        if(line != null && !line.equals(""))
                        {
                            String[] split = line.split("\t");
                            for (int j = 1; j < split.length; ++j)
                            {

                                if (i > currentMax)
                                {
                                    double val = new Double(split[j]);
                                    metricMap.get(metricList.get(j-1)).add(val);
                                } 
                                else
                                {
                                    double val = new Double(split[j]);
                                    double auxValue = metricMap.get(metricList.get(j-1)).get(i);
                                    auxValue += val;
                                    metricMap.get(metricList.get(j-1)).set(i, auxValue);
                                }


                            }

                            if (i > currentMax)
                            {
                                counter.add(1);
                                currentMax = i;
                            }
                            else
                            {
                                counter.set(i, counter.get(i) + 1);
                            }
                            ++i;
                        }
                    }
                } 
                catch (IOException ioe)
                {
                    System.err.println("Something failed while reading metrics.");
                }
            
                System.out.println(simulation + " done");
            }
            
            // Once the metrics have been calculated, it's time to write them in an only file.
            try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile + root + ".txt"))))
            {
                // Writing the header
                bw.write("Iteration");
                for(int i = 0; i < metricList.size(); ++i)
                    bw.write("\t" + metricList.get(i) + "_avg");
                bw.write("\tNumSim\n");

                for(int i = 0; i <= currentMax; ++i)
                {
                    bw.write("" + i);
                    for(String metric : metricList)
                    {
                        double metricValue = metricMap.get(metric).get(i);
                        metricValue /= (double) counter.get(i);
                        bw.write("\t" + metricValue);

                    }
                    bw.write("\t" + counter.get(i) + "\n");
                }
            }
            catch(IOException ioe)
            {
                System.err.println("Something failed while writing the merged metrics");
                ioe.printStackTrace();

            }
            
            System.out.println("Simulations for " + root + " done.");
        }    
    }       
}