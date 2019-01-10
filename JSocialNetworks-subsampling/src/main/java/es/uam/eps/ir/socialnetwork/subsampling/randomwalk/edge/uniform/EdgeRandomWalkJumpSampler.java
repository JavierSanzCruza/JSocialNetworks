/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.randomwalk.edge.uniform;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Uniform Random walk sampler that selects nodes uniformly from their neighborhoods. This 
 * sampler, with a given probability, teleports to a random node in the network.
 * 
 * This variant recovers a certain number of edges, and, after they have been found,
 * retrieves all nodes in their endpoints and add them to the graph.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class EdgeRandomWalkJumpSampler<U> extends AbstractEdgeUniformRandomWalkSampler<U> 
{
    /**
     * Constructor.
     * @param orientation For directed graphs, it shows the direction we traverse the edges in
     * @param r Teleport rate. With probability equal to r, the random walk will teleport.
     * @param maxNumIterMultiplier Maximum number of iterations before changing the random walk
     * @param numInitial Number of independent random walks.
     */
    public EdgeRandomWalkJumpSampler(EdgeOrientation orientation, double r, int maxNumIterMultiplier, int numInitial) 
    {
        super(orientation, r, maxNumIterMultiplier, numInitial);
    }

    @Override
    protected U teleport(Graph<U> fullGraph, U actual, U origin) 
    {
        List<U> vertices = fullGraph.getAllNodes().collect(Collectors.toCollection(ArrayList::new));
        int idx = this.getRNG().nextInt(vertices.size());
        return vertices.get(idx);
    }
}
