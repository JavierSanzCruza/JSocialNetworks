/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental.IncrementalRecommendationMetricIdentifiers.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental.accuracy.IncrementalRecallConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.incremental.accuracy.IncrementalSystemRecallConfigurator;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.IncrementalSystemMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Class that selects an incremental recommendation metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * @param <F> Type of the features.
 */
public class IncrementalRecommendationMetricSelector<U,I,F> 
{  
    /**
     * Obtains a configurator for a metric.
     * @param metric the name of the metric.
     * @return the configurator for the metric if it exists, null otherwise.
     */
    public IncrementalRecommendationMetricConfigurator<U,I> getConfigurator(String metric)
    {
        IncrementalRecommendationMetricConfigurator<U,I> conf;
        switch(metric)
        {
            // Accuracy metrics
            case RECALL:
                conf = new IncrementalRecallConfigurator<>();
                break;
            case SYSRECALL:
                conf = new IncrementalSystemRecallConfigurator<>();
                break;
            default:
                return null;
        }
        
        return conf;
    }
    
    /**
     * Selects and configures an incremental recommendation metric.
     * @param mpr the parameters for the incremental recommendation metric.
     * @param trainData training data.
     * @param testData test data.
     * @param featureData feature data of the different items.
     * @param relModel indicates whether a tuple (user, item) is relevant or not.
     * @param discModel indicates how the position in the ranking affects the evaluation of the metric.
     * @param cutoff maximum number of elements in the ranking to consider.
     * @return a tuple containing the name of the metric and the metric if the metric exists, null otherwise.
     */
    public Tuple2oo<String, IncrementalSystemMetric<U,I>> select(IncrementalRecommendationMetricParamReader mpr, PreferenceData<U,I> trainData, PreferenceData<U,I> testData, FeatureData<I,F,Double> featureData, RelevanceModel<U,I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        String name = mpr.getName();
        IncrementalRecommendationMetricConfigurator<U,I> conf = this.getConfigurator(name);
        if(conf != null)
        {
            IncrementalSystemMetric<U,I> sysMetric  = conf.configure(mpr, trainData, testData, relModel, discModel, cutoff);
            return new Tuple2oo<>(name, sysMetric);
        }
        return null;
    }
    /**
     * Selects and configures an incremental recommendation metric.
     * @param mpr Parameters for the metric.
     * @return A pair containing the name and the selected metric if it exists, null otherwise.
     */
    public Tuple2oo<String, IncrementalMetricFunction<U,I,F>> select(IncrementalRecommendationMetricParamReader mpr)
    {
        String name = mpr.getName();
        IncrementalRecommendationMetricConfigurator<U,I> conf = this.getConfigurator(name);
       
        IncrementalMetricFunction<U,I,F> sysMetric  = conf.configure(mpr);
        return new Tuple2oo<>(name, sysMetric);
    }
}
