/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental;

import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.IncrementalSystemMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Function for retrieving evaluation metrics.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users
 * @param <I> Type of the items
 * @param <F> Type of the items
 */
@FunctionalInterface
public interface IncrementalMetricFunction<U, I, F> 
{
    /**
     * Applies this function to the given arguments.
     * @param trainData Training data
     * @param testData Test data
     * @param relModel relevance model
     * @param discModel discount model
     * @param cutoff cutoff of the recommendation
     * @return a system metric.
     */
    IncrementalSystemMetric<U,I> apply(PreferenceData<U,I> trainData, PreferenceData<U,I> testData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff);
}
