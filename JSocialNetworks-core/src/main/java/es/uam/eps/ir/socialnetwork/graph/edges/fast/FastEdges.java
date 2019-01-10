/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.graph.edges.fast;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeType;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeWeight;
import es.uam.eps.ir.socialnetwork.graph.edges.Edges;
import es.uam.eps.ir.socialnetwork.index.AutoRelation;

/**
 * Abstract fast implementation of class Edges.
 * @author Javier Sanz-Cruzado Puig
 */
public abstract class FastEdges implements Edges
{
    /**
     * Current number of edges.
     */
    protected long numEdges = 0L;
    /**
     * Relation for storing the weights of the edges.
     */
    protected final AutoRelation<Double> weights;
    /**
     * Relation for storing the types of the edges.
     */
    protected final AutoRelation<Integer> types;

    /**
     * Constructor.
     * @param weights The weights of the edges.
     * @param types The types of the edges
     */
    public FastEdges(AutoRelation<Double> weights, AutoRelation<Integer> types)
    {
        this.weights = weights;
        this.types = types;
    }
    
    @Override
    public boolean containsEdge(int orig, int dest)
    {
        return this.weights.containsPair(orig, dest);
    }

    @Override
    public double getEdgeWeight(int orig, int dest)
    {
        Double value = this.weights.getValue(orig, dest);
        if(value == null)
        {
            if(this.containsEdge(orig, dest))
                value = EdgeWeight.getDefaultValue();
            else
                value = EdgeWeight.getErrorValue();
        }
        return value;
    }

    @Override
    public int getEdgeType(int orig, int dest)
    {
        Integer value = this.types.getValue(orig, dest);
        if(value == null)
            return EdgeType.getErrorType();
        return value;
    }
    
    @Override
    public boolean addUser(int node)
    {
        return this.weights.addFirstItem(node) && this.types.addFirstItem(node);
    }
    
    @Override
    public long getNumEdges()
    {
        return this.numEdges;
    }
    
    @Override
    public boolean removeEdge(int orig, int dest)
    {
        if(this.weights.removePair(orig, dest) && this.types.removePair(orig, dest))
        {
            this.numEdges--;
            return true;
        }
        return false;
    }
    

    @Override
    public long getIncidentCount(int node)
    {
        return this.types.numFirst(node);
    }
    
    @Override
    public long getAdjacentCount(int node)
    {
        return this.types.numSecond(node);
    }
    
    @Override
    public long getNeighbourCount(int node)
    {
        return this.getNeighbourNodes(node).count();
    }
    
    @Override
    public long getMutualCount(int node)
    {
        return this.getMutualNodes(node).count();
    }
}
