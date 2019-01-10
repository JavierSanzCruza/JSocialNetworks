/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental.accuracy;

import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental.IncrementalMetricFunction;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental.IncrementalRecommendationMetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental.IncrementalRecommendationMetricParamReader;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.IncrementalSystemMetric;
import org.ranksys.metrics.incremental.system.IncrementalSystemRecall;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.BinaryRelevanceModel;
import org.ranksys.metrics.rel.IdealRelevanceModel;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Configures an incremental system recall metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * 
 * @see org.ranksys.metrics.incremental.system.IncrementalSystemRecall
 */
public class IncrementalSystemRecallConfigurator<U,I> implements IncrementalRecommendationMetricConfigurator<U,I>
{
    /**
     * Score threshold for identifying the relevant items in the test set.
     */
    private final static String THRESHOLD = "threshold";
    /**
     * Indicates if the preferences have to be cached.
     */
    private final static String CACHING = "caching";
    /**
     * Indicates if the external influence has to be considered or not.
     */
    private final static String REMOVEXT = "removeExt";
    
    @Override
    public <F> IncrementalMetricFunction<U, I, F> configure(IncrementalRecommendationMetricParamReader params)
    {
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        boolean removeExt = params.getParams().getBooleanValue(REMOVEXT);
        return (trainData, testData, relModel, discModel, cutoff) -> 
        {
            IdealRelevanceModel<U,I> idealModel = new BinaryRelevanceModel<>(caching, testData, threshold);
            return new IncrementalSystemRecall<>(testData,idealModel, cutoff,removeExt);
        };
    }

    @Override
    public <F> IncrementalSystemMetric<U, I> configure(IncrementalRecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        boolean removeExt = params.getParams().getBooleanValue(REMOVEXT);

        IdealRelevanceModel<U,I> idealModel = new BinaryRelevanceModel<>(caching, testData, threshold);
        return new IncrementalSystemRecall<>(testData,idealModel, cutoff,removeExt);
    }
}
