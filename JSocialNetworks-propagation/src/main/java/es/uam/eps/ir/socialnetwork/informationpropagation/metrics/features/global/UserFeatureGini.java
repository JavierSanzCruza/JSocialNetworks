/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.global;

import es.uam.eps.ir.socialnetwork.informationpropagation.metrics.features.AbstractFeatureGlobalSimulationMetric;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Iteration;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

/**
 * Computes the Gini coefficient complement over the set of pairs (user, hashtag).
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 */
public class UserFeatureGini<U extends Serializable,I extends Serializable,P> extends AbstractFeatureGlobalSimulationMetric<U,I,P>
{
    /**
    * Metric name
    */
    private final static String GINI = "user-feature-gini";

    /**
     * Sum of the frequencies
     */
    private double freqsum = 0.0;
    
    /**
     * Main sum for the Gini coefficient.
     */
    private double mainsum = 0.0;
    /**
     * Total number of pairs.
     */
    private long size = 0;
    /**
     * Map for storing the frequency of the user/feature pairs.
     */
    private final Map<U, Map<P, Double>> frequencies;
    /**
     * Auxiliar map for computing Gini coefficient (contains the minimum position for each frequency).
     */
    private final Map<Double, Long> minimumPos;
    /**
     * Auxiliar map for computing Gini coefficient (contains the maximum position for each frequency).
     */
    private final Map<Double, Long> maximumPos;
    /**
     * Auxiliar set containing the possible values
     */
    private final TreeSet<Double> values;
    
    /**
     * Indicates if a piece of information is considered once (or each time it appears if false).
     */
    private final boolean unique;
    
    /**
     * Constructor.
     * @param userparam true if we are using a user parameter, false if we are using an information piece parameter.
     * @param parameter the name of the parameter.
     * @param unique true if a piece of information is considered once, false if it is considered each time it appears.
     */
    public UserFeatureGini(String parameter,boolean userparam, boolean unique) 
    {
        super(GINI + "-" + (userparam ? "user" : "info") + "-" + parameter + "-" + (unique ? "unique" : "repetitions"), userparam, parameter);
        
        this.frequencies = new HashMap<>();
        this.minimumPos = new HashMap<>();
        this.maximumPos = new HashMap<>();
        this.values = new TreeSet<>();
        this.unique = unique;
    }

    @Override
    protected void updateUserParam(Iteration<U, I, P> iteration) 
    {
        if(iteration == null) return;
        
        iteration.getReceivingUsers().forEach(u -> 
        {
            iteration.getSeenInformation(u).forEach(i -> 
            {
                double val = (unique ? 1.0 : i.v2().size());
                data.getCreators(i.v1()).forEach(creator -> 
                {
                    data.getUserFeatures(creator, this.getParameter()).forEach(p -> 
                    {
                        double oldfreq = frequencies.get(u).get(p.v1);
                        double newfreq = oldfreq + p.v2*val;

                        double increase = this.increaseOld(oldfreq);
                        increase += this.increaseNew(newfreq);
                        
                        Double cursor = oldfreq;
                        do
                        {
                            cursor = this.values.higher(cursor);
                            if(cursor != null && cursor < newfreq)
                            {
                                increase += this.increaseIntermediate(cursor);
                                
                            }
                        }
                        while(cursor != null && cursor < newfreq);
                        
                        this.frequencies.get(u).put(p.v1, newfreq);
                        
                        this.freqsum += p.v2;
                        this.mainsum += increase;
                    });
                });
            });
            
            if(!unique)
            {
                iteration.getReReceivedInformation(u).forEach(i -> 
                {
                    double val = i.v2().size();
                    data.getCreators(i.v1()).forEach(creator -> 
                    {
                        data.getUserFeatures(creator, this.getParameter()).forEach(p -> 
                        {
                            double oldfreq = frequencies.get(u).get(p.v1);
                            double newfreq = oldfreq + p.v2*val;

                            double increase = this.increaseOld(oldfreq);
                            increase += this.increaseNew(newfreq);

                            Double cursor = oldfreq;
                            do
                            {
                                cursor = this.values.higher(cursor);
                                if(cursor != null && cursor < newfreq)
                                {
                                    increase += this.increaseIntermediate(cursor);

                                }
                            }
                            while(cursor != null && cursor < newfreq);

                            this.frequencies.get(u).put(p.v1, newfreq);

                            this.freqsum += p.v2;
                            this.mainsum += increase;
                        });
                    });
                });
            }
        });
    }

    @Override
    protected void updateInfoParam(Iteration<U, I, P> iteration) 
    {
        if(iteration == null) return;
        
        iteration.getReceivingUsers().forEach(u -> 
        {
            iteration.getSeenInformation(u).forEach(i -> 
            {
                double val = (unique ? 1.0 : i.v2().size());
                data.getInfoPiecesFeatures(i.v1(), this.getParameter()).forEach(p -> 
                {
                    double oldfreq = frequencies.get(u).get(p.v1);
                    double newfreq = oldfreq + p.v2*val;
                    double increase = this.increaseOld(oldfreq);

                    Double cursor = oldfreq;
                    do
                    {
                        cursor = this.values.higher(cursor);
                        if(cursor != null && cursor < newfreq)
                        {
                            increase += this.increaseIntermediate(cursor);

                        }
                    }
                    while(cursor != null && cursor < newfreq);
                    
                    increase += this.increaseNew(newfreq);

                    

                    this.frequencies.get(u).put(p.v1, newfreq);

                    this.freqsum += p.v2;
                    this.mainsum += increase;
                });
            });
            
            if(!unique)
            {
                iteration.getReReceivedInformation(u).forEach(i -> 
                {
                    double val = i.v2().size();
                    data.getInfoPiecesFeatures(i.v1(), this.getParameter()).forEach(p -> 
                    {
                        double oldfreq = frequencies.get(u).get(p.v1);
                        double newfreq = oldfreq + p.v2*val;
                        double increase = this.increaseOld(oldfreq);

                        Double cursor = oldfreq;
                        do
                        {
                            cursor = this.values.higher(cursor);
                            if(cursor != null && cursor < newfreq)
                            {
                                increase += this.increaseIntermediate(cursor);

                            }
                        }
                        while(cursor != null && cursor < newfreq);

                        increase += this.increaseNew(newfreq);



                        this.frequencies.get(u).put(p.v1, newfreq);

                        this.freqsum += p.v2;
                        this.mainsum += increase;
                    });
                });
            }
        });
    }

    @Override
    protected void initialize() 
    {
        if(this.initialized == false && this.data.doesFeatureExist(this.getParameter()) && this.data.isUserFeature(this.getParameter()) == this.usesUserParam())
        {
            this.frequencies.clear();
            this.minimumPos.clear();
            this.maximumPos.clear();
            this.values.clear();
            
            this.size = this.data.getAllUsers().count()*this.data.getAllFeatureValues(this.getParameter()).count();

            // Initializing the map for storing the frequencies for pairs (u,h).
            this.data.getAllUsers().forEach(u -> 
            {
                Map<P, Double> feats = new HashMap<>();
                this.data.getAllFeatureValues(this.getParameter()).forEach(feat ->
                {
                    feats.put(feat, 0.0);
                });
                this.frequencies.put(u, feats);
                this.freqsum = 0.0;
                this.mainsum = 0.0;
            });
            
            // At first, no one has elements.
            this.minimumPos.put(0.0, 1L);
            this.maximumPos.put(0.0, this.size);
            this.values.add(0.0);
            this.initialized = true;
        }
    }

    @Override
    public double calculate() 
    {
        if(this.freqsum == 0.0 || this.size <= 1L)
            return Double.NaN;
        else
            return 1.0 - this.mainsum/(this.freqsum * (this.size - 1.0));
    }

    @Override
    public void clear() 
    {
        if(this.initialized == true)
        {
            this.frequencies.clear();
            this.minimumPos.clear();
            this.maximumPos.clear();
            this.values.clear();
            this.mainsum = 0.0;
            this.freqsum = 0.0;
            this.initialized = false;
        }
    }

    
    /**
     * Computes the increment for old frequencies.
     * @param oldfreq old frequency.
     * @return the variation for the old frequency.
     */
    private double increaseOld(double oldfreq) 
    {
        long oldMax = maximumPos.get(oldfreq);
        long oldMin = minimumPos.get(oldfreq);    
        

        if(oldMax <= oldMin) //It should only be greater or equal, but we add < for safety
        {
            maximumPos.remove(oldfreq);
            minimumPos.remove(oldfreq);
            this.values.remove(oldfreq);
        }
        else
        {
            maximumPos.put(oldfreq, oldMax-1);
        }
        
        return oldfreq*(this.size + 1 - 2*oldMax);

        
    }
    
    /**
     * Computes the increment for new frequencies.
     * @param newfreq new frequency.
     * @return the variation for the new frequency.
     */
    private double increaseNew(double newfreq)
    {
        double increase;
        // Modify the corresponding values for the new frequency
        if(maximumPos.containsKey(newfreq))
        {
            long newMin = minimumPos.get(newfreq);

            increase = newfreq*(2*newMin - this.size - 3);

            minimumPos.put(newfreq, newMin - 1);
        }
        else
        {
            double prev = this.values.lower(newfreq);

            long newMax = this.maximumPos.get(prev) + 1;
            long newMin = newMax;

            increase = newfreq*(2*newMax - this.size - 1);

            minimumPos.put(newfreq, newMin);
            maximumPos.put(newfreq, newMax);
            this.values.add(newfreq);
        }
        
        return increase;
    }
    
    /**
     * Computes the increment for frequencies between old and new.
     * @param freq new frequency.
     * @return the variation for the new frequency.
     */
    private double increaseIntermediate(double freq)
    {
        long intMax = maximumPos.get(freq);
        long intMin = minimumPos.get(freq);

        maximumPos.put(freq, intMax-1);
        minimumPos.put(freq, intMin-1);
        
        return freq * (2*intMin - 2*intMax - 2);
    }
    
}
