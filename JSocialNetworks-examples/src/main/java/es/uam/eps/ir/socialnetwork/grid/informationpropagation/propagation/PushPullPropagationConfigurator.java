/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.propagation;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.informationpropagation.propagation.PropagationMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.propagation.PullPushStrategyPropagationMechanism;
import java.io.Serializable;

/**
 * Configures a Push-Pull propagation mechanism.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.propagation.PullPushStrategyPropagationMechanism
 */
public class PushPullPropagationConfigurator<U extends Serializable,I extends Serializable,P> implements PropagationConfigurator<U,I,P> 
{
    /**
     * Identifier for the time before selecting a given user.
     */
    private final static String WAITTIME = "waitTime";
    /**
     * Identifier for the edge selection.
     */
    private final static String ORIENTATION = "orientation";
    
    @Override
    public PropagationMechanism<U, I, P> configure(PropagationParamReader params) 
    {
        int waitTime = params.getParams().getIntegerValue(WAITTIME);
        
        if(params.getParams().getOrientationValue(ORIENTATION) != null)
        {
            EdgeOrientation orient = params.getParams().getOrientationValue(ORIENTATION);
            return new PullPushStrategyPropagationMechanism<>(waitTime, orient);
        }
        else
        {
            return new PullPushStrategyPropagationMechanism<>(waitTime);
        }
    }
    
}
