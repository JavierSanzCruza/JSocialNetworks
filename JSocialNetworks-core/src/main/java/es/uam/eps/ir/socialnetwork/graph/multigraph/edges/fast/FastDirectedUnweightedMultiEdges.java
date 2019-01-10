/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.multigraph.edges.fast;

import es.uam.eps.ir.socialnetwork.graph.multigraph.edges.DirectedMultiEdges;
import es.uam.eps.ir.socialnetwork.graph.multigraph.edges.MultiEdgeTypes;
import es.uam.eps.ir.socialnetwork.graph.multigraph.edges.MultiEdgeWeights;
import es.uam.eps.ir.socialnetwork.graph.multigraph.edges.UnweightedMultiEdges;
import es.uam.eps.ir.socialnetwork.index.FastWeightedAutoRelation;

import java.util.ArrayList;
import java.util.List;

import java.util.stream.Stream;

/**
 * Fast implementation of directed unweighted edges for multigraphs.
 * @author Javier Sanz-Cruzado Puig
 */
public class FastDirectedUnweightedMultiEdges extends FastMultiEdges implements DirectedMultiEdges, UnweightedMultiEdges
{
    /**
     * Constructor.
     */
    public FastDirectedUnweightedMultiEdges()
    {
        super(new FastWeightedAutoRelation<>(), new FastWeightedAutoRelation<>());
    }

    @Override
    public Stream<Integer> getIncidentNodes(int node)
    {
        List<Integer> incidentNodes = new ArrayList<>();
        
        return this.types.getIdsFirst(node).map(weight -> weight.getIdx());
    }

    @Override
    public Stream<Integer> getAdjacentNodes(int node)
    {
        return this.types.getIdsSecond(node).map(weight -> weight.getIdx());

    }

    @Override
    public Stream<MultiEdgeTypes> getIncidentTypes(int node)
    {
        return this.types.getIdsFirst(node).map(type -> new MultiEdgeTypes(type.getIdx(), type.getValue()));
    }

    @Override
    public Stream<MultiEdgeTypes> getAdjacentTypes(int node)
    {
        return this.types.getIdsSecond(node).map(type -> new MultiEdgeTypes(type.getIdx(), type.getValue()));
    }

    @Override
    public Stream<MultiEdgeWeights> getNeighbourWeight(int node)
    {
        throw new UnsupportedOperationException("Invalid Operation");
    }

    @Override
    public boolean addEdge(int orig, int dest, double weight, int type)
    {
       boolean failed = true;
       if(this.weights.containsPair(orig, dest))
       {
           List<Double> weightList = this.weights.getValue(orig, dest);
           weightList.add(MultiEdgeWeights.getDefaultValue());
           
           List<Integer> typeList = this.types.getValue(orig, dest);
           typeList.add(type);
           
           failed &= this.weights.updatePair(orig, dest, weightList) & this.types.updatePair(orig, dest, typeList);
       }
       else
       {
           List<Double> weightList = new ArrayList<>();
           weightList.add(MultiEdgeWeights.getDefaultValue());
           
           List<Integer> typeList = new ArrayList<>();
           typeList.add(type);
           
           failed &= this.weights.addRelation(orig, dest, weightList) & this.types.addRelation(orig, dest, typeList);
       }
       
       if(failed)
           this.numEdges++;
       return failed;
       
    }   
}
