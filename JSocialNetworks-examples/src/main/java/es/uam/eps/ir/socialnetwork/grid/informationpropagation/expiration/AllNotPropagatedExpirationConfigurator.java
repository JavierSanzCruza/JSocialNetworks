/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.expiration;

import es.uam.eps.ir.socialnetwork.informationpropagation.expiration.AllNotPropagatedExpirationMechanism;
import es.uam.eps.ir.socialnetwork.informationpropagation.expiration.ExpirationMechanism;
import java.io.Serializable;

/**
 * Configures an All Not Propagated expiration mechanism.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.expiration.AllNotPropagatedExpirationMechanism
 */
public class AllNotPropagatedExpirationConfigurator<U extends Serializable,I extends Serializable,P> implements ExpirationConfigurator<U,I,P> 
{

    @Override
    public ExpirationMechanism<U, I, P> configure(ExpirationParamReader params) 
    {
        return new AllNotPropagatedExpirationMechanism<>();
    }
    
}
