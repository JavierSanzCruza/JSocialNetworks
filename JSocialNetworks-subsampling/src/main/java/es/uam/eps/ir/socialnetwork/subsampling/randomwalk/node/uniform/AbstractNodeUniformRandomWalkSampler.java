/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.randomwalk.node.uniform;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.subsampling.randomwalk.node.AbstractNodeRandomWalkSampler;
import java.util.List;

/**
 * Abstract class for representing uniform random walk samplers. These methods 
 * select, with uniform probability, the neighbor they are going to visit.
 * 
 * This variant recovers a certain number of nodes, and, after they have been found,
 * retrieves all edges among them.
 * 
 * Gjoka, M., Kurant, M., Butts, C., Markopoulou, A. Practical recommendations on Crawling Online Social Networks, IEEE Journal on Selected Areas in Communications, 29(9) 2011.
 *
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class AbstractNodeUniformRandomWalkSampler<U> extends AbstractNodeRandomWalkSampler<U> 
{

    /**
     * Constructor.
     * @param orientation For directed graphs, it shows the direction we traverse the edges in
     * @param r Teleport rate. With probability equal to r, the random walk will teleport.
     * @param maxNumIterMultiplier Maximum number of iterations before changing the random walk
     * @param numInitial Number of independent random walks.
     */
    public AbstractNodeUniformRandomWalkSampler(EdgeOrientation orientation, double r, int maxNumIterMultiplier, int numInitial) 
    {
        super(orientation, r, maxNumIterMultiplier, numInitial);
    }
    
    @Override
    protected U selectNeighbor(Graph<U> fullGraph, U actual, List<U> neighbors) 
    {
        if(neighbors == null || neighbors.isEmpty())
            return null;
        else
            return neighbors.get(this.getRNG().nextInt(neighbors.size()));
    }
    
}
