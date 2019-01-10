/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.partition.SelectedEdgesPartition;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.ranksys.formats.parsing.Parsers;

/**
 * Partitioning example that finds the intersection between two graphs as train,
 * and the remaining edges of the selected graph as test.
 * @author Javier Sanz-Cruzado Puig
 */
public class EdgeSelectionPartitionExample 
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
            System.out.println("Usage: <Original Graph> <folder> <directed> <weighted> <training>");
            return;
        }
        
        boolean directed = args[2].equalsIgnoreCase("true");
        boolean weighted = args[3].equalsIgnoreCase("true");
        String graphRoute = args[0];
        String trainRoute = args[4];
        String outputRoute = args[1];
                
        // Read the training graph
        TextGraphReader<String> greader = new TextGraphReader<>(false, directed, weighted, false, "\t", Parsers.sp);
        Graph<String> graph = greader.read(graphRoute, weighted, false);
      
        Graph<String> training = greader.read(trainRoute, weighted,false);
        
        // Step 2: Generate the partition
        Partition<String> part = new SelectedEdgesPartition<>(training);
        if(part.doPartition(graph))
        {
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputRoute + "train.txt"))))
            {
                Graph<String> sample = part.getTrainGraph();
                if(directed)
                {
                    sample.getAllNodes().forEach(node -> {
                        sample.getAdjacentNodes(node).forEach(neigh ->{
                            try {
                                bw.write(node + "\t" + neigh + "\t" + (weighted ? sample.getEdgeWeight(node, neigh) : 1.0)+"\n");
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
                                    bw.write(node + "\t" + neigh + "\t" + (weighted ? sample.getEdgeWeight(node, neigh) : 1.0)+"\n");
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
            
            try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputRoute + "test.txt"))))
            {
                //Clean the test and print it
                Graph<String> sample = part.cleanAndRemoveTestReciprocal();

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
        }
    }
}
