/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.metrics.user.individual;

import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.AbstractGlobalSimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.AbstractIndividualSimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Iteration;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.SimpleIteration;
import es.uam.eps.ir.socialnetwork.utils.indexes.GiniIndex;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes the number of pieces of information propagated and seen in all the iterations.
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> type of the user
 * @param <I> type of the information
 * @param <P> type of the parameters
 */
public class UserGini<U extends Serializable,I extends Serializable,P> extends AbstractIndividualSimulationMetric<U,I,P> 
{

    /**
     * Name fixed value
     */
    private final static String GINI = "user-gini";

    /**
     * Speed value
     */
    private final Map<U, Map<U, Double>> counter;
    
    /**
     * Indicates if a piece of information is considered once (or each time it appears if false).
     */
    private final boolean unique;
    
    /**
     * Constructor.
     * @param unique true if a piece of information is considered once, false if it is considered each time it appears.
     */
    public UserGini(boolean unique) 
    {
        super(GINI + "-" + (unique ? "unique" : "repetitions"));
        this.counter = new HashMap<>();
        this.unique = unique;
    }

    @Override
    public void clear() 
    {
        this.counter.clear();
        this.initialized = false;
    }

    @Override
    public double calculate() 
    {
        return this.calculateIndividuals().values().stream().mapToDouble(x->x).average().getAsDouble();
    }

    @Override
    public void update(Iteration<U, I, P> iteration) 
    {
        if(this.isInitialized())
        {
            iteration.getReceivingUsers().forEach(u -> 
            {
                iteration.getSeenInformation(u).forEach(i -> 
                {
                    data.getCreators(i.v1()).forEach(v -> 
                    {
                        this.counter.get(u).put(v, this.counter.get(u).get(v) + (unique ? 1.0 : i.v2().size()));
                    });
                });
            });
            
            if(!unique)
            {
                iteration.getReReceivingUsers().forEach(u -> 
                {
                    iteration.getReReceivedInformation(u).forEach(i -> 
                    {
                        data.getCreators(i.v1()).forEach(v -> 
                        {
                            this.counter.get(u).put(v, this.counter.get(u).get(v) + (unique ? 1.0 : i.v2().size()));
                        });
                    });
                });
            }
        }
    }

    @Override
    protected void initialize() 
    {
        if(!this.isInitialized())
        {
            this.counter.clear();
            Map<U,Double> aux = new HashMap<>();
            this.data.getAllUsers().forEach(u -> aux.put(u, 0.0));
            this.data.getAllUsers().forEach(u -> 
            {
                aux.remove(u);
                this.counter.put(u, aux);
            });
            this.initialized = true;
        }
    }  

    @Override
    public double calculate(U user) 
    {
        if(!this.isInitialized() || !data.containsUser(user))
            return Double.NaN;
        
        GiniIndex gi = new GiniIndex();
        return 1.0 - gi.compute(this.counter.get(user).values().stream(), true);
    }
}
