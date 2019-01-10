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
import java.util.stream.Collectors;

/**
 * Class that performs a random partition of a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the nodes of the graph.
 */ 
public class RandomCountedPartition<V> extends AbstractPartition<V>
{
    /**
     * Percentage of edges that will go to train (approximately).
     */
    private int count;

    
    /**
     * Constructor.
     * @param count Number of edges for each user that go into test set.
     */
    public RandomCountedPartition(int count)
    {
        this.count = count;
    }

    @Override
    public boolean doPartition(Graph<V> graph) 
    {
        try {
            if(count < 0)
            {
                System.err.println("Invalid number: " + count);
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
                    List<V> adj = graph.getAdjacentNodes(node).collect(Collectors.toCollection(ArrayList::new));
                    if(adj.size() > count)
                    {
                        for(int i = 0; i < count; ++i)
                        {
                            int next = rng.nextInt(adj.size());
                            this.test.addEdge(node, adj.get(next), graph.getEdgeWeight(node, adj.get(next)));
                            adj.remove(next);
                        }
                        
                        for(int i = 0; i < adj.size(); ++i)
                        {
                            this.train.addEdge(node, adj.get(i), graph.getEdgeWeight(node, adj.get(i)));
                        }
                    }
                    else
                    {

                        for(int i = 0; i < adj.size(); ++i)
                        {
                            this.test.addEdge(node, adj.get(i), graph.getEdgeWeight(node, adj.get(i)));
                        }
                    }
                });
            }
            
            return true;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return false;
        }
    }
    
    /**
     * Gets the number of selected edges for the test set.
     * @return The current number of selected edges for the test set.
     */
    public double getCount() 
    {
        return count;
    }

    /**
     * Sets the number of selected edges for the test set.
     * @param count The new number of selected edges for the test set.
     */
    public void setCount(int count) 
    {
        this.count = count;
    }
}
