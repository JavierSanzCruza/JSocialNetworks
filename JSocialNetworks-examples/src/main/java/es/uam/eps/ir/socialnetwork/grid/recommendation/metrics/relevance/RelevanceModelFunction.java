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
 * Function for retrieving relevance models.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
@FunctionalInterface
public interface RelevanceModelFunction<U,I> 
{
    /**
     * Applies this function to the given arguments.
     * @param trainData Training data
     * @param testData Test data
     * @return a system metric.
     */
    RelevanceModel<U,I> apply(PreferenceData<U,I> trainData, PreferenceData<U,I> testData);
}
