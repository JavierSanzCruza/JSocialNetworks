/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.novelty;

import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.DistanceModelIdentifiers;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.MetricFunction;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricParamReader;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.basic.AverageRecommendationMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;
import org.ranksys.novdiv.distance.CosineFeatureItemDistanceModel;
import org.ranksys.novdiv.distance.ItemDistanceModel;
import org.ranksys.novdiv.distance.JaccardFeatureItemDistanceModel;
import org.ranksys.novdiv.unexp.PDItemNovelty;
import org.ranksys.novdiv.unexp.metrics.EPD;

/**
 * Configures a Expected Profile Distance (EPD) metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * @param <F> Type of the parameters.
 * 
 * @see es.uam.eps.ir.ranksys.novelty.unexp.metrics.EPD
 */
public class EPDConfigurator<U,I,F> implements RecommendationMetricConfigurator<U,I>
{
    /**
     * Identifier for the item distance model.
     */
    private final static String DISTANCEMODEL = "distance";
    /**
     * Indicates if the preferences have to be cached
     */
    private final static String CACHING = "caching";
    
    @Override
    public <F> SystemMetric<U, I> configure(RecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, FeatureData<I, F, Double> featureData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        String distanceModel = params.getParams().getStringValue(DISTANCEMODEL);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        ItemDistanceModel<I> distModel;
        switch(distanceModel)
        {
            case DistanceModelIdentifiers.COSINE:
                distModel = new CosineFeatureItemDistanceModel<>(featureData);
                break;
            case DistanceModelIdentifiers.JACCARD:
                distModel = new JaccardFeatureItemDistanceModel<>(featureData);
                break;
            default:
                return null;
                
        }
        
        PDItemNovelty<U,I> itemNov = new PDItemNovelty(caching, trainData, distModel);
        return new AverageRecommendationMetric<>(new EPD<>(cutoff, itemNov, relModel, discModel), true);
    }
    
    @Override
    public <F> MetricFunction<U,I,F> configure(RecommendationMetricParamReader params)
    {
        String distanceModel = params.getParams().getStringValue(DISTANCEMODEL);
        boolean caching = params.getParams().getBooleanValue(CACHING);
        
        return (trainData, testData, featureData, relModel, discModel, cutoff) ->
        {
            ItemDistanceModel<I> distModel;
            switch(distanceModel)
            {
                case DistanceModelIdentifiers.COSINE:
                    distModel = new CosineFeatureItemDistanceModel<>(featureData);
                    break;
                case DistanceModelIdentifiers.JACCARD:
                    distModel = new JaccardFeatureItemDistanceModel<>(featureData);
                    break;
                default:
                    return null;

            }
        
            PDItemNovelty<U,I> itemNov = new PDItemNovelty(caching, trainData, distModel);
            return new AverageRecommendationMetric<>(new EPD<>(cutoff, itemNov, relModel, discModel), true);
        };
    }
}
