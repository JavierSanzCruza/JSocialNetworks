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
import java.util.HashMap;
import java.util.Map;
import org.ranksys.formats.parsing.Parsers;

/**
 * Applies a partitioning of a graph for the problem of recommending disappearing links.
 * @author Javier Sanz-Cruzado Puig
 */
public class AntiRecommendationFollowsPartitionExample 
{
    public static void main(String args[]) throws GeneratorNotConfiguredException, GeneratorBadConfiguredException
    {
        if(args.length < 4)
        {
            System.err.println("Usage: <train_graph> <validation_graph> <test_graph> <output_route>");
            return;
        }
        
        String trainRoute = args[0];
        String validationRoute = args[1];
        String testRoute = args[2];
        String outputRoute = args[3];
        
        
        Graph<Long> trainGraph;
        Graph<Long> validationGraph;
        Graph<Long> testGraph;
        
        TextGraphReader<Long> greader = new TextGraphReader<>(true, true, true, false, "\t", Parsers.lp);

        
        trainGraph = greader.read(trainRoute, true, false);
        validationGraph = greader.read(validationRoute, true, false);
        testGraph = greader.read(testRoute, true, false);
        
        TextGraphWriter<Long> gwriter = new TextGraphWriter<>("\t");
        gwriter.write(trainGraph, outputRoute + "-train-patterns.txt");
        
        GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
        gg.configure(true, true);
        Graph<Long> defTrainGraph = gg.generate();
        
        // We remove those nodes which disappear from the network.
        validationGraph.getAllNodes().forEach(u -> 
        {
            if(testGraph.containsVertex(u))
            {
                defTrainGraph.addNode(u);
                validationGraph.getAdjacentNodesWeights(u).forEach(weight -> 
                {
                    Long v = weight.getIdx();
                    if(testGraph.containsVertex(v))
                    {
                        defTrainGraph.addEdge(u, v, weight.getValue(), 0, true);
                    }
                });
            }
        });
        
        gwriter.write(defTrainGraph,  outputRoute + "-train.txt");
        gwriter.write(defTrainGraph, outputRoute + "-train-classes.txt");
        gwriter.write(testGraph, outputRoute + "-test.txt");
        
        gg.configure(true,true);
        Graph<Long> testLP = gg.generate();
        Graph<Long> testDLP = gg.generate();
        
        defTrainGraph.getAllNodes().forEach(u -> {
            defTrainGraph.getAdjacentNodes(u).forEach(v -> {
                if(!testGraph.containsEdge(u,v))
                {
                    testDLP.addEdge(u, v);
                }
            });
        });
        
        gwriter.write(testDLP, outputRoute + "-test-dlp.txt");
        
        testGraph.getAllNodes().forEach(u -> {
            testGraph.getAdjacentNodes(u).forEach(v -> {
                if(!defTrainGraph.containsEdge(u, v))
                {
                    if(defTrainGraph.containsVertex(u) && defTrainGraph.containsVertex(v) && !defTrainGraph.containsEdge(v,u))
                        testLP.addEdge(u,v);
                }
            });
        });
        
        gwriter.write(testLP, outputRoute + "-test-lp");
        
        
        
        
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
    
    /**
     * Given two graphs, obtains all the edges in the first that do not appear in the second.
     * @param first The first graph.
     * @param second The second graph (the graph to substract).
     * @return The first graph.
     */
    private static Graph<Long> graphSubstract(Graph<Long> first, Graph<Long> second)
    {
       try {
            Graph<Long> finalGraph;
            
            GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
            gg.configure(first.isDirected(), false);
            finalGraph = gg.generate();

            first.getAllNodes().forEach(node -> 
            {
                finalGraph.addNode(node);
            });
            
            first.getAllNodes().forEach(u -> 
            {
                first.getAdjacentNodes(u).forEach(v -> 
                {
                    if(!second.containsVertex(u) || !second.containsVertex(v) || !second.containsEdge(u, v))
                    {
                        finalGraph.addEdge(u,v);
                    }
                });
            });

            return finalGraph;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
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
