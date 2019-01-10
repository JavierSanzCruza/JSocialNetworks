/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.metrics.curves;

import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.util.ArrayList;
import java.util.List;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.metrics.rel.IdealRelevanceModel;

/**
 * Returns the ROC curve for a given ranking.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of users.
 * @param <I> Type of items.
 */
public class GeneralROCCurve<U,I>
{
    /**
     * Binary relevance model. Establishes if an item is relevant or not for a single user.
     */
    private final IdealRelevanceModel<U,I> binRel;
    /**
     * Number of relevant items.
     */
    private final long numRel;
    /**
     * Number of not relevant items.
     */
    private final long numNotRel;
    
    /**
     * Constructor.
     * @param uIndex User index.
     * @param binRel Binary relevance for the different items.
     * @param total Total number of items.
     */
    public GeneralROCCurve(FastUserIndex<U> uIndex, IdealRelevanceModel<U, I> binRel, long total)
    {
       this.binRel = binRel;
       this.numRel = uIndex.getAllUsers().mapToInt(u -> binRel.getModel(u).getRelevantItems().size()).sum();
       this.numNotRel = total - numRel;
    }
    
    /**
     * Obtains the ROC curve (Recall vs. Fallout) for a ranking
     * @param res Ranking.
     * @return The different points of the ROC curve.
     */
    public List<Pair<Double>> getCurve(List<Tuple2oo<U,I>> res)
    {
        List<Pair<Double>> curve = new ArrayList<>();
        
        int numRels = 0;
        int numNotRels = 0;
        
        for(int i = 0; i < res.size(); ++i)
        {
            Tuple2oo<U,I> pair = res.get(i);
            U u = pair.v1();
            I v = pair.v2();
            
            if(binRel.getModel(u).isRelevant(v))
            {
                numRels++;
            }
            else
            {
                numNotRels++;
            }
            
            Pair<Double> point = new Pair<>((numRels + 0.0) / (numRel + 0.0),(numNotRels + 0.0) / (numNotRel + 0.0));
            curve.add(point);
        }
        
        return curve;
    }
}
