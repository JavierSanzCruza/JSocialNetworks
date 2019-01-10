/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.randomwalk.edge.metropolishastings;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.subsampling.randomwalk.edge.AbstractEdgeRandomWalkSampler;
import java.util.List;

/**
 * Abstract class for representing Metropolis-Hastings random walk samplers. These methods
 * randomly select the neighbor which they are going to travel to. Then, with probability 
 * equal to 1.0 if the degree of the selected neighbor is smaller than the currently visited one, 
 * or with probability proportional to the division of the number of neighbors of the actual user divided
 * by the number of neighbors of the selected user, it travels to the selected node. Otherwise, it does not 
 * move.
 * 
 * This variant recovers a certain number of edges, and, after they have been found,
 * retrieves all nodes in their endpoints and add them to the graph.
 *
 * Gjoka, M., Kurant, M., Butts, C., Markopoulou, A. Practical recommendations on Crawling Online Social Networks, IEEE Journal on Selected Areas in Communications, 29(9) 2011.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public abstract class AbstractEdgeMetropolisHastingsRandomWalkSampler<U> extends AbstractEdgeRandomWalkSampler<U> 
{

    /**
     * Constructor.
     * @param orientation For directed graphs, it shows the direction we traverse the edges in
     * @param r Teleport rate. With probability equal to r, the random walk will teleport.
     * @param maxNumIterMultiplier Maximum number of iterations before changing the random walk
     * @param numInitial Number of independent random walks.
     */
    public AbstractEdgeMetropolisHastingsRandomWalkSampler(EdgeOrientation orientation, double r, int maxNumIterMultiplier, int numInitial) 
    {
        super(orientation, r, maxNumIterMultiplier, numInitial);
    }
    
    @Override
    protected U selectNeighbor(Graph<U> fullGraph, U actual, List<U> neighbors) 
    {
        U next = null;
        if(neighbors != null && !neighbors.isEmpty())
        {
            U selected = neighbors.get(this.getRNG().nextInt(neighbors.size()));
            double actualSize = neighbors.size() + 0.0;
            double selectedSize = fullGraph.getNeighbourhoodSize(selected, this.getOrientation()) + 0.0;

            if(selectedSize == 0)
            {
                next = selected;
            }
            else
            {
                double value = Math.min(1.0, actualSize/selectedSize);
                if(this.getRNG().nextDouble() < value)
                {
                    next = selected;
                }
            }
                
        }
        return next;
    }
    
}
