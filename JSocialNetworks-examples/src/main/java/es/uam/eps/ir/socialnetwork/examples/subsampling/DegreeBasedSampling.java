/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.subsampling;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.subsampling.Sampler;
import es.uam.eps.ir.socialnetwork.subsampling.degreebased.DegreeBasedSampler;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Shows how the subsampling module works. To do that, this module generates 
 * eleven subsamples by gradually varying the probability of selecting a node 
 * from uniform probability to degree proportional probability.
 * @author Javier Sanz-Cruzado Puig
 */
public class DegreeBasedSampling 
{
    /**
     * Starting from a graph, this method generates eleven subsamples by gradually
     * varying the probability of selecting a node from uniform probability to
     * degree proportional probability.
     * @param args Execution arguments. All of them are compulsory: 
     * <ol>
     *  <li><b>Original Graph:</b> A file containing the original graph</li>
     *  <li><b>Folder:</b> A folder in which to store the subsamples</li>
     *  <li><b>Directed:</b> true if the graph is directed, false if not</li>
     *  <li><b>Weighted:</b> true if the graph is weighted, false if not</li>
     *  <li><b>NumNodes:</b> The number of nodes in the subsample</li>
     * </ol>
     */
    public static void main(String[] args)
    {
        if(args.length < 5)
        {
            System.out.println("Usage: <Original Graph> <folder> <directed> <weighted> <numNodes>");
            return;
        }
        
        boolean directed = args[2].equalsIgnoreCase("true");
        boolean weighted = args[3].equalsIgnoreCase("false");
        int numNodes = new Integer(args[4]);
        
        Graph<String> graph = null;
        // Step 1: Read the graph
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]))))
        {
            EmptyGraphGenerator<String> gb = new EmptyGraphGenerator<>();
            gb.configure(directed, weighted);
            graph = gb.generate();
            
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                if(!graph.containsVertex(split[0]))
                    graph.addNode(split[0]);
                if(!graph.containsVertex(split[1]))
                    graph.addNode(split[1]);
                
                graph.addEdge(split[0], split[1], new Double(split[2]));
            }
            System.out.println("ENTER HERE");
        }
        catch(IOException ioe)
        {
            System.err.println("An error ocurred while reading the file: " + ioe.getLocalizedMessage());
            return;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            System.err.println("An error ocurred while creating the empty graph " + ex.getLocalizedMessage());
        }
        
        
        // Step 2: Generate the eleven samples
        for(double val = 0.0; val <= 1.0; val = val + 0.1)
        {
            Sampler s = new DegreeBasedSampler(val);
            Graph<String> sample = s.sample(graph, numNodes);
            
            if(sample == null)
            {
                System.err.println("An error ocurred while subsampling the graph");
                return;
            }
            
            System.out.println("Writing " + val);
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1] + "subsample" + val + ".txt"))))
            {
                
                if(directed)
                {
                    sample.getAllNodes().forEach(node -> {
                        sample.getAdjacentNodes(node).forEach(neigh ->{
                            try {
                                bw.write(node + "\t" + neigh + "\t" + sample.getEdgeWeight(node, neigh)+"\n");
                            } catch (IOException ex) {
                                System.err.println("An error ocurred while writing the file: " + ex.getLocalizedMessage());
                            }
                        });
                    });
                }
                else
                {
                    final List<String> visited = new ArrayList<>();
                    sample.getAllNodes().forEach(node -> {
                        sample.getAdjacentNodes(node).forEach(neigh ->{
                            try {
                                if(!visited.contains(neigh)) //If the corresponding link has already been added.
                                    bw.write(node + "\t" + neigh + "\t" + sample.getEdgeWeight(node, neigh)+"\n");
                            } catch (IOException ex) {
                                System.err.println("An error ocurred while writing the file: " + ex.getLocalizedMessage());
                            }
                            visited.add(node);
                        });
                    });
                }
            }
            catch(IOException ioe)
            {
                System.err.println("An error ocurred while writing the file: " + ioe.getLocalizedMessage());
            }
            
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1] + "subsample" + val + ".csv"))))
            {
                
                if(directed)
                {
                    sample.getAllNodes().forEach(node -> {
                        sample.getAdjacentNodes(node).forEach(neigh ->{
                            try {
                                bw.write(node + ";" + neigh +"\n");
                            } catch (IOException ex) {
                                System.err.println("An error ocurred while writing the file: " + ex.getLocalizedMessage());
                            }
                        });
                    });
                }
                else
                {
                    final List<String> visited = new ArrayList<>();
                    sample.getAllNodes().forEach(node -> {
                        sample.getAdjacentNodes(node).forEach(neigh ->{
                            try {
                                if(!visited.contains(neigh)) //If the corresponding link has already been added.
                                    bw.write(node + ";" + neigh +"\n");
                            } catch (IOException ex) {
                                System.err.println("An error ocurred while writing the file: " + ex.getLocalizedMessage());
                            }
                            visited.add(node);
                        });
                    });
                }
            }
            catch(IOException ioe)
            {
                System.err.println("An error ocurred while writing the file: " + ioe.getLocalizedMessage());
            }
        }
    }
}
