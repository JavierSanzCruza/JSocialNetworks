/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.communities.graph.gini.degree.sizenormalized;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.graph.InterCommunityGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;

/**
 * Computes the community degree Gini of the graph, i.e. the Gini coefficient for the
 * degree distribution of the communities in the graph. This version only considers
 * the inter-community links for the calculus.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public class SizeNormalizedInterCommunityDegreeGini<U> extends SizeNormalizedCommunityDegreeGini<U> 
{
    /**
     * Constructor
     * @param orientation Orientation of the edges. 
     */
    public SizeNormalizedInterCommunityDegreeGini(EdgeOrientation orientation)
    {
        super(orientation, new InterCommunityGraphGenerator<>());
    }

    @Override
    protected double getDenom(Graph<U> graph, Communities<U> comm, int c) 
    {
        int commSize = comm.getCommunitySize(c);

        if(graph.isDirected() && this.getOrientation().equals(EdgeOrientation.UND))
        {
            return 2.0*commSize*(graph.getVertexCount()-commSize);
        }
        else
        {
            return commSize*(graph.getVertexCount()-commSize);
        }
    }
    
}
