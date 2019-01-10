/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.randomwalk.node.metropolishastings;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;

/**
 * Metropolis-Hastings Random walk sampler. This 
 * sampler only teleports to another node if the number of iterations is exceeded,
 * and a new walk has to be started.
 * 
 * This variant recovers a certain number of nodes, and, after they have been found,
 * retrieves all edges among them.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class NodeBasicMetropolisHastingsRandomWalkSampler<U> extends NodeMetropolisHastingsRandomWalkJumpSampler<U> 
{
    /**
     * Constructor.
     * @param orientation For directed graphs, it shows the direction we traverse the edges in
     * @param maxNumIterMultiplier Maximum number of iterations before changing the random walk
     * @param numInitial Number of independent random walks.
     */
    public NodeBasicMetropolisHastingsRandomWalkSampler(EdgeOrientation orientation, int maxNumIterMultiplier, int numInitial) 
    {
        super(orientation, 0.0, maxNumIterMultiplier, numInitial);
    }
}
