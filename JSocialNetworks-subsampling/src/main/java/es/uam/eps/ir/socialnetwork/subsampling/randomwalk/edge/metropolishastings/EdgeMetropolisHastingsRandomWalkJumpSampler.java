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

/**
 * Metropolis-Hastings Random walk sampler. This 
 * sampler, with a given probability, teleports to a random node in the network.
 * 
 * This variant recovers a certain number of edges, and, after they have been found,
 * retrieves all nodes in their endpoints and add them to the graph.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class EdgeMetropolisHastingsRandomWalkJumpSampler<U> extends AbstractEdgeMetropolisHastingsRandomWalkSampler<U> 
{
    /**
     * Constructor.
     * @param orientation For directed graphs, it shows the direction we traverse the edges in
     * @param r Teleport rate. With probability equal to r, the random walk will teleport.
     * @param maxNumIterMultiplier Maximum number of iterations before changing the random walk
     * @param numInitial Number of independent random walks.
     */
    public EdgeMetropolisHastingsRandomWalkJumpSampler(EdgeOrientation orientation, double r, int maxNumIterMultiplier, int numInitial) 
    {
        super(orientation, r, maxNumIterMultiplier, numInitial);
    }

    @Override
    protected U teleport(Graph<U> fullGraph, U actual, U origin) 
    {
        return origin;
    }

    
}
