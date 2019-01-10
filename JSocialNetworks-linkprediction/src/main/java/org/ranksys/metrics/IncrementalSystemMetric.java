/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ranksys.metrics;

import java.util.Collection;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;

/**
 * Incremental metric which takes the system as a whole.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public interface IncrementalSystemMetric<U,I>
{
    /**
     * Adds a recommendation to the evaluation.
     * @param rec the recommendation.
     */
    public void add(Recommendation<U,I> rec);
    /**
     * Adds external (user,item) pairs.
     * @param u the user
     * @param external the list of links created towards items.
     */
    public void addExternal(U u, Collection<Tuple2od<I>> external);
    
    /**
     * Reverts the metric to its original configuration.
     */
    public void reset();
    
    /**
     * Obtains the value of the metric.
     * @return the value of the metric.
     */
    public double evaluate();
    
    /**
     * Obtains the effect of external actions on the metric.
     * @return the effect of external actions on the metric.
     */
    public double evaluateExternalEffect();
}
