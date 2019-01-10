/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics;

import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.sales.GiniSimpsonConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.sales.GiniConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.sales.EIURDConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.sales.IUDConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.sales.EntropyConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.sales.EIUDCConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.sales.AggregateDiversityConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.AlphaNDCGConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.ERRIAConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.SubtopicRecallConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.EILDConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.novelty.EPDConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.novelty.EPCConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.novelty.EFDConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.ERRConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.KCallConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.RecallConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.ReciprocalRankConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.AveragePrecisionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.PrecisionConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.NDCGConfigurator;
import static es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.RecommendationMetricIdentifiers.*;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.accuracy.CoverageConfigurator;
import es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics.diversity.ModEILDConfigurator;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import org.ranksys.core.feature.FeatureData;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.metrics.rank.RankingDiscountModel;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Class that selects an individual recommendation metric.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * @param <F> Type of the features.
 */
public class RecommendationMetricSelector<U,I,F> 
{  
    /**
     * Obtains a configurator for a metric.
     * @param metric the name of the metric.
     * @return the configurator for the metric if it exists, null otherwise.
     */
    public RecommendationMetricConfigurator<U,I> getConfigurator(String metric)
    {
        RecommendationMetricConfigurator<U,I> conf;
        switch(metric)
        {
            // Accuracy metrics
            case PRECISION:
                conf = new PrecisionConfigurator<>();
                break;
            case RECALL:
                conf = new RecallConfigurator<>();
                break;
            case NDCG:
                conf = new NDCGConfigurator<>();
                break;
            case RR:
                conf = new ReciprocalRankConfigurator<>();
                break;
            case ERR:
                conf = new ERRConfigurator<>();
                break;
            case AVERPREC:
                conf = new AveragePrecisionConfigurator<>();
                break;
            case KCALL:
                conf = new KCallConfigurator<>();
                break;
            case COVER:
                conf = new CoverageConfigurator<>();
                break;
            // Novelty metrics
            case EFD:
                conf = new EFDConfigurator<>();
                break;
            case EPC:
                conf = new EPCConfigurator<>();
                break;
            case EPD:
                conf = new EPDConfigurator<>();
                break;
            // Diversity metrics
            case EILD:
                conf = new EILDConfigurator<>();
                break;
            case MODEILD:
                conf = new ModEILDConfigurator<>();
                break;
            case ALPHANDCG:
                conf = new AlphaNDCGConfigurator<>();
                break;
            case ERRIA:
                conf = new ERRIAConfigurator<>();
                break;
            case SRECALL:
                conf = new SubtopicRecallConfigurator<>();
                break;
            // Sales diversity metrics
            case AGGRDIV:
                conf = new AggregateDiversityConfigurator<>();
                break;
            case EIUDC:
                conf = new EIUDCConfigurator<>();
                break;
            case EIURD:
                conf = new EIURDConfigurator<>();
                break;
            case ENTROPY:
                conf = new EntropyConfigurator<>();
                break;
            case GINI:
                conf = new GiniConfigurator<>();
                break;
            case GINISIMPSON:
                conf = new GiniSimpsonConfigurator<>();
                break;
            case IUD:
                conf = new IUDConfigurator<>();
                break;
            default:
                return null;
        }
        
        return conf;
    }
    
    /**
     * Selects and configures a recommendation metric.
     * @param mpr the parameters for the recommendation metric.
     * @param trainData training data.
     * @param testData test data.
     * @param featureData feature data of the different items.
     * @param relModel indicates whether a tuple (user, item) is relevant or not.
     * @param discModel indicates how the position in the ranking affects the evaluation of the metric.
     * @param cutoff maximum number of elements in the ranking to consider.
     * @return a tuple containing the name of the metric and the metric if the metric exists, null otherwise.
     */
    public Tuple2oo<String, SystemMetric<U,I>> select(RecommendationMetricParamReader mpr, PreferenceData<U,I> trainData, PreferenceData<U,I> testData, FeatureData<I,F,Double> featureData, RelevanceModel<U,I> relModel, RankingDiscountModel discModel, int cutoff)
    {
        String name = mpr.getName();
        RecommendationMetricConfigurator<U,I> conf = this.getConfigurator(name);
        if(conf != null)
        {
            SystemMetric<U,I> sysMetric  = conf.configure(mpr, trainData, testData, featureData, relModel, discModel, cutoff);
            return new Tuple2oo<>(name, sysMetric);
        }
        return null;
    }
    /**
     * Selects and configures a recommendation metric.
     * @param mpr Parameters for the metric.
     * @return A pair containing the name and the selected metric if it exists, null otherwise.
     */
    public Tuple2oo<String, MetricFunction<U,I,F>> select(RecommendationMetricParamReader mpr)
    {
        String name = mpr.getName();
        RecommendationMetricConfigurator<U,I> conf = this.getConfigurator(name);
       
        MetricFunction<U,I,F> sysMetric  = conf.configure(mpr);
        return new Tuple2oo<>(name, sysMetric);
    }
}
