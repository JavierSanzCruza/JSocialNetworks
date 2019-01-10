/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ranksys.metrics.incremental.system;

import java.util.Collection;
import org.ranksys.core.Recommendation;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.metrics.IncrementalSystemMetric;
import org.ranksys.metrics.basic.KCall;
import org.ranksys.metrics.rel.IdealRelevanceModel;

/**
 * Accumulated k-Call. Basically, it sums the number of users which have received,
 * at least, k relevant items in the previous feedback loop iterations.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class IncrementalSystemKCall<U,I> implements IncrementalSystemMetric<U,I> 
{
    /**
     * Accumulated counter.
     */
    private double count;
    /**
     * KCall metric.
     */
    private final KCall kcall;
    
    /**
     * Indicates if external influence has to be removed (currently unused).
     */
    private final boolean removeExt;
    
    /**
     * Test preference data.
     */
    private final PreferenceData<U,I> testData;
    
    /**
     * Constructor.
     * @param testData Test preference data.
     * @param relModel relevance model for the preference data.
     * @param cutoff number of recommended items to consider.
     * @param removeExt indicates if external influence has to be removed (unused)
     * @param k the number of relevant items which must be attained by each user.
     */
    public IncrementalSystemKCall(PreferenceData<U,I> testData, IdealRelevanceModel<U,I> relModel, int cutoff, boolean removeExt, int k)
    {
        this.removeExt = removeExt;
        this.kcall = new KCall(cutoff, k, relModel);
        this.testData = testData;
    }
    
    @Override
    public void add(Recommendation<U, I> rec)
    {
        this.count += this.kcall.evaluate(rec);
    }

    @Override
    public void addExternal(U u, Collection<Tuple2od<I>> external)
    {
        return;
    }

    @Override
    public void reset()
    {
        this.count = 0.0;
    }

    @Override
    public double evaluate()
    {
        return this.count/(testData.numUsers());
    }

    @Override
    public double evaluateExternalEffect()
    {
        return 0.0;
    }

}
