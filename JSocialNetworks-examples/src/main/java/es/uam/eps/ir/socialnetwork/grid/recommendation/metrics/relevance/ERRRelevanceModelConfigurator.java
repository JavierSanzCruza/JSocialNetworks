/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.relevance;

import org.ranksys.core.preference.PreferenceData;
import org.ranksys.metrics.rel.RelevanceModel;
import org.ranksys.novdiv.intentaware.metrics.ERRIA;

/**
 * Configures a relevance model for ERR metric
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * 
 * @see es.uam.eps.ir.ranksys.diversity.intentaware.metrics.ERRIA.ERRRelevanceModel
 * 
 */
public class ERRRelevanceModelConfigurator<U,I> implements RelevanceConfigurator<U,I> 
{
    /**
     * Identifier for the parameter that indicates if the relevance values have to be cached.
     */
    private final static String CACHING = "caching";
    /**
     * Identifier for the relevance threshold.
     */
    private final static String THRESHOLD = "threshold";
    
    @Override
    public RelevanceModel<U, I> configure(RelevanceParamReader params, PreferenceData<U,I> trainData, PreferenceData<U,I> testData) 
    {
        boolean caching = params.getParams().getBooleanValue(CACHING);
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        
        return new ERRIA.ERRRelevanceModel<>(caching, testData, threshold);
    }
    
    @Override
    public RelevanceModelFunction<U, I> configure(RelevanceParamReader params) 
    {
        boolean caching = params.getParams().getBooleanValue(CACHING);
        double threshold = params.getParams().getDoubleValue(THRESHOLD);
        
        return (trainData, testData)->
        {
            return new ERRIA.ERRRelevanceModel<>(caching, testData, threshold);
        };
    }
    
}
