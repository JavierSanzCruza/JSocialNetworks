/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import es.uam.eps.ir.socialnetwork.partition.Partition;
import es.uam.eps.ir.socialnetwork.partition.weights.ThresholdWeightsPartition;
import java.util.HashMap;
import java.util.Map;
import org.ranksys.formats.parsing.Parsers;

/**
 * Applies a partitioning of a graph for the problem of recommending disappearing links.
 * @author Javier Sanz-Cruzado Puig
 */
public class InteractionsPartitionExample 
{
    public static void main(String args[])
    {
        if(args.length < 3)
        {
            System.err.println("Usage: <graph_route> <timestamp> <output_route>");
            return;
        }
        
        String inputRoute = args[0];
        double timestamp = new Double(args[1]);
        String outputRoute = args[2];
        
        TextGraphReader<Long> greader = new TextGraphReader<>(true, true, true, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(inputRoute, true, false);
        
        
        Graph<Long> original;
        
        original = greader.read(inputRoute, true, false);
        
        // Step 1: Apply the first temporal partition
        Partition<Long> partition = new ThresholdWeightsPartition(timestamp);
        partition.doPartition(original);
        
        Graph<Long> train = partition.getTrainGraph();
        Graph<Long> test = partition.cleanAndRemoveTestReciprocal();
             
        train = InteractionsPartitionExample.condenseGraph((MultiGraph<Long>) train);
        test = InteractionsPartitionExample.condenseGraph((MultiGraph<Long>) test);
        
        TextGraphWriter<Long> gwriter = new TextGraphWriter("\t");
        gwriter.write(train, outputRoute + "train.txt", true, false);
        gwriter.write(test, outputRoute + "test.txt",true, false);
        
        
        
    }
    
    /**
     * Given a multigraph, condenses the graph into a new simple graph, containing the number of 
     * edges as a weight. It also removes autoloops.
     * @param multigraph the multigraph.
     * @return the multigraph.
     */
    private static Graph<Long> condenseGraph(MultiGraph<Long> multigraph)
    {
        try {
            Graph<Long> finalGraph;
            
            GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
            gg.configure(multigraph.isDirected(), true);
            finalGraph = gg.generate();
            
            multigraph.getAllNodes().forEach(node -> 
            {
                finalGraph.addNode(node);
            });
            
            multigraph.getAllNodes().forEach(u -> {
                
                Map<Long, Double> newWeights = new HashMap<>();
                
                // Compute the weights of the condensed graph.
                multigraph.getAdjacentNodesWeights(u).forEach(weight -> 
                {
                    Long v = weight.getIdx();
                    if(!u.equals(v)) // Remove autoloops
                    {
                        if(newWeights.containsKey(v))
                        {
                            newWeights.put(v, newWeights.get(v)+1.0);
                        }
                        else
                        {
                            newWeights.put(v, 1.0);
                        }
                    }
                });
                
                // Add the edges to the condensed graph.
                newWeights.entrySet().forEach(entry -> 
                {
                    finalGraph.addEdge(u, entry.getKey(), entry.getValue());
                });
            });

            return finalGraph;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return null;
        }
    }   
}
