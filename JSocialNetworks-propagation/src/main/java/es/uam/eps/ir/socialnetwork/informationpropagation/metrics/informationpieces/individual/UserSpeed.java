/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.metrics.informationpieces.individual;

import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.AbstractIndividualSimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Iteration;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Computes the number of information pieces received by each user in the network.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the user
 * @param <I> type of the information
 * @param <P> type of the parameters
 */
public class UserSpeed<U extends Serializable,I extends Serializable,P> extends AbstractIndividualSimulationMetric<U,I,P> 
{
    /**
     * Name fixed value
     */
    private final static String SPEED = "numinfo";

    /**
     * Speed value
     */
    private final Map<U, Double> speed;

    /**
     * Constructor.
     */
    public UserSpeed() 
    {
        super(SPEED);
        this.speed = new HashMap<>();
    }   

    @Override
    public void clear() 
    {
        this.speed.clear();
        this.initialized = false;
    }

    @Override
    public double calculate() 
    {
        return data.getAllUsers().mapToDouble(u -> this.speed.get(u)).average().getAsDouble();
    }

    @Override
    public void update(Iteration<U, I, P> iteration) 
    {
        iteration.getReceivingUsers().forEach(u -> 
        {
            this.speed.put(u, this.speed.get(u) + iteration.getNumUniqueSeen(u));
        });
    }

    @Override
    protected void initialize() 
    {
        if(!this.isInitialized() && this.data != null)
        {
            this.speed.clear();
            data.getAllUsers().forEach(u -> speed.put(u, 0.0));
            this.initialized = true;
        }
    }

    @Override
    public double calculate(U user) 
    {
        if(this.isInitialized() && data.containsUser(user))
            return this.speed.get(user);
        return Double.NaN;
    }

    
}
