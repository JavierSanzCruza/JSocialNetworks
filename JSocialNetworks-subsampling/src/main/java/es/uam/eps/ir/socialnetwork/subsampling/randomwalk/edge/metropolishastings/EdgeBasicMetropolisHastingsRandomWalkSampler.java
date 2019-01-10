/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.randomwalk.edge.metropolishastings;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;

/**
 * Metropolis-Hastings Random walk sampler. This 
 * sampler only teleports to another node if the number of iterations is exceeded,
 * and a new walk has to be started.
 * 
 * This variant recovers a certain number of edges, and, after they have been found,
 * retrieves all nodes in their endpoints and add them to the graph.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class EdgeBasicMetropolisHastingsRandomWalkSampler<U> extends EdgeMetropolisHastingsRandomWalkJumpSampler<U> 
{
    /**
     * Constructor.
     * @param orientation For directed graphs, it shows the direction we traverse the edges in
     * @param maxNumIterMultiplier Maximum number of iterations before changing the random walk
     * @param numInitial Number of independent random walks.
     */
    public EdgeBasicMetropolisHastingsRandomWalkSampler(EdgeOrientation orientation, int maxNumIterMultiplier, int numInitial) 
    {
        super(orientation, 0.0, maxNumIterMultiplier, numInitial);
    }
}
