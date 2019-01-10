/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.metrics.incremental.system;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.metrics.IncrementalMetric;
import org.ranksys.metrics.IncrementalSystemMetric;

/**
 * Average of an incremental metric: system incremental metric based on the arithmetic mean of a recommendation metric for a set of users' recommendations.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class AverageIncrementalMetric<U,I> implements IncrementalSystemMetric<U,I>
{
    /**
     * The incremental metric to average over all users.
     */
    private final IncrementalMetric<U,I> incrMetric;
    /**
     * The current set of users
     */
    private final Set<U> users;
    /**
     * The aggregated sum of the values for every user.
     */
    private double sum;
    /**
     * The aggregated sum for the external effect
     */
    private double sumExt;
    /**
     * The number of users.
     */
    private double numUsers;
    /**
     * Is the number of users previously set?
     */
    private final boolean allUsers;
    /**
     * Previous metric values for the different users.
     */
    private final Map<U, Double> oldvalues;
    /**
     * Previous external action effects on the metric for the different users.
     */
    private final Map<U, Double> oldExtValues;
       
    /**
     * Constructor in which the number of users of the recommendation metric to be averaged is specified
     * @param incrMetric the incremental metric to be averaged
     * @param numUsers the number of expected users' recommendations
     */
    public AverageIncrementalMetric(IncrementalMetric<U,I> incrMetric, int numUsers)
    {
        this.incrMetric = incrMetric;
        this.sum = 0;
        this.sumExt = 0;
        this.numUsers = 0;
        this.allUsers = true;
        this.users = new HashSet<>();
        this.oldvalues = new HashMap<>();
        this.oldExtValues = new HashMap<>();
    }
    
    /**
     * Constructor in which the average is calculated for all the recommendations added during the calculation
     * @param incrMetric the incremental metric to be averaged
     */
    public AverageIncrementalMetric(IncrementalMetric<U,I> incrMetric)
    {
        this.incrMetric = incrMetric;
        this.sum = 0;
        this.numUsers = 0;
        this.allUsers = false;
        this.users = new HashSet<>();
        this.oldvalues = new HashMap<>();
        this.oldExtValues = new HashMap<>();
    }

    
    @Override
    public void add(Recommendation<U,I> rec)
    {
        U u = rec.getUser();
        if(!this.users.contains(u))
        {
            this.users.add(u);
            this.oldvalues.put(u, 0.0);
            this.oldExtValues.put(u,0.0);
            if(!allUsers) ++this.numUsers;
        }
        
        double value = this.incrMetric.evaluate(rec);
        
        this.sum = this.sum + value - this.oldvalues.get(u);
        this.oldvalues.put(u,value);
        
    }
    
    @Override
    public void addExternal(U u, Collection<Tuple2od<I>> external)
    {
        if(!this.users.contains(u))
        {
            this.users.add(u);
            this.oldvalues.put(u, 0.0);
            this.oldExtValues.put(u,0.0);
            if(!allUsers) ++this.numUsers;
        }
        
        double value = this.incrMetric.evaluateExternalEffect(u, external);
        this.sumExt = this.sumExt + value - this.oldExtValues.get(u);
        this.oldExtValues.put(u, value);
        
        // Reevaluate the value of the metric.
        double reeval = this.incrMetric.evaluate(new Recommendation<>(u, new ArrayList<>()));
        this.sum = this.sum + reeval - this.oldvalues.get(u);
        this.oldvalues.put(u,reeval);
    }
    
    @Override
    public void reset()
    {
        this.incrMetric.reset();
        this.oldvalues.clear();
        this.oldExtValues.clear();
        if(!allUsers) numUsers = 0.0;
        this.sum = 0.0;
        this.sumExt = 0.0;
        this.users.clear();
    }

    @Override
    public double evaluate()
    {
        if(this.numUsers == 0.0)
        {
            return 0.0;
        }
        return this.sum / this.numUsers;
    }

    @Override
    public double evaluateExternalEffect()
    {
        if(this.numUsers == 0.0)
        {
            return 0.0;
        }
        return this.sumExt / this.numUsers;
    }
}
