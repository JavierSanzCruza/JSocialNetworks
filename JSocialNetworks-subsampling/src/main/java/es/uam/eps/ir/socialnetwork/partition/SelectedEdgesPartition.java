/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.partition;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import java.util.function.Predicate;

/**
 * Class that performs a partition of a graph given a set of selected edges
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the nodes of the graph.
 */ 
public class SelectedEdgesPartition<V> extends AbstractPartition<V>
{
    /**
     * Percentage of edges that will go to train (approximately).
     */
    private final Graph<V> selectedEdges;

    /**
     * Constructor.
     * @param selectedEdges Graph that contains the edges to mantain in the training set.
     * Every other edge on the graph will go to test.
     */
    public SelectedEdgesPartition(Graph<V> selectedEdges)
    {
        this.selectedEdges = selectedEdges;
    }

    @Override
    public boolean doPartition(Graph<V> graph) 
    {
        try 
        {
            if(this.selectedEdges == null)
                return false;
            
            boolean directed = graph.isDirected();
            boolean weighted = graph.isWeighted();
            
            
            EmptyGraphGenerator<V> gg = new EmptyGraphGenerator<>();
            gg.configure(directed, weighted);
            this.train = gg.generate();
            this.test = gg.generate();
            
            graph.getAllNodes().forEach((node)->
            {
                this.train.addNode(node);
                this.test.addNode(node);
            });
            
            graph.getAllNodes().forEach((node) ->
            {
                graph.getAdjacentNodes(node).forEach((adj) -> {
                    if(this.selectedEdges.containsVertex(node) && this.selectedEdges.containsVertex(adj) && this.selectedEdges.containsEdge(node, adj))
                        this.train.addEdge(node, adj, graph.getEdgeWeight(node, adj));
                    else
                        this.test.addEdge(node, adj, graph.getEdgeWeight(node, adj));
                    
                });
            });
            
            return true;
        }
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) 
        {
            return false;
        }
    }
}
