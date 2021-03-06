/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.communities.graph.gini.degree;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.graph.CommunityGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.metrics.CommunityMetric;
import es.uam.eps.ir.socialnetwork.metrics.graph.DegreeGini;

/**
 * Computes the community degree Gini of the graph, i.e. the Gini coefficient for the
 * degree distribution of the communities in the graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 */
public abstract class CommunityDegreeGini<U> implements CommunityMetric<U> 
{
    /**
     * The degree to take in the community graph.
     */
    private final EdgeOrientation orientation;
    /**
     * The community graph generator.
     */
    private final CommunityGraphGenerator<U> cgg;
    
    /**
     * Constructor.
     * @param orientation Orientation of the edges. 
     * @param cgg Community graph generator.
     */
    public CommunityDegreeGini(EdgeOrientation orientation, CommunityGraphGenerator<U> cgg)
    {
        this.orientation = orientation;
        this.cgg = cgg;
    }
    
    @Override
    public double compute(Graph<U> graph, Communities<U> comm) 
    {
        Graph<Integer> commGraph = cgg.generate(graph, comm);
        
        DegreeGini<Integer> degreeGini = new DegreeGini<>(orientation);
        return degreeGini.compute(commGraph);
    }
    
}
