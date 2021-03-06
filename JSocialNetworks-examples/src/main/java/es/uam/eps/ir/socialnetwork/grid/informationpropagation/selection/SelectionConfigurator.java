/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.selection;

import es.uam.eps.ir.socialnetwork.informationpropagation.selections.SelectionMechanism;
import java.io.Serializable;

/**
 * Configures a selection mechanism.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 */
public interface SelectionConfigurator<U extends Serializable,I extends Serializable,P> 
{
    /**
     * Configures a selection mechanism for the information pieces to propagate.
     * @param params the parameters of the mechanism.
     * @return the selection mechanism.
     */
    public SelectionMechanism<U,I,P> configure(SelectionParamReader params);
}
