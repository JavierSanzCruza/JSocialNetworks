/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.relevance;

import static es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.relevance.RelevanceIdentifiers.*;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Class that selects an individual relevance model.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class RelevanceSelector<U,I> 
{     
    /**
     * Obtains a relevance configurator.
     * @param name the name of the relevance model.
     * @return the configurator for the relevance model if exists, null otherwise
     */
    public RelevanceConfigurator<U,I> getConfigurator(String name)
    {
        RelevanceConfigurator<U,I> conf;
        switch(name)
        {
            case BACKGROUND:
                conf = new BackgroundBinaryRelevanceModelConfigurator<>();
                break;
            case BINARY:
                conf = new BinaryRelevanceModelConfigurator<>();
                break;
            case ERR:
                conf = new ERRRelevanceModelConfigurator<>();
                break;
            case NDCG:
                conf = new NDCGRelevanceModelConfigurator<>();
                break;
            case NOREL:
                conf = new NoRelevanceModelConfigurator<>();
                break;
            default:
                return null;
        }
        
        return conf;
    }
    
    /**
     * Selects and configures a selection mechanism.
     * @param spr Parameters for the selection mechanism.
     * @param trainData Training data.
     * @param testData Test data.
     * @return A pair containing the name and the selected selection mechanism.
     */
    public Tuple2oo<String, RelevanceModel<U,I>> select(RelevanceParamReader spr, PreferenceData<U,I> trainData, PreferenceData<U,I> testData)
    {
        String name = spr.getName();
        RelevanceConfigurator<U,I> conf = this.getConfigurator(name);
        RelevanceModel<U,I> relevance = conf.configure(spr, trainData, testData);
        return new Tuple2oo<>(name, relevance);
    }
    
    /**
     * Selects and configures a selection mechanism.
     * @param spr Parameters for the selection mechanism.
     * @return A pair containing the name and the selected selection mechanism.
     */
    public Tuple2oo<String, RelevanceModelFunction<U,I>> select(RelevanceParamReader spr)
    {
        String name = spr.getName();
        RelevanceConfigurator<U,I> conf = this.getConfigurator(name);
        RelevanceModelFunction<U,I> relevance = conf.configure(spr);
        return new Tuple2oo<>(name, relevance);
    }
}
