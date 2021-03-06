/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.protocol;

import es.uam.eps.ir.socialnetwork.informationpropagation.protocols.Protocol;
import java.io.Serializable;

/**
 * Configures a protocol
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 */
public interface ProtocolConfigurator<U extends Serializable,I extends Serializable,P>
{
    /**
     * Configures a protocol.
     * @param params Parameters of the protocol.
     * @return the protocol.
     */
    public Protocol<U,I,P> configure(ProtocolParamReader params);
}
