/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.novelty;

import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricParamReader;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.basic.AverageRecommendationMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;
import org.ranksys.novdiv.longtail.PCItemNovelty;
import org.ranksys.novdiv.longtail.metrics.EPC;

/**
 * Configures an Expected Popularity Complement (EPC) precision metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * 
 * @see es.uam.eps.ir.ranksys.novelty.longtail.metrics.EPC
*/
public class EPCConfigurator<U,I> implements RecommendationMetricConfigurator<U,I>
{
    @Override
    public <F> SystemMetric<U, I> configure(RecommendationMetricParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData, FeatureData<I, F, Double> featureData, RelevanceModel<U, I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        PCItemNovelty<U,I> itemNov = new PCItemNovelty(trainData);
        return new AverageRecommendationMetric<>(new EPC<>(cutoff, itemNov, relModel, discModel), true);
    }
}
