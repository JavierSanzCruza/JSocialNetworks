/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.multigraph;

import es.uam.eps.ir.socialnetwork.graph.Weight;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import java.util.stream.Stream;

/**
 * Interface for undirected weighted multigraphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the vertices
 */
public interface UndirectedWeightedMultiGraph<V> extends WeightedMultiGraph<V>, UndirectedMultiGraph<V>
{   

    @Override
    public default Stream<Weight<V,Double>> getIncidentNodesWeights(V node)
    {
        return getNeighbourNodesWeights(node);
    }

    @Override
    public default Stream<Weight<V,Double>> getAdjacentNodesWeights(V node)
    {
        return getNeighbourNodesWeights(node);
    }

    @Override
    public default Stream<Weight<V,Double>> getNeighbourNodesWeights(V node)
    {
        return this.getNeighbourNodes(node).map((neigh)->new Weight<>(neigh, this.getEdgeWeight(node, neigh)));
    }

    @Override
    public default Stream<Weight<V,Double>> getNeighbourhoodWeights(V node, EdgeOrientation direction)
    {
        return getNeighbourNodesWeights(node);
    }
    
    @Override
    public default Stream<Weights<V,Double>> getIncidentNodesWeightsLists(V node)
    {
        return getNeighbourNodesWeightsLists(node);
    }

    @Override
    public default Stream<Weights<V,Double>> getAdjacentNodesWeightsLists(V node)
    {
        return getNeighbourNodesWeightsLists(node);
    }

    /**
     * Gets the different weights for the edges of the neighbour nodes.
     * @param node The node to study
     * @return A stream containing the weights
     */
    @Override
    public default Stream<Weights<V, Double>> getNeighbourNodesWeightsLists(V node)
    {
        return this.getNeighbourNodes(node).map((inc)-> new Weights<>(inc, this.getEdgeWeights(node,inc)));
        
    }

    @Override
    public default Stream<Weights<V,Double>> getNeighbourhoodWeightsLists(V node, EdgeOrientation direction)
    {
        return getNeighbourNodesWeightsLists(node);
    }
}
