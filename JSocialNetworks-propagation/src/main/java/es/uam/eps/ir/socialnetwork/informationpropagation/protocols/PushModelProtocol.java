/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.protocols;

import es.uam.eps.ir.socialnetwork.informationpropagation.expiration.AllNotPropagatedExpirationMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.propagation.PushStrategyPropagationMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.selections.CountSelectionMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.sight.AllSightMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.update.IndependentCascadeModelUpdateMechanism;
import java.io.Serializable;

/**
 * Model that applies the push strategy for diffunding the information. In this strategy, each
 * user selects a neighbour, and sends it all the information.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 * @param <I> type of the information pieces.
 * @param <P> type of the parameters.
 */
public class PushModelProtocol<U extends Serializable,I extends Serializable,P> extends Protocol<U,I,P>
{
    /**
     * Constructor.
     * @param numOwn Number of own pieces of information to spread every iteration.
     * @param numRec Number of received pieces of information to spread every iteration.
     * @param numWait Number of steps before selecting again a certain neighbor.
     */
    public PushModelProtocol(int numOwn, int numRec, int numWait)
    {
        super(  new CountSelectionMechanism<>(numOwn, numRec), 
                new AllNotPropagatedExpirationMechanism<>(),
                new IndependentCascadeModelUpdateMechanism(),
                new PushStrategyPropagationMechanism<>(numWait),
                new AllSightMechanism<>());
    }
    
}
