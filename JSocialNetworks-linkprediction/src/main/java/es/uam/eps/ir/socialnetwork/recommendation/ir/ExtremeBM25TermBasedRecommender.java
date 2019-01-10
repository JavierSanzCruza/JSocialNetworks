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
 * Binary independent retrieval algorithm, using a document-based query processing mechanism.
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * Sparck Jones, K., Walker, S., Roberton S.E. A Probabilistic Model of Information Retrieval: Development and Comparative Experiments. 
 * Information Processing and Management 36. February 2000, pp. 779-808 (part 1), pp. 809-840 (part 2).
 * 
 * @param <U> type of the users
 */
public class ExtremeBM25TermBasedRecommender<U> extends BM25TermBasedRecommender<U>
{
    /**
     * Constructor
     * @param graph Graph
     * @param uSel Selection of the neighbours of the target user
     * @param vSel Selection of the neighbours of the candidate user
     * @param dlSel Selection of the neighbours for the document length
     * @param b Tunes the effect of the neighborhood size. Between 0 and 1
     */
    public ExtremeBM25TermBasedRecommender(FastGraph<U> graph, EdgeOrientation uSel, EdgeOrientation vSel, EdgeOrientation dlSel, double b) 
    {
        super(graph, uSel, vSel, dlSel, b, Double.POSITIVE_INFINITY);
    }
}
