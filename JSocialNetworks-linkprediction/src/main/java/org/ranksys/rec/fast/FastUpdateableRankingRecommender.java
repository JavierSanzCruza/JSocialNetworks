/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.fast;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import static java.lang.Integer.min;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntPredicate;
import static java.util.stream.Collectors.toList;
import org.ranksys.core.fast.FastRecommendation;
import org.ranksys.core.preferences.fast.updateable.FastUpdateablePreferenceData;
import org.ranksys.core.util.topn.IntDoubleTopN;
import org.ranksys.core.util.tuples.Tuple2id;

/**
 *
 * @author Javier
 */
public abstract class FastUpdateableRankingRecommender<U,I> extends AbstractFastUpdateableRecommender<U,I> 
{

    public FastUpdateableRankingRecommender(FastUpdateablePreferenceData<U, I> prefData) 
    {
        super(prefData);
    }

    @Override
    public FastRecommendation getRecommendation(int uidx, int maxLength, IntPredicate filter) 
    {
        if (uidx == -1) {
            return new FastRecommendation(uidx, new ArrayList<>(0));
        }

        Int2DoubleMap scoresMap = getScoresMap(uidx);

        final IntDoubleTopN topN = new IntDoubleTopN(min(maxLength, scoresMap.size()));
        scoresMap.int2DoubleEntrySet().forEach(e -> {
            int iidx = e.getIntKey();
            double score = e.getDoubleValue();
            if (filter.test(iidx)) {
                topN.add(iidx, score);
            }
        });

        topN.sort();

        List<Tuple2id> items = topN.reverseStream()
                .collect(toList());

        return new FastRecommendation(uidx, items);
    }

    /**
     * Returns a map of item-score pairs.
     *
     * @param uidx index of the user whose scores are predicted
     * @return a map of item-score pairs
     */
    public abstract Int2DoubleMap getScoresMap(int uidx);
    
}
