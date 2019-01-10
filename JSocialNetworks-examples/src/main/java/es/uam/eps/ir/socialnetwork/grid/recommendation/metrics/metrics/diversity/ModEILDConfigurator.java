/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity;

import es.uam.eps.ir.ranksys.socialextension.ModEILD;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.DistanceModelIdentifiers;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.MetricFunction;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricParamReader;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;
import org.ranksys.novdiv.distance.CosineFeatureItemDistanceModel;
import org.ranksys.novdiv.distance.ItemDistanceModel;
import org.ranksys.novdiv.distance.JaccardFeatureItemDistanceModel;

/**
 * Configures a Expected Intra-List Diversity (EILD) metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * @param <F> Type of the features.
 * 
 * @see es.uam.eps.ir.ranksys.diversity.distance.metrics.EILD
 */
public class ModEILDConfigurator<U,I,F> implements RecommendationMetricConfigurator<U,I>
{
    /**
     * Identifier for the distance model.
     */
    private final static String DISTANCEMODEL = "distance";

    @Override
    public <F> MetricFunction<U, I, F> configure(RecommendationMetricParamReader params)
    {
        String distanceModel = params.getParams().getStringValue(DISTANCEMODEL);
        return (trainData,testData,featureData,relModel,discModel,cutoff)->
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
        
            return new ModEILD<>(cutoff, distModel);  
        };
    }

    @Override
    public <F> SystemMetric<U, I> configure(RecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, FeatureData<I, F, Double> featureData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        String distanceModel = params.getParams().getStringValue(DISTANCEMODEL);
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
        
        return new ModEILD<>(cutoff, distModel);    
    }
}
