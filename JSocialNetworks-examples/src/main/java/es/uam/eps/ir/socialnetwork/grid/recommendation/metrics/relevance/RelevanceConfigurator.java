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

/**
 * Configures a relevance model.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public interface RelevanceConfigurator<U,I> 
{
    /**
     * Obtains a function for configuring a relevance model given the training and test datasets.
     * @param params the parameters of the relevance model.
     * @return the function.
     */
    public default RelevanceModelFunction<U,I> configure(RelevanceParamReader params)
    {
        return (trainData, testData) -> this.configure(params, trainData, testData);
    }
    
    /**
     * Configures a relevance model.
     * @param params the parameters of the relevance model.
     * @param trainData training data.
     * @param testData test data.
     * @return the configured relevance model.
     */
    public RelevanceModel<U,I> configure(RelevanceParamReader params, PreferenceData<U,I> trainData, PreferenceData<U,I> testData);
}
