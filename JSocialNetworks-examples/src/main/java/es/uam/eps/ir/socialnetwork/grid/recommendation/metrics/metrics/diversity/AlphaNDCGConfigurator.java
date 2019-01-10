/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity;

import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.MetricFunction;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricParamReader;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.basic.AverageRecommendationMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.BinaryRelevanceModel;
import org.ranksys.metrics.rel.RelevanceModel;
import org.ranksys.novdiv.intentaware.metrics.AlphaNDCG;

/**
 * Configures a alpha nDCG metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * @param <F> Type of the features.
 * 
 * @see es.uam.eps.ir.ranksys.diversity.intentaware.metrics.AlphaNDCG
 */
public class AlphaNDCGConfigurator<U,I,F> implements RecommendationMetricConfigurator<U,I>
{
    /**
     * Identifier for the relevance threshold score.
     */
    private final static String THRESHOLD = "threshold";
    /**
     * Identifier for determining if relevance has to be cached.
     */
    private final static String CACHING = "caching";
    /**
     * Identifier for the alpha parameter.
     */
    private final static String ALPHA = "alpha";
    
    @Override
    public <F> MetricFunction<U, I, F> configure(RecommendationMetricParamReader params)
    {
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        double alpha = params.getParams().getDoubleValue(ALPHA);
        
        return (trainData, testData, featureData, relModel, discModel, cutoff)->
        {
            BinaryRelevanceModel<U,I> idealModel = new BinaryRelevanceModel<>(caching, testData, threshold);
            return new AverageRecommendationMetric<>(new AlphaNDCG<>(cutoff, alpha, featureData, idealModel), true);
        };
    }

    @Override
    public <F> SystemMetric<U, I> configure(RecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, FeatureData<I, F, Double> featureData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        double alpha = params.getParams().getDoubleValue(ALPHA);
        BinaryRelevanceModel<U,I> idealModel = new BinaryRelevanceModel<>(caching, testData, threshold);
        return new AverageRecommendationMetric<>(new AlphaNDCG<>(cutoff, alpha, featureData, idealModel), true);
    }
}
