/* 
 *  Copyright (C) 2017 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.socialnetwork.community.detection;

import es.uam.eps.ir.socialnetwork.community.Dendogram;
import es.uam.eps.ir.socialnetwork.graph.Graph;

/**
 * Algorithm for detecting the communities of a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public interface DendogramCommunityDetectionAlgorithm<U>
{
    /**
     * Computes the community dendogram for a certain graph.
     * @param graph The full graph.
     * @return The dendogram if everything went OK, null if not.
     */
    public Dendogram<U> detectCommunityDendogram(Graph<U> graph);
}
