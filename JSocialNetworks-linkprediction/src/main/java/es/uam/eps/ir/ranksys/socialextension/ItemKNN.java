/* 
 * Copyright (C) 2015 Information Retrieval Group at Universidad Autónoma
 * de Madrid, http://ir.ii.uam.es
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.socialextension;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.recommenders.fast.FastRankingRecommender;
import org.ranksys.recommenders.nn.item.neighborhood.ItemNeighborhood;

/**
 * Item-based nearest neighbors recommender.
 * 
 * F. Aiolli. Efficient Top-N Recommendation for Very Large Scale Binary Rated
 * Datasets. RecSys 2013.
 * 
 * Paolo Cremonesi, Yehuda Koren, and Roberto Turrin. Performance of 
 * recommender algorithms on top-n recommendation tasks. RecSys 2010.
 * 
 * C. Desrosiers, G. Karypis. A comprehensive survey of neighborhood-based 
 * recommendation methods. Recommender Systems Handbook.
 *
 * @author Javier Sanz-Cruzado Puig
 *
 * @param <U> Type of the users
 * @param <I> Type of the items
 */
public class ItemKNN<U, I> extends FastRankingRecommender<U, I> {

    /**
     * Preference data.
     */
    protected final FastPreferenceData<U, I> data;

    /**
     * User neighborhood.
     */
    protected final ItemNeighborhood<I> neighborhood;

    /**
     * Exponent of the similarity.
     */
    protected final int q;

    /**
     * Constructor.
     *
     * @param data preference data
     * @param neighborhood user neighborhood
     * @param q exponent of the similarity
     */
    public ItemKNN(FastPreferenceData<U, I> data, ItemNeighborhood<I> neighborhood, int q) {
        super(data, data);
        this.data = data;
        this.neighborhood = neighborhood;
        this.q = q;
    }

    /**
     * Returns a map of item-score pairs.
     *
     * @param uidx index of the user whose scores are predicted
     * @return a map of item-score pairs
     */
    @Override
    public Int2DoubleMap getScoresMap(int uidx) {
        Int2DoubleOpenHashMap scoresMap = new Int2DoubleOpenHashMap();
        scoresMap.defaultReturnValue(0.0);
        
        Int2DoubleMap ratings = new Int2DoubleOpenHashMap();
        ratings.defaultReturnValue(0.0);
        data.getUidxPreferences(uidx).forEach(pref -> ratings.put(pref.v1, pref.v2));
        
        
        data.getAllIidx().forEach(vidx -> 
        {
            double score = neighborhood.getNeighbors(vidx).mapToDouble(neigh -> 
            {
                double w = Math.pow(neigh.v2, this.q);
                return w * ratings.get(neigh.v1);
            }).sum();
            scoresMap.put(vidx, score);
        });

        return scoresMap;
    }
}