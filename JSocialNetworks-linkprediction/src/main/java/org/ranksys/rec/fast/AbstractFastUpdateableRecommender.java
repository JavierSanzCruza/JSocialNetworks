/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.fast;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.ranksys.core.Recommendation;
import org.ranksys.core.fast.FastRecommendation;
import org.ranksys.core.preferences.fast.updateable.FastUpdateablePreferenceData;
import org.ranksys.recommenders.AbstractRecommender;

/**
 * Abstract (fast) recommender. It implements the free and candidate-based recommendation methods as variants of the filter recommendation.
 *
 * @author Saúl Vargas (saul.vargas@uam.es)
 *
 * @param <U> type of the users
 * @param <I> type of the items
 */
public abstract class AbstractFastUpdateableRecommender<U, I> extends AbstractRecommender<U, I> implements FastUpdateableRecommender<U, I> 
{

    /**
     * Fast preference data.
     */
    protected final FastUpdateablePreferenceData<U,I> prefData;
    
    /**
     * Constructor.
     *
     * @param prefData Preference data proxy (cannot be modified)
     */
    public AbstractFastUpdateableRecommender(FastUpdateablePreferenceData<U,I> prefData) 
    {
        super();

        this.prefData = prefData;
    }

    @Override
    public int numUsers() 
    {
        return prefData.numUsers();
    }

    @Override
    public int user2uidx(U u) 
    {
        return prefData.user2uidx(u);
    }

    @Override
    public U uidx2user(int uidx) 
    {
        return prefData.uidx2user(uidx);
    }

    @Override
    public int numItems() 
    {
        return prefData.numItems();
    }

    @Override
    public int item2iidx(I i) 
    {
        return prefData.item2iidx(i);
    }

    @Override
    public I iidx2item(int iidx) 
    {
        return prefData.iidx2item(iidx);
    }

    @Override
    public Recommendation<U, I> getRecommendation(U u, int maxLength) 
    {
        FastRecommendation rec = getRecommendation(user2uidx(u), maxLength);

        return new Recommendation<>(uidx2user(rec.getUidx()), rec.getIidxs().stream()
                .map(this::iidx2item)
                .collect(toList()));
    }

    @Override
    public FastRecommendation getRecommendation(int uidx) 
    {
        return getRecommendation(uidx, Integer.MAX_VALUE);
    }

    @Override
    public FastRecommendation getRecommendation(int uidx, int maxLength) 
    {
        return getRecommendation(uidx, maxLength, iidx -> true);
    }

    @Override
    public Recommendation<U, I> getRecommendation(U u, int maxLength, Predicate<I> filter) 
    {
        FastRecommendation rec = getRecommendation(user2uidx(u), maxLength, iidx -> filter.test(iidx2item(iidx)));

        return new Recommendation<>(uidx2user(rec.getUidx()), rec.getIidxs().stream()
                .map(this::iidx2item)
                .collect(toList()));
    }

    @Override
    public FastRecommendation getRecommendation(int uidx, IntPredicate filter) 
    {
        return getRecommendation(uidx, Integer.MAX_VALUE, filter);
    }

    @Override
    public abstract FastRecommendation getRecommendation(int uidx, int maxLength, IntPredicate filter);

    @Override
    public Recommendation<U, I> getRecommendation(U u, Stream<I> candidates) 
    {
        FastRecommendation rec = getRecommendation(user2uidx(u), candidates.mapToInt(this::item2iidx));

        return new Recommendation<>(uidx2user(rec.getUidx()), rec.getIidxs().stream()
                .map(this::iidx2item)
                .collect(toList()));
    }

    @Override
    public FastRecommendation getRecommendation(int uidx, IntStream candidates) 
    {
        IntSet set = new IntOpenHashSet();
        candidates.forEach(iidx -> set.add(iidx));

        return getRecommendation(uidx, item -> set.contains(item));
    }
}
