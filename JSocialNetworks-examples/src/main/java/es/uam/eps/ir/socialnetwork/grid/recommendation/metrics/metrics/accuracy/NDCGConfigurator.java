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
import org.ranksys.metrics.basic.NDCG;
import org.ranksys.metrics.basic.NDCG.NDCGRelevanceModel;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Configures a nDCG metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * 
 * @see es.uam.eps.ir.ranksys.metrics.basic.NDCG
 */
public class NDCGConfigurator<U,I> implements RecommendationMetricConfigurator<U,I>
{
    /**
     * Score threshold for identifying the relevant items in the test set.
     */
    private final static String THRESHOLD = "threshold";
    /**
     * Indicates if the preferences have to be cached.
     */
    private final static String CACHING = "caching";

    @Override
    public <F> MetricFunction<U, I, F> configure(RecommendationMetricParamReader params)
    {
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        return (trainData, testData, featureData, relModel, discModel, cutoff) -> 
        {
            NDCGRelevanceModel<U,I> ndcgModel = new NDCGRelevanceModel<>(caching, testData, threshold);
            return new AverageRecommendationMetric<>(new NDCG<>(cutoff, ndcgModel), true);
        };
    }

    @Override
    public <F> SystemMetric<U, I> configure(RecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, FeatureData<I, F, Double> featureData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        NDCGRelevanceModel<U,I> ndcgModel = new NDCGRelevanceModel<>(caching, testData, threshold);
        return new AverageRecommendationMetric<>(new NDCG<>(cutoff, ndcgModel), true);    
    }
}
