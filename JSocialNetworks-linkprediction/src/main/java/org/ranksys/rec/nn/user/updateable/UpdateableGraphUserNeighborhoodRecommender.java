/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.user.updateable;

import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.stream.Stream;
import org.jooq.lambda.tuple.Tuple3;
import org.ranksys.core.preferences.fast.updateable.FastUpdateablePreferenceData;
import org.ranksys.rec.fast.FastUpdateableRankingRecommender;
import org.ranksys.rec.nn.neigh.updateable.UpdateableUserNeighborhood;

/**
 * 
 * @author Javier
 * @param <U>
 */
public class UpdateableGraphUserNeighborhoodRecommender<U> extends FastUpdateableRankingRecommender<U,U> 
{
    protected final FastUpdateablePreferenceData<U,U> data;
    protected final UpdateableUserNeighborhood<U> neighborhood;
    protected final int q;

    public UpdateableGraphUserNeighborhoodRecommender(FastUpdateablePreferenceData<U, U> prefData, UpdateableUserNeighborhood<U> neighborhood, int q) 
    {
        super(prefData);
        this.data = prefData;
        this.neighborhood = neighborhood;
        this.q = q;
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        Int2DoubleOpenHashMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(0.0);
        neighborhood.getNeighbors(uidx).forEach(vs -> 
            {
                double w = Math.pow(vs.v2,q);
                data.getUidxPreferences(vs.v1).forEach(iv -> 
            {
                double p = w*iv.v2;
                scoresMap.addTo(iv.v1, p);
            });   
        });
        
        return scoresMap;
    }

   
    

    @Override
    public void update(U u, U i, double val) 
    {
        this.neighborhood.updateAdd(this.prefData.user2uidx(u), this.prefData.item2iidx(i), val);
        this.prefData.update(u, i, val);
    }

    @Override
    public void updateDelete(Stream<Tuple3<U, U, Double>> tuples) 
    {
        tuples.forEach(t -> this.updateDelete(t.v1(), t.v2(), t.v3()));
    }

    @Override
    public void updateDelete(U u, U i, double val) 
    {
        this.neighborhood.updateDelete(this.prefData.user2uidx(u), this.prefData.item2iidx(i));
        this.prefData.updateDelete(u,i);
    }

    @Override
    public void updateAddUser(U u) 
    {
        this.neighborhood.addUser(u);
        this.prefData.addUser(u);
    }

    @Override
    public void updateAddItem(U i) {
        this.updateAddUser(i);
    }

    @Override
    public void update(Stream<Tuple3<U, U, Double>> tuples) {
        tuples.forEach(t -> 
        {
            if(this.containsUser(t.v1))
            {
                this.updateAddUser(t.v1);
            }
            
            if(this.containsItem(t.v2))
            {
                this.updateAddUser(t.v2);
            }
            
            this.update(t.v1, t.v2, t.v3);
        });
    }
    
}
