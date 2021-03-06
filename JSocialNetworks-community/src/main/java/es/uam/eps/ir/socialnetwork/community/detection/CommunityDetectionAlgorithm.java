/* 
 *  Copyright (C) 2017 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.socialnetwork.community.detection;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.util.List;

/**
 * Algorithm for detecting the communities of a graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public interface CommunityDetectionAlgorithm<U>
{
    /**
     * Computes the communities for a certain graph.
     * @param graph The full graph.
     * @return The communities if everything went OK, null if not.
     */
    public Communities<U> detectCommunities(Graph<U> graph);
    
    /**
     * Computes the communities for a certain graph, given a previous partition.Used for evolution of networks.
     * @param graph The full graph.
     * @param newLinks The links which have newly appeared in the graph.
     * @param disapLinks The links which have disappeared from the graph.
     * @param previous the previous community partition
     * @return the new community partition.
     */
    public default Communities<U> detectCommunities(Graph<U> graph, List<Pair<U>> newLinks, List<Pair<U>> disapLinks, Communities<U> previous)
    {
        return this.detectCommunities(graph);
    }
}
