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
 * 
 * It also preprocesses the graphs for the problem we are trying to solve.
 * 
 * @author Javier Sanz-Cruzado Puig
 */
public class AntiRecommendationPartitionExample 
{
    public static void main(String args[])
    {
        if(args.length < 4)
        {
            System.err.println("Usage: <graph_route> <first_timestamp> <second_timestamp> <output_route>");
            return;
        }
        
        String inputRoute = args[0];
        double first = new Double(args[1]);
        double second = new Double(args[2]);
        String outputRoute = args[3];
        
        
        Graph<Long> original;
        TextGraphReader<Long> greader = new TextGraphReader<>(true, true, true, false, "\t", Parsers.lp);
        
        
        original = greader.read(inputRoute,true, false);
        
        // Step 1: Apply the first temporal partition
        Partition<Long> partition = new ThresholdWeightsPartition(second);
        partition.doPartition(original);
        
        Graph<Long> train = partition.getTrainGraph();
        Graph<Long> test = partition.getTestGraph();
        Graph<Long> validation = partition.getTrainGraph();
        
        train = AntiRecommendationPartitionExample.condenseGraph((MultiGraph<Long>) train);
        test = AntiRecommendationPartitionExample.condenseGraph((MultiGraph<Long>) test);
        
        // Write the graphs
        TextGraphWriter<Long> gwriter = new TextGraphWriter("\t");       
        gwriter.write(train, outputRoute + "-train.txt", true, false);
        gwriter.write(test, outputRoute + "-fulltest.txt", true, false);
        
        
        Graph<Long> testDLP = AntiRecommendationPartitionExample.graphSubstractDLP(train, test);
        Graph<Long> testLP = AntiRecommendationPartitionExample.graphSubstractLP(train, test);
        
        // Write the separate test graphs for Link Prediction and Disappearing Link Prediction
        gwriter.write(testDLP, outputRoute + "-test-dlp.txt", true, false);      
        gwriter.write(testLP, outputRoute + "-test-lp.txt", true, false);
        
        // Step 2: Apply the second temporal partition (Machine Learning sets).
        partition = new ThresholdWeightsPartition(first);
        partition.doPartition(validation);
        
        train = partition.getTrainGraph();
        validation = partition.getTestGraph();
        
        train = AntiRecommendationPartitionExample.condenseGraph((MultiGraph<Long>) train);
        validation = AntiRecommendationPartitionExample.condenseGraph((MultiGraph<Long>) validation);
        
        gwriter.write(train, outputRoute + "-train-patterns.txt", true, false);
        gwriter.write(validation, outputRoute + "-train-classes.txt", true, false);
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
            
            multigraph.getAllNodes().forEach(u -> 
            {  
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
    
    /**
     * Given the training and the test graphs, generates the test set for the link prediction problem.
     * @param train The training graph.
     * @param test The test graph.
     * @return the cleaned graph.
     */
    private static Graph<Long> graphSubstractLP(Graph<Long> train, Graph<Long> test)
    {
       try 
       {
            Graph<Long> finalGraph;
            
            GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
            gg.configure(train.isDirected(), false);
            finalGraph = gg.generate();

            train.getAllNodes().forEach(node -> 
            {
                finalGraph.addNode(node);
            });
            
            train.getAllNodes().forEach(u -> 
            {
                train.getAllNodes().forEach(v -> 
                {
                    // Select all nodes which do not contain the edge or its reciprocal.
                    if((!train.containsEdge(u, v) && !train.containsEdge(v,u)) && test.containsEdge(u, v))
                        finalGraph.addEdge(u, v);
                });
            });

            return finalGraph;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return null;
        }
    }
    
    /**
     * Given the training and the test graphs, generates the test set for the disappearing link prediction problem.
     * @param train The training graph.
     * @param test The test graph.
     * @return the cleaned graph.
     */
    private static Graph<Long> graphSubstractDLP(Graph<Long> train, Graph<Long> test)
    {
        try
        {
            Graph<Long> finalGraph;
            GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
            gg.configure(train.isDirected(), false);
            finalGraph = gg.generate();
            
            train.getAllNodes().forEach(node -> {
                finalGraph.addNode(node);
            });
            
            train.getAllNodes().forEach(u -> {
                train.getAdjacentNodes(u).forEach(v -> {
                   if(!test.containsEdge(u,v))
                       finalGraph.addEdge(u, v);
                });
            });
            
            return finalGraph;
        }
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex)
        {
            return null;
        }
    }
    
    
    /**
     * Combines two graphs.
     * @param first the original graph.
     * @param secondTrain Common links.
     * @param secondTest New links.
     * @return the combined graph.
     */
    private static Graph<Long> combineCondensedGraphs(Graph<Long> first, Graph<Long> secondTrain, Graph<Long> secondTest)
    {
        try {
            Graph<Long> finalGraph;
            
            GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
            gg.configure(first.isDirected(), true);
            finalGraph = gg.generate();
            
            // Add all the nodes.
            first.getAllNodes().forEach(node -> 
            {
                finalGraph.addNode(node);
            });
            
            secondTest.getAllNodes().forEach(node -> {
                finalGraph.addNode(node);
            });
            
            // Add the edges in the first graph
            first.getAllNodes().forEach(u -> {
                Map<Long, Double> map = new HashMap<>();
                first.getAdjacentNodesWeights(u).forEach(weight -> {
                    Long v = weight.getIdx();
                    if(secondTrain.containsEdge(u, weight.getIdx()))
                    {
                        map.put(v, weight.getValue() + secondTrain.getEdgeWeight(u, v));
                    }
                    else
                    {
                        map.put(v, weight.getValue());
                    }
                });
                
                map.entrySet().forEach(entry -> 
                {
                    finalGraph.addEdge(u, entry.getKey(), entry.getValue());
                });
            });
            
            // Add the new edges
            secondTest.getAllNodes().forEach(u -> {
                secondTest.getAdjacentNodesWeights(u).forEach(w -> {
                    finalGraph.addEdge(u, w.getIdx(), w.getValue());
                });
            });
            
            return finalGraph;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return null;
        }
    }
    
}
