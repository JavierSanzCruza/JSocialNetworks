/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.indiv;

import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.AbstractFeatureIndividualSimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Iteration;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Computes the number of pieces of information propagated and seen in all the iterations.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the user
 * @param <I> type of the information
 * @param <P> type of the parameters
 */
public class FeatureRecall<U extends Serializable,I extends Serializable,P> extends AbstractFeatureIndividualSimulationMetric<U,I,P> 
{
    /**
     * Name fixed value
     */
    private final static String RECALL = "feat-recall";

    /**
     * Stores (if it is necessary), a relation between users and parameters.
     */
    private final Map<U, Set<P>> recParams;

    /**
     * The total number of external parameters that have reached the different users
     */
    private double total;    
    
    /**
     * Constructor.
     * @param userparam true if we are using a user parameter, false if we are using an information piece parameter.
     * @param parameter the name of the parameter.
     */
    public FeatureRecall(String parameter, boolean userparam) 
    {
        super(RECALL + "-" + (userparam ? "user" : "info") + "-" + parameter, userparam, parameter);
        this.recParams = new HashMap<>();
    }

    @Override
    public void clear() 
    {
        this.total = 0.0;
        this.recParams.clear();
        this.initialized = false;
    }

    @Override
    public double calculate() 
    {
        return this.calculateIndividuals().entrySet().stream().mapToDouble(entry -> entry.getValue()).average().getAsDouble();
    }

    /**
     * Updates the necessary values for computing the metric (when using user parameters).
     * @param iteration the iteration data.
     */
    @Override
    protected void updateUserParam(Iteration<U,I,P> iteration)
    {
        if(iteration == null) return;
        
        // For each user that has received at least a new piece of information
        iteration.getReceivingUsers().forEach(u -> 
        {
            // Get all his received pieces
            iteration.getSeenInformation(u).forEach(i -> 
            {
                // and its creators.
                data.getCreators(i.v1()).forEach(creator -> 
                {
                    // Identify the parameters of the creators.
                    data.getUserFeatures(creator, this.getParameter()).forEach(p -> 
                    {
                        this.recParams.get(u).add(p.v1);
                    });
                });
                
            });
        });
    }
    
    /**
     * Updates the necessary values for computing the metric (when using user parameters).
     * @param iteration the iteration data.
     */
    @Override
    protected void updateInfoParam(Iteration<U,I,P> iteration)
    {
        if(iteration == null) return;
        
        // For each user that has received at least a new piece of information
        iteration.getReceivingUsers().forEach(u -> 
        {
            // Get all his received pieces
            iteration.getSeenInformation(u).forEach(i -> 
            {
                // Identify its parameters.
                data.getInfoPiecesFeatures(i.v1(), this.getParameter()).forEach(p -> 
                {
                    this.recParams.get(u).add(p.v1);
                });
            });
            

        });
    }

    @Override
    protected void initialize() 
    {
        if(!this.isInitialized())
        {
            this.recParams.clear();
            data.getAllUsers().forEach(u -> 
            {
                this.recParams.put(u, new HashSet<>());
            });
            this.total = data.numFeatureValues(this.getParameter());
            this.initialized = data.doesFeatureExist(this.getParameter());
        }
        
    }    

    @Override
    public double calculate(U user) 
    {
        if(!this.isInitialized())
            return Double.NaN;
        
        if(!this.data.containsUser(user)) return Double.NaN;
        
        if(this.total > 0.0)
            return this.recParams.get(user).size() / (total);
        else
            return 0.0;
    }
}
