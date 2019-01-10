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
import org.ranksys.core.preference.ConcatPreferenceData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.basic.AverageRecommendationMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;
import org.ranksys.novdiv.intentaware.FeatureIntentModel;
import org.ranksys.novdiv.intentaware.IntentModel;
import org.ranksys.novdiv.intentaware.metrics.ERRIA;
import org.ranksys.novdiv.intentaware.metrics.ERRIA.ERRRelevanceModel;

/**
 * Configures a ERR-IA metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * @param <F> Type of the features.
 * 
 * @see es.uam.eps.ir.ranksys.diversity.intentaware.metrics.ERRIA
 */
public class ERRIAConfigurator<U,I,F> implements RecommendationMetricConfigurator<U,I>
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
            PreferenceData<U,I> totalData = new ConcatPreferenceData(trainData, testData);

            ERRRelevanceModel<U,I> errModel = new ERRRelevanceModel<>(caching, testData, threshold);
            IntentModel<U,I,F> intentModel = new FeatureIntentModel<>(totalData, featureData);
            return new AverageRecommendationMetric<>(new ERRIA<>(cutoff, intentModel, errModel), true);
        };
    }

    @Override
    public <F> SystemMetric<U, I> configure(RecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, FeatureData<I, F, Double> featureData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        PreferenceData<U,I> totalData = new ConcatPreferenceData(trainData, testData);
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        ERRRelevanceModel<U,I> errModel = new ERRRelevanceModel<>(caching, testData, threshold);
        IntentModel<U,I,F> intentModel = new FeatureIntentModel<>(totalData, featureData);
        return new AverageRecommendationMetric<>(new ERRIA<>(cutoff, intentModel, errModel), true);
    }
}
