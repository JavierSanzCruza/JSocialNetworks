/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.selections;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.data.PropagatedInformation;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.UserState;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.SimulationEdgeTypes;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.SimulationState;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Selects the propagated pieces depending on the recommendations. When the user has to propagate
 * others information, chooses with some probability the information that has arrived from some users
 * that have been recommended to him, and with the rest of probability the information that has arrived from
 * users of the original graph.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 * @param <I> type of the information.
 * @param <P> type of the parameters.
 */
public class RecommenderSelectionMechanism<U extends Serializable,I extends Serializable, P> implements SelectionMechanism<U,I,P>
{
    /**
     * Number of own information pieces to propagate for each user and iteration.
     */
    private final int numOwn;
    /**
     * Number of received information to propagate for each user and iteration.
     */
    private final int numPropagate;
    /**
     * Probability of choosing information to propagate that comes from recommended users.
     * If there are enough pieces, each time a piece of information is selected, with probability prob that piece will come from recommended
     * users, and with probability (1-p) the piece will come from an edge in the training graph.
     */
    private final double prob;
    
    /**
     * Constructor.
     * @param numOwn Number of own information pieces to propagate for each user and iteration.
     * @param numPropagate Number of received information to propagate for each user and iteration.
     * @param prob Probability of chosing information to propagate that comes from recommended users.
     */
    public RecommenderSelectionMechanism(int numOwn, int numPropagate, double prob)
    {
        this.numOwn = numOwn;
        this.numPropagate = numPropagate;
        this.prob = prob;
    }

    @Override
    public Selection select(UserState<U> user, Data<U, I, P> data, SimulationState<U,I,P> state, int numIter, Long timestamp)
    {
        // Variables for generating the selection
        int userId = data.getUserIndex().object2idx(user.getUserId());
        
        Random rng = new Random();
        List<PropagatedInformation> ownToPropagate = new ArrayList<>();
        List<PropagatedInformation> receivedToPropagate = new ArrayList<>();
        
        // Auxiliary variables
        Set<Integer> setInfo;
        
        // Decides which own information pieces we want to propagate in this iteration.
        if(user.getOwnInformation().count() <= numOwn) //If there are not enough pieces of information (or the same we want).
        {
            user.getOwnInformation().forEach(info -> ownToPropagate.add(new PropagatedInformation(info.getInfoId(),numIter,userId)));
        }
        else // In case there are more pieces of information that the number we want to retrieve.
        {
            List<PropagatedInformation> ownInfo = user.getOwnInformation().collect(Collectors.toCollection(ArrayList::new));
            setInfo = new HashSet<>();
            while(setInfo.size() < this.numOwn)
            {
                setInfo.add(ownInfo.get(rng.nextInt(ownInfo.size())).getInfoId());
            }
            
            setInfo.stream().forEach(idx -> ownToPropagate.add(new PropagatedInformation(idx, numIter, userId)));
        }
        
        // Decides which received pieces we want to propagate in this iteration.
        if(user.getReceivedInformation().count() <= this.numPropagate) // If there are not enough pieces of information (or the same number as we want).
        {
            user.getReceivedInformation().forEach(info-> receivedToPropagate.add(new PropagatedInformation(info.getInfoId(), numIter, userId)));
        }
        else
        {
            // Divide the information depending on the source.
            List<Integer> fromNeigh = new ArrayList<>();
            List<Integer> fromRec = new ArrayList<>();

            setInfo = new HashSet<>();
            
            
            user.getReceivedInformation().forEach(info -> 
            {
                List<Integer> creators = new ArrayList<>();
                creators.addAll(info.getCreators());                
                List<Integer> indexesToDelete = new ArrayList<>();
                for(int i = 0; i < creators.size();++i)
                {
                    Integer creator = creators.get(i);
                    if(creator == null) // Checks if the index has to be deleted (null source)
                    {
                        indexesToDelete.add(i);
                    }
                    else
                    {
                        U creatorUser = data.getUserIndex().idx2object(creator);
                        if(data.getGraph().getEdgeType(user.getUserId(), creatorUser) == SimulationEdgeTypes.RECOMMEND)
                            fromRec.add(info.getInfoId());
                        else
                            fromNeigh.add(info.getInfoId());   
                    }

                }

                // Remove the corresponding null sources
                for(int i = indexesToDelete.size()-1; i >= 0; --i)
                {
                    creators.remove((int) indexesToDelete.get(i)); 
                    System.out.println(creators);
                }
            });
            
            // Decides which information pieces we want to propagate
            while(setInfo.size() < this.numPropagate)
            {
                double r = rng.nextDouble();
                if(fromNeigh.isEmpty() || (r < this.prob && !fromRec.isEmpty())) // Select a piece from 
                {
                    setInfo.add(fromRec.get(rng.nextInt(fromRec.size())));
                }
                else if(!fromNeigh.isEmpty())
                {
                    setInfo.add(fromNeigh.get(rng.nextInt(fromNeigh.size())));
                }
            }
            
            setInfo.stream().forEach(idx -> receivedToPropagate.add(new PropagatedInformation(idx, numIter, userId)));
        }
        
        return new Selection(ownToPropagate, receivedToPropagate);
    }

    @Override
    public Stream<U> getSelectableUsers(Data<U, I, P> data, SimulationState<U, I, P> state, int numIter, Long timestamp) {
        return data.getAllUsers();
    }
}
