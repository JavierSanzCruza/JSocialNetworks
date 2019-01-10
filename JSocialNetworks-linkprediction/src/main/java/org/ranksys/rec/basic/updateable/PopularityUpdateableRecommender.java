/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.basic.updateable;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import static java.lang.Math.min;
import java.util.List;
import java.util.function.IntPredicate;
import static java.util.stream.Collectors.toList;
import static java.util.Comparator.comparingDouble;
import java.util.stream.Stream;
import org.jooq.lambda.tuple.Tuple3;
import org.ranksys.core.fast.FastRecommendation;
import org.ranksys.core.preferences.fast.updateable.FastUpdateablePreferenceData;
import org.ranksys.core.util.tuples.Tuple2id;
import static org.ranksys.core.util.tuples.Tuples.tuple;
import org.ranksys.rec.updateable.UpdateableRecommender;
import org.ranksys.rec.fast.AbstractFastUpdateableRecommender;

/**
 * Popularity-based recommender. Non-personalized recommender that returns the
 * most popular items according to the preference data provided.
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 * 
 * @param <U> type of the users
 * @param <I> type of the items
 */
public class PopularityUpdateableRecommender<U, I> extends AbstractFastUpdateableRecommender<U, I> implements UpdateableRecommender<U,I>
{
    /**
     * Popularity list.
     */
    private List<Tuple2id> popList;
    /**
     * Map containing the popularity value.
     */
    private final Int2DoubleMap pop;
    
    /**
     * Constructor.
     *
     * @param data preference data
     */
    public PopularityUpdateableRecommender(FastUpdateablePreferenceData<U, I> data) 
    {
        super(data);

        pop = new Int2DoubleOpenHashMap();
        popList = data.getIidxWithPreferences()
                .mapToObj(iidx -> 
                        {
                            double popul = data.numUsers(iidx);
                            pop.put(iidx, popul);
                            return tuple(iidx, popul);
                        })
                .sorted(comparingDouble(Tuple2id::v2).reversed())
                .collect(toList());
    }

    @Override
    public FastRecommendation getRecommendation(int uidx, int maxLength, IntPredicate filter) 
    {
        
        List<Tuple2id> items = popList.stream()
                .filter(is -> filter.test(is.v1))
                .limit(min(maxLength, popList.size()))
                .collect(toList());
        
        return new FastRecommendation(uidx, items);
    }
    
    @Override
    public void updateAddUser(U u)
    {
        this.prefData.addUser(u);
    }
    
    @Override
    public void updateAddItem(I i)
    {
        this.prefData.addItem(i);
        int iidx = this.prefData.item2iidx(i);
        pop.put(iidx, 0.0);
        
    }

    @Override
    public void update(Stream<Tuple3<U, I, Double>> tuples) 
    {      
        // Update the data
        int numMod = tuples.mapToInt(t -> 
        {
            int numPreferences = this.prefData.numPreferences();
            this.prefData.update(t.v1,t.v2,t.v3);
            int increase = this.prefData.numPreferences() - numPreferences;
            if(increase > 0)
            {
                int vidx = this.item2iidx(t.v2);
                ((Int2DoubleOpenHashMap) this.pop).addTo(vidx, 1.0);
            }
            return increase;
        }).sum();

        // Update the popularity list
        if(numMod > 0)
            popList = pop.int2DoubleEntrySet().stream()
                     .map(t -> tuple(t.getIntKey(), t.getDoubleValue()))
                     .sorted(comparingDouble(Tuple2id::v2).reversed()).collect(toList());
    }

    @Override
    public void update(U u, I i, double val) 
    {
        int numPreferences = this.prefData.numPreferences();
        this.prefData.update(u,i,val);
        if(numPreferences < this.prefData.numPreferences())
        {
            int vidx = this.item2iidx(i);
            ((Int2DoubleOpenHashMap) this.pop).addTo(vidx, 1.0);
            
            popList = pop.int2DoubleEntrySet().stream()
                     .map(t -> tuple(t.getIntKey(), t.getDoubleValue()))
                     .sorted(comparingDouble(Tuple2id::v2).reversed()).collect(toList());
        }
    }

    @Override
    public void updateDelete(Stream<Tuple3<U, I, Double>> tuples) 
    {
        int numMod = tuples.mapToInt(t -> 
        {
            int numPreferences = this.prefData.numPreferences();
            this.prefData.updateDelete(t.v1(),t.v2());
            int increase = numPreferences - this.prefData.numPreferences();
            if(increase > 0)
            {
                int vidx = this.item2iidx(t.v2());
                ((Int2DoubleOpenHashMap) this.pop).addTo(vidx, 1.0);
            }
            return increase;
        }).sum();
        
        // Update the popularity list
        if(numMod > 0)
            popList = pop.int2DoubleEntrySet().stream()
                     .map(t -> tuple(t.getIntKey(), t.getDoubleValue()))
                     .sorted(comparingDouble(Tuple2id::v2).reversed()).collect(toList());
    }

    @Override
    public void updateDelete(U u, I i, double val) 
    {
        int numPreferences = this.prefData.numPreferences();
        this.prefData.updateDelete(u,i);
        if(numPreferences > this.prefData.numPreferences())
        {
            int vidx = this.item2iidx(i);
            ((Int2DoubleOpenHashMap) this.pop).addTo(vidx, 1.0);
            
            popList = pop.int2DoubleEntrySet().stream()
                     .map(t -> tuple(t.getIntKey(), t.getDoubleValue()))
                     .sorted(comparingDouble(Tuple2id::v2).reversed()).collect(toList());
        }    
    }

    /*
    @Override
    public void updateRemoveUser(U u)
    {
        // Note: this could be vastly improved if users were different from
        // items. But, due to the general nature of the framework, we cannot
        // assume this...
        this.prefData.updateRemoveUser(u);
        pop.clear();
        popList = this.prefData.getIidxWithPreferences()
                .mapToObj(iidx -> 
                        {
                            double popul = this.prefData.numUsers(iidx);
                            pop.put(iidx, popul);
                            return tuple(iidx, popul);
                        })
                .sorted(comparingDouble(Tuple2id::v2).reversed())
                .collect(toList());
    }

    @Override
    public void updateRemoveItem(I i)
    {
        // Note: this could be vastly improved if users were different from
        // items. But, due to the general nature of the framework, we cannot
        // assume this...
        this.prefData.updateRemoveItem(i);
        pop.clear();
        popList = this.prefData.getIidxWithPreferences()
                .mapToObj(iidx -> 
                        {
                            double popul = this.prefData.numUsers(iidx);
                            pop.put(iidx, popul);
                            return tuple(iidx, popul);
                        })
                .sorted(comparingDouble(Tuple2id::v2).reversed())
                .collect(toList());
    }*/
}
