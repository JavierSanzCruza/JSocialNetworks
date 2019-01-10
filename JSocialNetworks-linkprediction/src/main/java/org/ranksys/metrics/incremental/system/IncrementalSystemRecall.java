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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ranksys.core.Recommendation;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.metrics.IncrementalSystemMetric;
import org.ranksys.metrics.rel.IdealRelevanceModel;
import org.ranksys.metrics.rel.IdealRelevanceModel.UserIdealRelevanceModel;

/**
 * Metric that measures the system recall.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class IncrementalSystemRecall<U,I> implements IncrementalSystemMetric<U,I>
{
    /**
     * Ideal relevance model
     */
    private final IdealRelevanceModel<U,I> relModel;
    /**
     * Maximum length of the recommendation ranking
     */
    private final int cutoff;
    /**
     * Total number of relevant (user,item) pairs.
     */
    private final int totalRelevant;
    /**
     * Number of relevant (user, item) pairs produced by an external action.
     */
    private int numExternal;
    /**
     * Number of relevant (user, item) pairs produced by recommendation.
     */
    private int numHits;
    /**
     * Should we remove the number of externally produced pairs from the denominator count?
     */
    private final boolean removeExt;
    
    /**
     * Map for checking which items have been previously considered for the user.
     */
    private final Map<U, Set<I>> previouslyAdded;
    
    /**
     * Constructor.
     * @param testData test preferences.
     * @param relModel relevance model.
     * @param cutoff maximum length of the recommendation lists.
     * @param removeExt should we remove the number of externally created links from the denominator?
     */
    public IncrementalSystemRecall(PreferenceData<U,I> testData, IdealRelevanceModel<U,I> relModel, int cutoff, boolean removeExt)
    {
        this.relModel = relModel;
        this.cutoff = cutoff;
        this.totalRelevant = testData.getAllUsers().mapToInt(u -> 
        {
           UserIdealRelevanceModel<U,I> userModel = relModel.getModel(u);
           return testData.getUserPreferences(u).mapToInt(i -> userModel.isRelevant(i.v1) ? 1 : 0).sum();
        }).sum();
        this.numExternal = 0;
        this.numHits = 0;
        this.removeExt = removeExt;
        this.previouslyAdded = new HashMap<>();
    }
    
    @Override
    public void add(Recommendation<U,I> rec)
    {
        U user = rec.getUser();
        IdealRelevanceModel.UserIdealRelevanceModel<U,I> userModel = relModel.getModel(user);
        
        if(!this.previouslyAdded.containsKey(user))
        {
            this.previouslyAdded.put(user, new HashSet<>());
        }
        
        int additionalHits = 0;
        List<Tuple2od<I>> list = rec.getItems();
        int max = Math.min(cutoff, list.size());
        for(int i = 0; i < max; ++i)
        {
            I item = list.get(i).v1();
            if(!this.previouslyAdded.get(user).contains(item) && userModel.isRelevant(item))
            {
                this.previouslyAdded.get(user).add(item);
                ++additionalHits;
            }
        }
        
        this.numHits += additionalHits;
    }
    
    @Override
    public void addExternal(U user, Collection<Tuple2od<I>> external)
    {
        IdealRelevanceModel.UserIdealRelevanceModel<U,I> userModel = relModel.getModel(user);
        if(!this.previouslyAdded.containsKey(user))
        {
            this.previouslyAdded.put(user, new HashSet<>());
        }
        
        int additionalExt = 0;
        for(Tuple2od<I> tuple : external)
        {
            I item = tuple.v1;
            if(!this.previouslyAdded.get(user).contains(item) && userModel.isRelevant(item))
            {
                this.previouslyAdded.get(user).add(item);
                ++additionalExt;
            }
        }
        
        this.numExternal += additionalExt;
    }
    
    @Override
    public double evaluate()
    {
        double den = this.totalRelevant - (this.removeExt ? this.numExternal : 0.0);
        if(den == 0.0) return 0.0;
        return (this.numHits +0.0)/ (den + 0.0);
    }
    
    @Override
    public double evaluateExternalEffect()
    {
        if(this.totalRelevant > 0)
            return (this.numExternal+0.0) / (this.totalRelevant+0.0);
        return 0.0;
    }
    

    @Override
    public void reset()
    {
        this.previouslyAdded.clear();
        this.numExternal = 0;
        this.numHits = 0;
    }
}
