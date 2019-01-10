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
 * Configures a recommendation metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public interface IncrementalRecommendationMetricConfigurator<U,I> 
{
    /**
     * Configures a recommendation metric.
     * @param <F> Type of the features
     * @param params the parameters of the metric.
     * @return the metric.
     */
    public default <F> IncrementalMetricFunction<U,I,F> configure(IncrementalRecommendationMetricParamReader params)
    {
        return (trainData, testData, relModel, discModel, cutoff) ->
        {
            return this.configure(params, trainData, testData, relModel, discModel, cutoff);
        };
    }
    
    /**
     * Configures a recommendation metric.
     * @param <F> Type of the features.
     * @param params parameters.
     * @param trainData training data.
     * @param testData test data.
     * @param relModel relevance model.
     * @param discModel discount model.
     * @param cutoff recommendation cutoff
     * @return the system metric
     */
    public <F> IncrementalSystemMetric<U,I> configure(IncrementalRecommendationMetricParamReader params, PreferenceData<U,I> trainData, PreferenceData<U,I> testData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff);

}
