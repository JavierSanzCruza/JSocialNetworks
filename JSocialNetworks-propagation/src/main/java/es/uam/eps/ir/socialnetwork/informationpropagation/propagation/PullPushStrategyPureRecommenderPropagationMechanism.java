/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.propagation;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.data.PropagatedInformation;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.UserState;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.SimulationEdgeTypes;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Also known as the rumour spreading propagation mechanism, this is the 
 * propagation mechanism for the pull strategy propagation mechanism.
 * Each iteration, each user selects another one which has not visited in a certain time.
 * The users share all the information between them. 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 * @param <I> type of the information pieces
 * @param <P> type of the parameters.
 */
public class PullPushStrategyPureRecommenderPropagationMechanism<U extends Serializable,I extends Serializable,P> implements PropagationMechanism<U,I,P>
{
    /**
     * Number of iterations to wait until a profile can be revisited
     */
    private final int waitTime;
    /**
     * For each user, the list of users that user will propagate the information to.
     */
    private Map<U, List<U>> propagationList;
    /**
     * The list of users in the last iterations
     */
    private final Map<U, List<U>> lastIterations;
    
    /**
     * Constructor.
     * @param waitTime Number of iterations to wait until a profile can be revisited.
     */
    public PullPushStrategyPureRecommenderPropagationMechanism(int waitTime)
    {
        this.waitTime = waitTime;
        this.lastIterations = new HashMap<>();
    }

    @Override
    public Stream<U> getUsersToPropagate(PropagatedInformation information, UserState<U> originUser, Data<U, I, P> data)
    {
        if(propagationList.containsKey(originUser.getUserId()))
            return propagationList.get(originUser.getUserId()).stream();
        return Stream.empty();
    }
    
    @Override
    public void resetSelections(Data<U,I,P> data)
    {
        Random rng = new Random();
        propagationList = new HashMap<>();
        data.getAllUsers().forEach((u)-> 
        {
            List<U> neighbours = data.getGraph().getAdjacentNodes(u).filter(v -> (data.getGraph().getEdgeType(u, v) == SimulationEdgeTypes.RECOMMEND)).collect(Collectors.toCollection(ArrayList::new));
            List<U> alreadyVisited = lastIterations.containsKey(u) ? lastIterations.get(u) : new ArrayList<>();
            U neigh;
            boolean selected = false;
            
            // Select the neighbour
            do
            {
                if(neighbours.size() > 0)
                {
                    int index = rng.nextInt(neighbours.size());
                    neigh = neighbours.get(index);

                    if(!alreadyVisited.contains(neigh))
                        selected = true;
                }
                else
                {
                    neigh = null;
                    selected = true;
                }
            }
            while(!selected);
            
            if(neigh != null)
            {
                if(!propagationList.containsKey(neigh))
                    propagationList.put(neigh, new ArrayList<>());
                if(!propagationList.get(neigh).contains(u))
                    propagationList.get(neigh).add(u);

                if(!propagationList.containsKey(u))
                    propagationList.put(u, new ArrayList<>());
                if(!propagationList.get(u).contains(neigh))
                    propagationList.get(u).add(neigh);

                alreadyVisited.add(0, neigh);
            }
            
            // Prune the list
            int maxSize = Math.min(this.waitTime, neighbours.size());
            if(alreadyVisited.size() > maxSize)
            {
                for(int i = alreadyVisited.size()-1; i >= maxSize; --i)
                {
                    alreadyVisited.remove(i);
                }
            }
        });
        
    }

    @Override
    public boolean dependsOnInformationPiece() {
        return false;
    }
}
