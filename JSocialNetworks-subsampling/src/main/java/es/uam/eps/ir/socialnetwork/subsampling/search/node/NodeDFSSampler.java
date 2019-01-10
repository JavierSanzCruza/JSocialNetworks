/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.search.node;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Sampler that uses the Depth-First Search (DFS) algorithm.
 * 
 * In this version, all edges between visited nodes are retrieved.
 * 
 * Doerr, C., Blenn, N. Metric convergence in social network sampling. HotPlanet 2013.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * 
 */
public class NodeDFSSampler<U> extends AbstractNodeSearchSampler<U> 
{
    /**
     * Constructor.
     * @param orientation The orientation.
     */
    public NodeDFSSampler(EdgeOrientation orientation) 
    {
        super(orientation, 1);
    }

    @Override
    protected Collection<U> getChildren(Graph<U> fullGraph, U actual, List<U> allChildren) 
    {
        return allChildren;
    }

    @Override
    protected U nextActual(LinkedList<U> queue) 
    {
        return queue.pollLast();
    }

}
