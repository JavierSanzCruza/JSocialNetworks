/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.partitioning;

import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastDirectedWeightedGraph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphWriter;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
public class AntiRecommendationPartitionExample2 
{
    public static void main(String args[]) throws IOException
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
        
        Graph<Long> train = new FastDirectedWeightedGraph<>();
        train = Adapters.removeAutoloops(train);
        Graph<Long> test = new FastDirectedWeightedGraph<>();
        test = Adapters.removeAutoloops(test);
        Graph<Long> trainClasses = new FastDirectedWeightedGraph<>();
        Graph<Long> trainPatterns = new FastDirectedWeightedGraph<>();
        
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(inputRoute))))
        {
            String line;
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                Long u = Parsers.lp.parse(split[0]);
                Long v = Parsers.lp.parse(split[1]);
                Long time = Parsers.lp.parse(split[2]);
                
                if(time < second)
                {
                    train.addEdge(u, v, 1.0);
                    if(time > first)
                    {
                        trainClasses.addEdge(u,v,1.0);
                    }
                    else
                    {
                        trainPatterns.addEdge(u,v,1.0);
                    }
                }
                else
                {
                    test.addEdge(u,v,1.0);
                }
            }
        }
        
        TextGraphWriter<Long> gwriter = new TextGraphWriter<>("\t");
        gwriter.write(train, outputRoute + "-train.txt", true, false);
        gwriter.write(test, outputRoute + "-test.txt", true, false);
        gwriter.write(trainClasses, outputRoute + "-train-classes.txt", true, false);
        gwriter.write(trainPatterns, outputRoute + "-train-patterns.txt", true, false);
        
        Graph<Long> testDLP = AntiRecommendationPartitionExample2.graphSubstractDLP(train, test);
        Graph<Long> testLP = AntiRecommendationPartitionExample2.graphSubstractLP(train, test);
        testLP = AntiRecommendationPartitionExample2.removeReciprocal(train, testLP);
        
        gwriter.write(testDLP, outputRoute + "-test-dlp.txt", true, false);
        gwriter.write(testLP, outputRoute + "-test-lp.txt", true, false);
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

    private static Graph<Long> removeReciprocal(Graph<Long> train, Graph<Long> testLP) 
    {
        try {
            Graph<Long> finalGraph;
            
            GraphGenerator<Long> gg = new EmptyGraphGenerator<>();
            gg.configure(testLP.isDirected(), true);
            finalGraph = gg.generate();         
            
            // Add all the nodes.
            testLP.getAllNodes().forEach(node -> 
            {
                finalGraph.addNode(node);
            });
            
            testLP.getAllNodes().forEach(u -> {
                testLP.getAdjacentNodes(u).forEach(v -> {
                    if(!train.containsEdge(v,u))
                        finalGraph.addEdge(u,v);
                });
            });
            
            return finalGraph;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return null;
        }
    }
    
}
