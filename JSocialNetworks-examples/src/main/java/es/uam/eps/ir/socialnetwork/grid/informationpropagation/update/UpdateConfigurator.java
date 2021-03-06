/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.update;

import es.uam.eps.ir.socialnetwork.informationpropagation.update.UpdateMechanism;

/**
 * Configures a update mechanism.
 * @author Javier Sanz-Cruzado Puig
 */
public interface UpdateConfigurator 
{
    /**
     * Configures a update mechanism for the information pieces.
     * @param params the parameters of the mechanism.
     * @return the update mechanism.
     */
    public UpdateMechanism configure(UpdateParamReader params);
}
