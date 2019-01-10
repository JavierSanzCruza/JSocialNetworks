/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.ranksys.core.index.fast.FastItemIndex;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.recommenders.fast.FastRankingRecommender;

/**
 * Inverse recommender. Given a recommender, top items appear in bottom positions, and viceversa.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the user.
 */
public class InverseRecommender<U> extends FastRankingRecommender<U,U> 
{

    /**
     * Original recommender
     */
    private final FastRankingRecommender<U,U> rec;
    
    /**
     * Constructor.
     * @param uIndex User index.
     * @param iIndex Item index (equal to user index).
     * @param rec Recommendation.
     */
    public InverseRecommender(FastUserIndex<U> uIndex, FastItemIndex<U> iIndex, FastRankingRecommender<U,U> rec) {
        super(uIndex, iIndex);
        this.rec = rec;
    }

    @Override
    public Int2DoubleMap getScoresMap(int i) {
        Int2DoubleMap aux = this.rec.getScoresMap(i);
        
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        aux.int2DoubleEntrySet().forEach(entry -> {
            scoresMap.put((int) entry.getIntKey(), (double) -entry.getDoubleValue());
        });
        
        return scoresMap;
    }

    
}
