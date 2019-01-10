/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.ranksys.socialextension;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import org.ranksys.metrics.rel.RelevanceModel;

/**
 * Relevance model which indicates that a recommendation is relevant if the 
 * test graph contains the link and its reciprocal.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> type of the users
 */
public class ReciprocalRelevanceModel<U> extends RelevanceModel<U,U>
{
    /**
     * The test graph.
     */
    private final Graph<U> graph;
    
    /**
     * Constructor.
     * @param graph the test graph. 
     */
    public ReciprocalRelevanceModel(Graph<U> graph)
    {
        this.graph = graph;
    }
    
    @Override
    protected UserRelevanceModel<U, U> get(U user)
    {
        return new UserReciprocalRelevanceModel(user);
    }
    
    /**
     * Reciprocal relevance model for the user.
     */
    public class UserReciprocalRelevanceModel implements UserRelevanceModel<U,U>
    {
        /**
         * The user
         */
        private final U user;
        /**
         * Constructor.
         * @param user the user.
         */
        public UserReciprocalRelevanceModel(U user)
        {
            this.user = user;
        }
        
        @Override
        public boolean isRelevant(U item)
        {
            if(!graph.isDirected()) return graph.containsEdge(user, item);
            else return graph.containsEdge(user, item) && graph.containsEdge(item, user);
        }

        @Override
        public double gain(U item)
        {
            return isRelevant(item) ? 1.0 : 0.0;
        }
        
    }

}
