/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.edges.fast;

import es.uam.eps.ir.socialnetwork.graph.edges.DirectedEdges;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeType;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeWeight;
import es.uam.eps.ir.socialnetwork.graph.edges.UnweightedEdges;
import es.uam.eps.ir.socialnetwork.index.FastUnweightedAutoRelation;
import es.uam.eps.ir.socialnetwork.index.FastWeightedAutoRelation;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Stream;
import org.ranksys.core.preference.fast.IdxPref;

/**
 * Fast implementation of directed unweighted edges.
 * @author Javier Sanz-Cruzado Puig
 */
public class FastDirectedUnweightedEdges extends FastEdges implements DirectedEdges, UnweightedEdges
{
    /**
     * Constructor.
     */
    public FastDirectedUnweightedEdges()
    {
        super(new FastUnweightedAutoRelation<>(), new FastWeightedAutoRelation<>());
    }

    @Override
    public Stream<Integer> getIncidentNodes(int node)
    {
        return this.weights.getIdsFirst(node).map(weight -> weight.getIdx());
    }

    @Override
    public Stream<Integer> getAdjacentNodes(int node)
    {
        return this.weights.getIdsSecond(node).map(weight -> weight.getIdx());
    }

    @Override
    public Stream<EdgeType> getIncidentTypes(int node)
    {
        return this.types.getIdsFirst(node).map(type -> new EdgeType(type.getIdx(), type.getValue()));
    }

    @Override
    public Stream<EdgeType> getAdjacentTypes(int node)
    {
        return this.types.getIdsSecond(node).map(type -> new EdgeType(type.getIdx(), type.getValue()));
    }

    @Override
    public Stream<IdxPref> getNeighbourWeights(int node)
    {
        List<IdxPref> neighbors = new ArrayList<>();
        Set<Integer> visited = new HashSet<>();
        Comparator<Tuple2oo<Integer, Iterator<Integer>>> comparator = (Tuple2oo<Integer, Iterator<Integer>> x, Tuple2oo<Integer, Iterator<Integer>> y) -> 
        {
            return (int) (x.v1() - y.v1());
        };
        
        PriorityQueue<Tuple2oo<Integer, Iterator<Integer>>> queue = new PriorityQueue<>(2, comparator);
                
        Iterator<Integer> iteratorIncident = this.getIncidentNodes(node).iterator();
        Iterator<Integer> iteratorAdjacent = this.getAdjacentNodes(node).iterator();
        
        if(iteratorIncident.hasNext()) queue.add(new Tuple2oo<>(iteratorIncident.next(), iteratorIncident));
        if(iteratorAdjacent.hasNext()) queue.add(new Tuple2oo<>(iteratorAdjacent.next(), iteratorAdjacent));
        
        while(!queue.isEmpty())
        {
            Tuple2oo<Integer, Iterator<Integer>> tuple = queue.poll();

            if(!visited.contains(tuple.v1()))
            {
                neighbors.add(new IdxPref(tuple.v1(), EdgeWeight.getDefaultValue()));
                visited.add(tuple.v1());
            }

            if(tuple.v2().hasNext())
            {
                queue.add(new Tuple2oo<>(tuple.v2().next(), tuple.v2()));
            }
        }
        
        return neighbors.stream();
    }

    @Override
    public boolean addEdge(int orig, int dest, double weight, int type)
    {
        if(this.weights.addRelation(orig, dest, weight) && this.types.addRelation(orig, dest, type))
        {
            this.numEdges++;
            return true;
        }
        return false;
    }
    
    @Override
    public boolean updateEdgeWeight(int orig, int dest, double weight)
    {
        return this.containsEdge(orig, dest);
    }

    @Override
    public boolean removeNode(int idx)
    {
        int toDel = 0;
        if(this.weights.containsPair(idx, idx)) toDel--;
        toDel += this.getAdjacentCount(idx) + this.getIncidentCount(idx);
        if(this.weights.remove(idx) && this.types.remove(idx))
        {
            this.numEdges -= toDel;
            return true;
        }
        return false;
    }
    
  
    
}
