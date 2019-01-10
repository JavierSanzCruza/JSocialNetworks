/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ir;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;

/**
 * Binary independent retrieval algorithm.
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * Sparck Jones, K., Walker, S., Roberton S.E. A Probabilistic Model of Information Retrieval: Development and Comparative Experiments. 
 * Information Processing and Management 36. February 2000, pp. 779-808 (part 1), pp. 809-840 (part 2).
 * 
 * @param <U> type of the users
 */
public class BIRRecommender<U> extends BM25Recommender<U>
{
    /**
     * Constructor
     * @param graph Graph
     * @param uSel Selection of the neighbours of the target user
     * @param vSel Selection of the neighbours of the candidate user
     */
    public BIRRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel) 
    {
        super(graph, uSel, vSel, EdgeOrientation.UND, 0.0, Double.POSITIVE_INFINITY);
    }
}
