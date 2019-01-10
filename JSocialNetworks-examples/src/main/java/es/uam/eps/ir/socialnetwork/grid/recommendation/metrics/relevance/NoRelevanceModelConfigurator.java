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
import org.ranksys.metrics.rel.NoRelevanceModel;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Configures a relevance model where every item is relevant.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 * 
 * @see es.uam.eps.ir.ranksys.metrics.rel.NoRelevanceModel
 * 
 */
public class NoRelevanceModelConfigurator<U,I> implements RelevanceConfigurator<U,I> 
{
    /**
     * Constructor.
     */
    public NoRelevanceModelConfigurator()
    {
    }

    @Override
    public RelevanceModel<U, I> configure(RelevanceParamReader params, PreferenceData<U, I> trainData, PreferenceData<U, I> testData)
    {
        return new NoRelevanceModel<>();
    }
    
}
