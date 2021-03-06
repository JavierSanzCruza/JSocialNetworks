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
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.SimulationState;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Selects the propagated pieces. This algorithm propagates a certain number of information pieces each
 * iteration, chosen randomly from the own pieces of information and the received ones.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 * @param <I> type of the information.
 * @param <P> type of the parameters.
 */
public class CountSelectionMechanism<U extends Serializable,I extends Serializable,P> extends AbstractSelectionMechanism<U,I,P>
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
     * Number of information pieces to repropagate for each user and iteration.
     */
    private final int numRepropagate;
    /**
     * Random number generator
     */
    protected final Random rng;
    
    /**
     * Constructor.
     * @param numOwn Number of own information pieces to propagate for each user and iteration.
     * @param numPropagate Number of received information to propagate for each user and iteration.
     * @param numRepropagate Number of information pieces to repropagate for each user and iteration.
     */
    public CountSelectionMechanism(int numOwn, int numPropagate, int numRepropagate)
    {
        this.numOwn = numOwn;
        this.numPropagate = numPropagate;
        this.numRepropagate = numRepropagate;
        this.rng = new Random();
    }
    
    /**
     * Constructor.
     * @param numOwn Number of own information pieces to propagate for each user and iteration.
     * @param numPropagate Number of received information to propagate for each user and iteration.
     */
    public CountSelectionMechanism(int numOwn, int numPropagate)
    {
        this(numOwn, numPropagate, SelectionConstants.NONE);
        
    }
    
    @Override
    protected List<PropagatedInformation> getOwnInformation(UserState<U> user, Data<U, I, P> data, SimulationState<U, I, P> state, int numIter, Long timestamp) 
    {
        List<PropagatedInformation> ownInfo = user.getOwnInformation().collect(Collectors.toCollection(ArrayList::new));
        int userId = data.getUserIndex().object2idx(user.getUserId());
        
        return this.getPropagatedInformation(userId, this.numOwn, numIter, ownInfo);
    }

    @Override
    protected List<PropagatedInformation> getReceivedInformation(UserState<U> user, Data<U, I, P> data, SimulationState<U, I, P> state, int numIter, Long timestamp) 
    {
        List<PropagatedInformation> recInfo = user.getReceivedInformation().collect(Collectors.toCollection(ArrayList::new));
        int userId = data.getUserIndex().object2idx(user.getUserId());
        
        return this.getPropagatedInformation(userId, this.numPropagate, numIter, recInfo);    
    }

    @Override
    protected List<PropagatedInformation> getRepropagatedInformation(UserState<U> user, Data<U, I, P> data, SimulationState<U, I, P> state, int numIter, Long timestamp) 
    {
      List<PropagatedInformation> repInfo = user.getPropagatedInformation().collect(Collectors.toCollection(ArrayList::new));
        int userId = data.getUserIndex().object2idx(user.getUserId());
        
        return this.getPropagatedInformation(userId, this.numRepropagate, numIter, repInfo);        
    }
    
    /**
     * Obtains the repropagated information from a list of pieces.
     * @param userId the user identifier.
     * @param count the number of information pieces to obtain from that list.
     * @param numIter current iteration number.
     * @param propInfo the list of propagated information we want to obtain some pieces from.
     * @return the list of information pieces to propagate.
     */
    protected List<PropagatedInformation> getPropagatedInformation(int userId, int count, int numIter, List<PropagatedInformation> propInfo)
    {
        List<PropagatedInformation> propagatedPieces = new ArrayList<>();
        Set<Integer> setInfo = new HashSet<>();
        int size = propInfo.size();
        if(count != SelectionConstants.NONE)
        {
            if(count == SelectionConstants.ALL || size <= count)
            {
                propInfo.forEach(info -> propagatedPieces.add(new PropagatedInformation(info.getInfoId(), numIter, userId)));
            }
            else
            {
                while(setInfo.size() < this.numOwn)
                {
                    setInfo.add(propInfo.get(rng.nextInt(size)).getInfoId());
                }

                setInfo.stream().forEach(idx -> propagatedPieces.add(new PropagatedInformation(idx, numIter, userId)));
            }
        }
        return propagatedPieces;
    }

    /**
     * Gets the number of own information pieces to propagate.
     * @return the number of own information pieces to propagate
     */
    public int getNumOwn() 
    {
        return numOwn;
    }

    /**
     * Gets the number of received information pieces to propagate.
     * @return the number of received information pieces to propagate.
     */
    public int getNumPropagate() 
    {
        return numPropagate;
    }

    /**
     * Gets the number of propagated information pieces to repropagate.
     * @return the number of propagated information pieces to repropagate.
     */
    public int getNumRepropagate() 
    {
        return numRepropagate;
    }
    
    
    
    
}
