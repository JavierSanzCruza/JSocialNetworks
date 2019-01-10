/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.partition.weights;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyMultiGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.GraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import es.uam.eps.ir.socialnetwork.partition.AbstractPartition;

/**
 * Class that performs a partition of the graph depending on the values of the weights.
 * All edges with a threshold smaller than a certain threshold will be in the training graph,
 and all edges with a threshold greater than the corresponding threshold will be in the test
 graph.
 
 This allows, as an example, temporal partitions (partitioning graphs with temporal marks
 as weights. Graphs will maintain the weights. In the case of multigraphs, a same edge can appear 
 in both training and test sets (with different threshold values).
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the nodes of the graph.
 */ 
public class ThresholdWeightsPartition<V> extends AbstractPartition<V>
{
    /**
     * Percentage of edges that will go to train (approximately).
     */
    private final double threshold;

    
    /**
     * Constructor.
     * @param threshold Partition value of the graph.
     */
    public ThresholdWeightsPartition(double threshold)
    {
        this.threshold = threshold;
    }

    @Override
    public boolean doPartition(Graph<V> graph) 
    {
        if(graph == null)
        {
            return false;
        }
        
        boolean directed = graph.isDirected();
        boolean weighted = graph.isWeighted();
        
        GraphGenerator<V> gg;
        if(graph.isMultigraph())
        {
            gg = new EmptyMultiGraphGenerator<>();
        }
        else
        {
            gg = new EmptyGraphGenerator<>();
        }
        gg.configure(directed, weighted);
        
        try 
        {
            this.train = gg.generate();
            this.test = gg.generate();
        } 
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return false;
        }
        
        // Add all nodes to the training and test graphs.
        graph.getAllNodes().forEach((node) -> 
        {
            this.train.addNode(node);
            this.test.addNode(node);
        });
        
        if(graph.isMultigraph())
        {
            MultiGraph<V> multi = (MultiGraph<V>) graph;
            multi.getAllNodes().forEach((u) -> 
            {
                multi.getAdjacentNodesWeightsLists(u).forEach(weight -> 
                {
                    for(double value : weight.getValue())
                    {
                        if(value <= this.threshold)
                        {
                            this.train.addEdge(u, weight.getIdx(), value);
                        }
                        else
                        {
                            this.test.addEdge(u, weight.getIdx(), value);
                        }
                    }
                });
            });
        }
        else
        {
            // Add the edges to the training and test graphs
            graph.getAllNodes().forEach((u) -> 
            {
                graph.getAdjacentNodesWeights(u).forEach((weight) -> 
                {
                    if(weight.getValue() <= this.threshold)
                    {
                        this.train.addEdge(u, weight.getIdx(), weight.getValue());
                    }
                    else
                    {
                        this.test.addEdge(u, weight.getIdx(), weight.getValue());
                    }
                });
            });
        }
        
        return true;
    }
}
