/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.multigraph.edges;

import java.util.List;
import java.util.stream.Stream;

/**
 * Interface for unweighted edges
 * @author Javier Sanz-Cruzado Puig
 */
public interface UnweightedMultiEdges extends MultiEdges
{
    @Override
    public default List<Double> getEdgeWeights(int orig, int dest)
    {
        if(this.containsEdge(orig, dest))
            return MultiEdgeWeights.getDefaultValue(this.getNumEdges(orig, dest));
        else
            return MultiEdgeWeights.getErrorType();
    }
    
    @Override
    public default Stream<MultiEdgeWeights> getIncidentWeight(int node)
    {
        return this.getIncidentNodes(node).map(val -> new MultiEdgeWeights(val, MultiEdgeWeights.getDefaultValue(this.getNumEdges(val, node))));
    }
    @Override
    public default Stream<MultiEdgeWeights> getAdjacentWeight(int node)
    {
        return this.getAdjacentNodes(node).map((val) -> new MultiEdgeWeights(val, MultiEdgeWeights.getDefaultValue(this.getNumEdges(node, val))));
    }
    
    @Override
    public default Stream<MultiEdgeWeights> getNeighbourWeight(int node)
    {
        return this.getNeighbourNodes(node).map((val) -> new MultiEdgeWeights(val, MultiEdgeWeights.getDefaultValue(this.getNumEdges(node, val))));
    }

}
