/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.expiration;

import es.uam.eps.ir.socialnetwork.informationpropagation.expiration.ExpirationMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.expiration.TimedExpirationMechanism;
import java.io.Serializable;

/**
 * Configures an Timed expiration mechanism.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.expiration.TimedExpirationMechanism
 */
public class TimedExpirationConfigurator<U extends Serializable,I extends Serializable,P> implements ExpirationConfigurator<U,I,P> 
{
    /**
     * Identifier for the maximum time before expiration of an information piece.
     */
    private final static String MAXTIME = "maxTime";

    @Override
    public ExpirationMechanism<U, I, P> configure(ExpirationParamReader params) 
    {
        long maxTime = params.getParams().getLongValue(MAXTIME);
        return new TimedExpirationMechanism<>(maxTime);
    }
    
}
