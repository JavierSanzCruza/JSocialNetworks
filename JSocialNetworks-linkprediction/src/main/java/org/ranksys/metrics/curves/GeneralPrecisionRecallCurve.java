/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.metrics.curves;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import it.unimi.dsi.fastutil.doubles.Double2DoubleMap;
import it.unimi.dsi.fastutil.doubles.Double2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import org.ranksys.metrics.rel.BinaryRelevanceModel;

/**
 * Computes the Precision-Recall curve of a recommender.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class GeneralPrecisionRecallCurve<U>
{
    /**
     * Binary relevance model, for establishing which elements are relevant and which not.
     */
    private final BinaryRelevanceModel binRel;
    /**
     * Number of relevant items
     */
    private final int numRel;
    
    /**
     * Constructor
     * @param graph The graph.
     * @param binRel Binary relevance model which establishes the elements relevant to each user.
     */
    public GeneralPrecisionRecallCurve(Graph<U> graph, BinaryRelevanceModel<U, U> binRel)
    {
       this.binRel = binRel;    
       this.numRel = graph.getAllNodes().mapToInt(u -> binRel.getModel(u).getRelevantItems().size()).sum();
    }
    
    /**
     * Computes the Precision-Recall curve
     * @param res recommendation ranking
     * @return a map containing the curve.
     */
    public Double2DoubleMap getCurve(List<Pair<U>> res)
    {
        List<Double> precisions = new ArrayList<>();
        int numRels = 0;
        Double2DoubleMap curve = new Double2DoubleOpenHashMap();
        List<Integer> kr = new ArrayList<>();
        
        double currentR = 0.1;
        double maxp = 0.0;
        // Compute P@(i+1) and R@(i+1) for all.
        for(int i = 0; i < res.size(); ++i)
        {
            Pair<U> pair = res.get(i);
            U u = pair.v1();
            U v = pair.v2();
            
            if(binRel.getModel(u).isRelevant(v))
            {
                numRels++;
            }
            
            double p = (numRels+0.0)/(i+1.0);
            double r = (numRels + 0.0)/(this.numRel + 0.0);
            precisions.add(p);
            
            if(p > maxp)
            {
                maxp = p;
            }
            
            if(r > currentR)
            {
                currentR += 0.1;
                kr.add(i);
            }
        }       
        
        int currentKrIndex = kr.size() - 1;
        int currentKr = kr.get(currentKrIndex);
        double currentmaxp = 0.0;
        for(int i = precisions.size(); i >= kr.get(0); ++i)
        {
            if(precisions.get(i) > currentmaxp)
            {
                currentmaxp = precisions.get(i);
            }
            
            if(i < currentKr)
            {
                curve.put(currentR, currentmaxp);
                currentR -= 0.1;
                currentKrIndex--;
                currentKr = kr.get(currentKrIndex);
            }
        }
        curve.put(0.0, maxp);

        return curve;
        
    }
}
