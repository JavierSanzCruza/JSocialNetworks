/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.partition.random;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.partition.AbstractPartition;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class that performs a random partition of a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the nodes of the graph.
 */ 
public class RandomPartition<V> extends AbstractPartition<V>
{
    /**
     * Percentage of edges that will go to train (approximately).
     */
    private double percentage;

    
    /**
     * Constructor.
     * @param percentage Percentage of the edges that will belong to the train
     * set, and percentage of the edges that will belong to the test set.
     */
    public RandomPartition(double percentage)
    {
        this.percentage = percentage;
    }

    @Override
    public boolean doPartition(Graph<V> graph) 
    {
        try {
            if(percentage < 0.0 || percentage > 1.0)
            {
                System.err.println("Invalid percentage: " + percentage);
                return false;
            }
            
            boolean directed = graph.isDirected();
            boolean weighted = graph.isWeighted();
            
            EmptyGraphGenerator<V> gg = new EmptyGraphGenerator<>();
            gg.configure(directed, weighted);
            this.train = gg.generate();
            this.test = gg.generate();
                   
            graph.getAllNodes().forEach((node)->{
                this.train.addNode(node);
                this.test.addNode(node);
            });
            
            Random rng = new Random();
            if(directed) // Generate the random partition in case the original graph is directed.
            {
                graph.getAllNodes().forEach((node)->
                {
                    graph.getAdjacentNodes(node).forEach((adj)->
                    {
                        if(!adj.equals(node))
                        {

                            // With probability = percentage, add the edge to train
                            if(rng.nextDouble() < percentage)
                            {
                                this.train.addEdge(node, adj,graph.getEdgeWeight(node, adj));
                            }
                            else //With probability = 1 - percentage, add it to test.
                            {
                                this.test.addEdge(node, adj, graph.getEdgeWeight(node, adj));
                            }
                        }
                    });
                });
            }
            else // Generate the random partition in case the original graph is not directed
            {
                // List of already visited nodes (to prevent including the same edge
                // in train and test.
                final List<V> visited = new ArrayList<>();
                
                graph.getAllNodes().forEach((node)->
                {
                    graph.getAdjacentNodes(node).forEach((adj)->
                    {
                        if(!visited.contains(adj) && !node.equals(adj)) //If the user has not been visited yet
                        {
                            // With probability = percentage, add the edge to train
                            if(rng.nextDouble() < percentage)
                            {
                                this.train.addEdge(node, adj,graph.getEdgeWeight(node, adj));
                            }
                            else // With probability = 1 - percentage, add the edge to train
                            {
                                this.test.addEdge(node, adj, graph.getEdgeWeight(node, adj));
                            }
                        }
                    });
                    
                    visited.add(node);
                });
            }
            
            return true;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return false;
        }
    }
    
    /**
     * Gets the current percentage of edges in the train set.
     * @return The current percentage of edges in the train set.
     */
    public double getPercentage() 
    {
        return percentage;
    }

    /**
     * Sets the percentage of edges in the train set.
     * @param percentage The new percentage of edges in the train set.
     */
    public void setPercentage(double percentage) 
    {
        this.percentage = percentage;
    }
}
