/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.subsampling.search.traverse;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Finds a sample of a graph using the method known as Random-First Search (RFS)
 * 
 * In this version, only traversed edges are sampled, but the sample is oriented
 * to retrieve a fixed number of nodes.
 * 
 * Doerr, C., Blenn, N. Metric convergence in social network sampling. HotPlanet 2013.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of users
 */
public class TraverseRFSSampler<U> extends AbstractTraverseSearchSampler<U> 
{
    private final Random rng = new Random();
    /**
     * Constructor.
     * @param orientation The orientation.
     */
    public TraverseRFSSampler(EdgeOrientation orientation) 
    {
        super(orientation, 1);
    }

    @Override
    protected Collection<Tuple2oo<U,EdgeOrientation>> getChildren(Graph<U> fullGraph, U actual,List<U> allChildren) 
    {
        List<Tuple2oo<U,EdgeOrientation>> orient = new ArrayList<>();
        allChildren.forEach(u -> orient.add(new Tuple2oo<>(u, this.getOrientation())));
        return orient;
    }

    @Override
    protected U nextActual(LinkedList<U> queue) 
    {
        int idx = rng.nextInt(queue.size());
        U actual = queue.get(idx);
        queue.remove(idx);
        return actual;
    }

}
