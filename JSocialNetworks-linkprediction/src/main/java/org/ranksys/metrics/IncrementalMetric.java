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
 * Definition of a metric which changes over time.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public interface IncrementalMetric<U,I>
{
    /**
     * Updates the value of the metric.
     * @param newRec new recommended items.
     * @return the value of evaluating
     */
    public double evaluate(Recommendation<U,I> newRec);
    
    /**
     * Updates and computes the effect of external actions on the system.
     * @param user the user.
     * @param externalAction relations between users and items created by some external action.
     * @return the effect of external action on the metric.
     */
    public double evaluateExternalEffect(U user, Collection<Tuple2od<I>> externalAction);
    
    /**
     * Resets everything
     */
    public void reset();
}
