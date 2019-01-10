/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.ranksys.metrics.incremental;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.metrics.IncrementalMetric;
import org.ranksys.metrics.rel.IdealRelevanceModel;

/**
 * Metric that measures the proportion of elements which have been discovered
 * from an initial test set.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> type of the users.
 * @param <I> type of the items.
 */
public class IncrementalRecall<U,I> implements IncrementalMetric<U,I> 
{
    /**
     * Map for checking which items have been previously considered for the user.
     */
    private final Map<U, Set<I>> previouslyAdded;
    /**
     * Relevance model
     */
    private final IdealRelevanceModel<U,I> relModel;
    /**
     * Maximum size of a recommendation
     */
    private final int cutoff;
    /**
     * Map containing the number of relevant items for a user, according to relModel
     */
    private final Map<U, Double> den;
    /**
     * Number of relevant links obtained by an external agent.
     */
    private final Map<U, Double> numExt;
    /**
     * Number of hits of the recommender system
     */
    private final Map<U, Double> numHits;
    /**
     * Do we want to reduce the denominator as an action of the external agents?
     */
    private final boolean reduceExt;

    /**
     * Constructor.
     * @param cutoff    maximum length of recommendation lists
     * @param relModel  relevance model
     * @param reduceExt do we want to reduce the denominator as an action of the external agents?
     */
    public IncrementalRecall(int cutoff, IdealRelevanceModel<U,I> relModel, boolean reduceExt)
    {
        this.cutoff = cutoff;
        this.relModel = relModel;
        this.reduceExt = reduceExt;
        
        den = new HashMap<>();
        numExt = new HashMap<>();
        numHits = new HashMap<>();
        
        this.previouslyAdded = new HashMap<>();
    }
    
    @Override
    public double evaluate(Recommendation<U, I> newRec)
    {
        U user = newRec.getUser();
        IdealRelevanceModel.UserIdealRelevanceModel<U,I> userModel = relModel.getModel(user);

        if(!previouslyAdded.containsKey(user))
        {
            this.previouslyAdded.put(user, new HashSet<>());
            this.den.put(user, userModel.getRelevantItems().size()+0.0);
            this.numExt.put(user, 0.0);
            this.numHits.put(user, 0.0);
        }
        
        List<Tuple2od<I>> recList = newRec.getItems();
        int max = Math.min(recList.size(), cutoff);
        
        double additionalHits = 0.0;
        for(int i = 0; i < max; ++i)
        {
            Tuple2od<I> tuple = recList.get(i);
            if(!previouslyAdded.get(user).contains(tuple.v1) && userModel.isRelevant(tuple.v1))
            {
                this.previouslyAdded.get(user).add(tuple.v1);
                ++additionalHits;
            }
        }
        
        this.numHits.put(user, this.numHits.get(user) + additionalHits);       
        double d = this.den.get(user) - (reduceExt ? this.numExt.get(user) : 0.0);
        if(d > 0.0)
            return this.numHits.get(user) / d;
        return 0.0;
    }
    
    @Override
    public double evaluateExternalEffect(U user, Collection<Tuple2od<I>> external)
    {
        IdealRelevanceModel.UserIdealRelevanceModel<U,I> userModel = relModel.getModel(user);
        
        if(!previouslyAdded.containsKey(user))
        {
            this.previouslyAdded.put(user, new HashSet<>());
            this.den.put(user, userModel.getRelevantItems().size()+0.0);
            this.numExt.put(user, 0.0);
            this.numHits.put(user, 0.0);
        }
        
        double additionalExt = 0.0;
        for(Tuple2od<I> tuple : external)
        {
            if(!previouslyAdded.get(user).contains(tuple.v1) && userModel.isRelevant(tuple.v1))
            {
                this.previouslyAdded.get(user).add(tuple.v1);
                ++additionalExt;
            }
        }
        
        this.numExt.put(user, this.numExt.get(user) + additionalExt);
        double d = this.den.get(user);
        if(d > 0.0)
            return this.numExt.get(user) / d;
        return 0.0;
    }
    
    
    @Override
    public void reset()
    {
        this.previouslyAdded.clear();
        this.den.clear();
        this.numExt.clear();
        this.numHits.clear();
    }
}
