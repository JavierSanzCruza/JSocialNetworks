/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.neigh.updateable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.ranksys.core.util.tuples.Tuple2id;
import org.ranksys.rec.nn.sim.updateable.UpdateableSimilarity;

/**
 *
 * @author Javier
 */
public class CachedTopKUpdateableNeighborhood extends CachedUpdateableNeighborhood
{
    private final int k;

    public CachedTopKUpdateableNeighborhood(int n, UpdateableSimilarity sim, int k) {
        super(n, sim);
        this.k = k;
    }
    

    @Override
    public Stream<Tuple2id> getNeighbors(int idx) 
    {
        List<Tuple2id> list = new ArrayList<>();
        List<Integer> ilist = this.idxla.get(idx);
        List<Double> dlist = this.simla.get(idx);
        
        int m = Math.min(k, ilist.size());
        for(int i = 0; i < m; ++i)
        {
            list.add(new Tuple2id(ilist.get(i), dlist.get(i)));
        }
        
        return list.stream();
    }
    
}
