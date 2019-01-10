/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation;

import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.index.Index;
import java.util.stream.Stream;

/**
 * Class that represents both user and item indexes for a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class FastGraphIndex<U> implements GraphIndex<U>
{
    /**
     * User index.
     */
    private final Index<U> index;

    /**
     * Constructor. From a FastGraph, extracts the information.
     * @param graph the graph.
     */
    public FastGraphIndex(FastGraph<U> graph)
    {
        this.index = graph.getIndex();
    }
    
    @Override
    public int user2uidx(U u) 
    {
        return this.index.object2idx(u);
    }

    @Override
    public U uidx2user(int i) 
    {
        return this.index.idx2object(i);
    }

    @Override
    public boolean containsUser(U u) 
    {
        return this.index.containsObject(u);
    }

    @Override
    public int numUsers() 
    {
        return this.index.numObjects();
    }

    @Override
    public Stream<U> getAllUsers() 
    {
        return this.index.getAllObjects();
    }

    @Override
    public int item2iidx(U i) 
    {
        return this.user2uidx(i);
    }
}
