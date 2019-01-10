/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy;

import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.MetricFunction;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricParamReader;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.basic.AverageRecommendationMetric;
import org.ranksys.metrics.basic.KCall;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Configures a K-Call metric
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * 
 * @see org.ranksys.metrics.basic.KCall
 */
public class KCallConfigurator<U,I> implements RecommendationMetricConfigurator<U,I>
{
    /**
     * Identifier for the number of relevant items.
     */
    private final static String K = "k";

    @Override
    public <F> MetricFunction<U, I, F> configure(RecommendationMetricParamReader params)
    {
        int k = params.getParams().getIntegerValue(K);
        return (trainData, testData, featureData, relModel, discModel, cutoff) -> 
        {
            return new AverageRecommendationMetric<>(new KCall<>(cutoff, k, relModel), true);
        };
    }

    @Override
    public <F> SystemMetric<U, I> configure(RecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, FeatureData<I, F, Double> featureData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        int k = params.getParams().getIntegerValue(K);
        return new AverageRecommendationMetric<>(new KCall<>(cutoff, k, relModel), true);
    }
}
