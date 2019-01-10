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
 * Interface for directed graphs.
 * @author Javier Sanz-Cruzado Puig
 * @param <V> type of the vertices
 */
public interface DirectedGraph<V> extends Graph<V>
{
    @Override
    public default Stream<V> getNeighbourhood(V node, EdgeOrientation direction)
    {
        switch(direction)
        {
            case OUT:
                return this.getAdjacentNodes(node);
            case IN:
                return this.getIncidentNodes(node);
            case UND:
                return this.getNeighbourNodes(node);
            case MUTUAL:
                return this.getMutualNodes(node);
            default:
                return Stream.empty();
        }
    }
    
    @Override
    public default int getNeighbourhoodSize(V node, EdgeOrientation direction)
    {
        switch(direction)
        {
            case OUT:
                return this.getAdjacentNodesCount(node);
            case IN:
                return this.getIncidentNodesCount(node);
            case UND:
                return this.getNeighbourNodesCount(node);
            case MUTUAL:
                return this.getMutualNodesCount(node);
            default:
                return -1;
        }
    }
    
    @Override
    public default Stream<Weight<V,Double>> getNeighbourhoodWeights(V node, EdgeOrientation direction)
    {
        switch(direction)
        {
            case OUT:
                return this.getAdjacentNodesWeights(node);
            case IN:
                return this.getIncidentNodesWeights(node);
            case UND:
                return this.getNeighbourNodesWeights(node);
            case MUTUAL:
                return this.getMutualNodesWeights(node);
            default:
                return Stream.empty();
        }
    }
    @Override
    public default Stream<Weight<V,Integer>> getNeighbourhoodTypes(V node, EdgeOrientation direction)
    {
        switch(direction)
        {
            case OUT:
                return this.getAdjacentNodesTypes(node);
            case IN:
                return this.getIncidentNodesTypes(node);
            case UND:
                return this.getNeighbourNodesTypes(node);
            default:
                return Stream.empty();
        }
    }
    
    @Override
    public default int degree(V node)
    {
        if(this.containsVertex(node))
            return this.inDegree(node) + this.outDegree(node);
        return 0;
    }
    
    
    /**
     * Obtains the in-degree of a node.
     * @param node The node.
     * @return the in-degree of the node.
     */
    public default int inDegree(V node)
    {
        return this.containsVertex(node) ? this.getIncidentEdgesCount(node) : 0;
    }
    
    /**
     * Obtains the out-degree of a node.
     * @param node The node.
     * @return the out-degree of the node.
     */
    public default int outDegree(V node)
    {
        return this.containsVertex(node) ? this.getAdjacentEdgesCount(node) : 0;
    }
    
    @Override
    public default int degree(V node, EdgeOrientation orientation)
    {
        if(orientation.equals(EdgeOrientation.IN))
            return this.inDegree(node);
        else if(orientation.equals(EdgeOrientation.OUT))
            return this.outDegree(node);
        else
            return this.inDegree(node) + this.outDegree(node);
    }
    

    @Override
    public default int getNeighbourEdgesCount(V node)
    {
        return this.getIncidentEdgesCount(node) + this.getAdjacentEdgesCount(node);
    }
    
    @Override
    public default boolean isDirected()
    {
        return true;
    }
    
    
    
}
