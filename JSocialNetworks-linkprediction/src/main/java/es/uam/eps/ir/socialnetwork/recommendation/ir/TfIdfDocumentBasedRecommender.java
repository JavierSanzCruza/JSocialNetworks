/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ir;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.CommonNeighborsDocBasedRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;


/**
 * Adaptation of the TF-IDF method of Information Retrieval for user recommendation
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class TfIdfDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{   
    /**
     * Target users' inverse document frequency
     */
    private final Int2DoubleMap uIdf;
    /**
     * Candidate users' inverse document frequency
     */
    private final Int2DoubleMap vIdf;
    /**
     * tf-idf vector modules for each user
     */
    private final Int2DoubleMap mod;

    
    /**
     * Constructor.
     * @param graph The original social network graph.
     * @param uSel Neighborhood selection for the target user.
     * @param vSel Neighborhood selection for the candidate user.
     */
    public TfIdfDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph, uSel, vSel);
        this.uIdf = new Int2DoubleOpenHashMap();
        this.mod = new Int2DoubleOpenHashMap();
        EdgeOrientation uAuxOrient = uSel.invertSelection();
        EdgeOrientation vAuxOrient = vSel.invertSelection();
        
        if(!graph.isDirected() || uSel.equals(vSel))
        {
            this.getAllUidx().forEach(uidx -> 
            {
                uIdf.put(uidx, this.calculateIdf(uidx, uAuxOrient));
            });
            this.vIdf = uIdf;
        }
        else
        {
            this.vIdf = new Int2DoubleOpenHashMap();
            this.getAllUidx().forEach(uidx -> 
            {
                uIdf.put(uidx, this.calculateIdf(uidx, uAuxOrient));
                vIdf.put(uidx, this.calculateIdf(uidx, vAuxOrient));
            });
        }

        this.getAllUidx().forEach(vidx -> 
        {
            double module = graph.getNeighborhoodWeights(vidx, vSel).mapToDouble(widx -> 
            {
                double val = this.calculateTf(widx.v2)*this.vIdf.get(widx.v1);
                return val*val;
            }).sum();
            this.mod.put(vidx, module);
        });
    }

    /**
     * Computes the term frequency of a pair of nodes
     * @param weight the weight.
     * @return the value of the tf
     */
    private double calculateTf(double weight)
    {
        return 1.0 + Math.log(weight)/Math.log(2.0);
    }
    
    /**
     * Compute the inverse document frequency of a node
     * @param uidx the node
     * @param s the orientation of the neighbors
     * @return the value of the idf
     */
    private double calculateIdf(int uidx, EdgeOrientation s)
    {
        double num = this.graph.getNeighborhood(uidx, s).count() + 0.0;
        double val = Math.log(1.0 + ((double) this.numUsers()) / (num + 1.0))/Math.log(2.0);
        return val;
    }
        
    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        double vWeight = this.calculateTf(vW)*this.vIdf.get(vidx);
        double uWeight = this.calculateTf(uW)*this.uIdf.get(uidx);
        
        return uWeight*vWeight;
    }

    @Override
    protected double normalization(int uidx, int vidx, double score) 
    {
        U v = this.uidx2user(vidx);
        double norm = this.mod.get(vidx);
        if(norm == 0.0) return 0.0;
        return score/norm;

    }
}
