/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.propagation;

import static es.uam.eps.ir.socialnetwork.grid.informationpropagation.propagation.PropagationMechanismIdentifiers.*;
import es.uam.eps.ir.socialnetwork.informationpropagation.propagation.PropagationMechanism;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.io.Serializable;

/**
 * Class that selects an individual propagation mechanism.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.propagation
 */
public class PropagationSelector<U extends Serializable,I extends Serializable,P> 
{
    /**
     * Selects and configures a propagation mechanism.
     * @param ppr Parameters for the propagation mechanism.
     * @return A pair containing the name and the selected propagation mechanism.
     */
    public Tuple2oo<String, PropagationMechanism<U,I,P>> select(PropagationParamReader ppr)
    {
        String name = ppr.getName();
        PropagationConfigurator<U,I,P> conf;
        switch(name)
        {
            case ALLFOLLOWERS:
                conf = new AllFollowersPropagationConfigurator<>();
                break;
            case PUSHPULL:
                conf = new PushPullPropagationConfigurator<>();
                break;
            case PUSH:
                conf = new PushPropagationConfigurator<>();
                break;
            case PULL:
                conf = new PullPropagationConfigurator<>();
                break;
            case PUSHPULLPUREREC:
                conf = new PushPullPureRecommenderPropagationConfigurator<>();
                break;
            case PUSHPULLREC:
                conf = new PushPullRecommenderPropagationConfigurator<>();
                break;
            default:
                return null;
        }
        
        PropagationMechanism<U,I,P> propagation = conf.configure(ppr);
        return new Tuple2oo<>(name, propagation);
    }
}
