/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.utils.indexes;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Computes the KL divergence as a distance between two distributions (an original one, P(x), and an estimated
 * one from real data Q(x). By definition, the Kullback-Leibler divergence can only be computed if Q(x) = 0
 * implies that P(x) = 0.
 * 
 * Kullback, S., Leibler, R.A. On Information and Sufficiency. Annals. Math. Stats 22(1), 1951
 * 
 * @author Javier Sanz-Cruzado Puig
 */
public class KLDivergence 
{
    /**
     * Computes the Kullback-Leibler divergence for two different distributions
     * @param p the distribution
     * @param q proxy to the real distribution (i.e. the distribution we estimate from data)
     * @return the value of the Kullback-Leibler divergence
     */
    public double compute(List<Double> p, List<Double> q)
    {
        double sumP = p.stream().mapToDouble(x -> x).sum();
        double sumQ = q.stream().mapToDouble(y -> y).sum();
        
        return this.compute(p, q, sumP, sumQ);
    }
    
    /**
     * Computes the Kullback-Leibler divergence for two different distributions
     * @param p the distribution
     * @param q proxy to the real distribution (i.e. the distribution we estimate from data)
     * @param sumP the sum of the values in P
     * @param sumQ the sum of the values in Q
     * @return the value of the Kullback-Leibler divergence
     */
    public double compute(List<Double> p, List<Double> q, double sumP, double sumQ)
    {
        if(p.size() != q.size())
        {
            return Double.NaN;
        }
        
        int size = p.size();
        double kldiv = 0.0;
        for(int i = 0; i < size; ++i)
        {
            double pval = p.get(i) / sumP;
            double qval = q.get(i) / sumQ;
            
            if(pval != 0.0)
            {
                kldiv += pval*Math.log(pval/qval)/Math.log(2.0);
            }
        }
        return kldiv;
    }
    
    /**
     * Computes the Kullback-Leibler divergence for two different distributions
     * @param p the distribution
     * @param q proxy to the real distribution (i.e. the distribution we estimate from data)
     * @return the value of the Kullback-Leibler divergence
     */
    public double compute(Stream<Double> p, Stream<Double> q)
    {
        return this.compute(p.collect(Collectors.toCollection(() -> new ArrayList<>())), q.collect(Collectors.toCollection(ArrayList::new)));
    }
    
    /**
     * Computes the Kullback-Leibler divergence for two different distributions
     * @param p the distribution
     * @param q proxy to the real distribution (i.e. the distribution we estimate from data)
     * @param sumP the sum of the values in P
     * @param sumQ the sum of the values in Q
     * @return the value of the Kullback-Leibler divergence
     */
    public double compute(Stream<Double> p, Stream<Double> q, double sumP, double sumQ)
    {
        return this.compute(p.collect(Collectors.toCollection(() -> new ArrayList<>())), q.collect(Collectors.toCollection(ArrayList::new)), sumP, sumQ);
    }
}
