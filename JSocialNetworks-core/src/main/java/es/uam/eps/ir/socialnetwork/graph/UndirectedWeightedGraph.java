/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import java.util.stream.Stream;

/**
 * Interface for undirected weighted graphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the vertices
 */
public interface UndirectedWeightedGraph<V> extends WeightedGraph<V>, UndirectedGraph<V>
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
}
