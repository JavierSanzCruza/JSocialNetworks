/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.degree;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.CommonNeighborsDocBasedRecommender;

/**
 * Most Common Neighbours (MCN) recommender. Recommends the users that share the maximum number of neighbours with the target user.
 * This algorithm is computed using a similar scheme to a document-based query processing task.
 * 
 * Liben-Nowell, D., Kleinberg, J. The Link Prediction Problem for Social Networks. Journal of the American Society for Information Science and Technology 58(7), May 2007.
 * Newman, M.E.J. Clustering and Preferential Attachment in Growing Networks. Physical Review Letters E, 64(025102), April 2001.
 * 
 * Büttcher, S., Clarke, C.L.A., Cormack, G.V. Information Retrieval: Implementing and Evaluating Search Engines. Chapter 5. The MIT Press, 2010
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> type of the users.
 */
public class MostCommonNeighboursDocumentBasedRecommender<U> extends CommonNeighborsDocBasedRecommender<U> 
{    
    /**
     * Constructor.
     * @param graph User graph.
     * @param uSel Link orientation for the target users.
     * @param vSel Link orientation for the candidate users.
     */
    public MostCommonNeighboursDocumentBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph, uSel, vSel);
    }

    @Override
    protected double getValue(int uidx, int vidx, int widx, double uW, double vW) 
    {
        return 1.0;
    }

    @Override
    protected double normalization(int uidx, int vidx, double score) 
    {
        return score;
    }
}
