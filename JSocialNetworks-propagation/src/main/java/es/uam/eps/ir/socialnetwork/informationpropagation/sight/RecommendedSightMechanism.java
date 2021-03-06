/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.sight;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.data.PropagatedInformation;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.UserState;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.SimulationEdgeTypes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;

/**
 * This mechanism applies two different probabilities: one for observing information
 * pieces from recommended users, and other for observing information pieces from
 * training users.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 * @param <I> Type of the information pieces
 * @param <P> Type of the parameters
 */
public class RecommendedSightMechanism<U extends Serializable,I extends Serializable,P> implements SightMechanism<U,I,P>
{

    /**
     * For each user, the set of users they are neighbors to see information from
     */
    private final Map<U, Set<U>> neighbors;
    /**
     * Indicates if the selections have been initialized or not.
     */
    private boolean initialized = false;
    /**
     * Probability of observing a piece of information that comes from a recommended user.
     */
    private final double probRec;
    /**
     * Probability of observing a piece of information that comes from a training user.
     */
    private final double probTrain;
    /**
     * Random number generator
     */
    private final Random rng;
    
    /**
     * Constructor. 
     * @param probRec probability of observing a piece of information that comes from a recommended user.
     * @param probTrain probability of observing a piece of information that comes from a training user.
     */
    public RecommendedSightMechanism(double probRec, double probTrain)
    {
        this.neighbors = new HashMap<>();
        this.probRec = probRec;
        this.probTrain = probTrain;
        this.rng = new Random();
    }
    
/*    @Override
    public Stream<PropagatedInformation> seeInformation(UserState<U> user, Data<U, I, P> data)
    {
        List<PropagatedInformation> seen = new ArrayList<>();
        
        U u = user.getUserId();
        user.getNewInformation().forEach(info -> 
        {
            boolean propagate = info.getCreators().stream().map(creator -> 
            {
                double rnd = rng.nextDouble();
                U cr = data.getUserIndex().idx2object(creator);
                if(this.neighbors.get(u).contains(cr))
                {
                    return rnd < probRec;
                }
                else
                {
                    return rnd < probTrain;
                }
            }).reduce(false, (x,y) -> x || y);
            
            if(propagate)
            {
                seen.add(info);
            }
        });
        
        return seen.stream();
    }*/
    
    @Override
    public void resetSelections(Data<U,I,P> data)
    {
        // Store the information for speeding up the simulation
        if(this.initialized == false)
        {
            Graph<U> graph = data.getGraph();
            graph.getAllNodes().forEach(u -> {
                this.neighbors.put(u, new HashSet<>());
                graph.getAdjacentNodes(u).forEach(v -> {
                    int edgeType = graph.getEdgeType(u, v);
                    if(edgeType == SimulationEdgeTypes.RECOMMEND)
                        this.neighbors.get(u).add(v);
                });
            });
            this.initialized = true;
        }
    }
    
    @Override
    public boolean seesInformation(UserState<U> user, Data<U,I,P> data, PropagatedInformation prop)
    {
        boolean propagate = prop.getCreators().stream().map(creator -> 
        {
            double rnd = rng.nextDouble();
            U cr = data.getUserIndex().idx2object(creator);
            if(this.neighbors.get(user.getUserId()).contains(cr))
            {
                return rnd < probRec;
            }
            else
            {
                return rnd < probTrain;
            }
        }).reduce(false, (x,y) -> x || y);
            
        return propagate && !user.containsPropagatedInformation(prop.getInfoId());
    }
}
