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
import java.util.List;
import org.ranksys.core.index.fast.FastUserIndex;
import org.ranksys.metrics.rel.BinaryRelevanceModel;
import org.ranksys.metrics.rel.IdealRelevanceModel;

/**
 * Computes the area under the ROC Curve for a ranking.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 * @param <I> type of the items.
 */
public class GeneralAUC<U,I> 
{
    /**
     * The ROC Curve
     */
    private final GeneralROCCurve<U,I> curve;
    
    /**
     * Constructor.
     * @param uIndex User index.
     * @param binRel Binary relevance for the different items.
     * @param total Total number of items.
     */
    public GeneralAUC(FastUserIndex<U> uIndex, IdealRelevanceModel<U, I> binRel, long total)
    {
        this.curve = new GeneralROCCurve<>(uIndex, binRel, total);
    }
    
    /**
     * Constructor
     * @param curve TThe ROC Curve. 
     */
    public GeneralAUC(GeneralROCCurve<U,I> curve)
    {
        this.curve = curve;
    }
    
    /**
     * Computes the area under the ROC curve for the ranking
     * @param res the ranking.
     * @return the GeneralAUC value.
     */
    public double evaluate(List<Tuple2oo<U,I>> res)
    {
        List<Pair<Double>> roc = this.curve.getCurve(res);
        
        double auc = 0.0;
        if(roc.size() > 2)
        {
            for(int i = 1; i < roc.size(); ++i)
            {
                double currentX = roc.get(i).v2();
                double previousX = roc.get(i-1).v2();
                double currentY = roc.get(i).v1();
                double previousY = roc.get(i-1).v1();
                
                auc += (currentX - previousX)*(currentY + previousY)/2.0;
            }
        }
        
        return auc;
    }
    
    
}
