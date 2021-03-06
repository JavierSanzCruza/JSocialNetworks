/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.update;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.PropagatedInformation;

/**
 * Class for the update mechanism for the information in the corresponding lists
 * @author Javier Sanz-Cruzado Puig
 */
public interface UpdateMechanism
{
    /**
     * Updates a piece of information which has been newly seen.
     * @param oldInfo Piece of information with old info.
     * @param newInfo Piece of information with new info.
     * @return the updated piece of information.
     */
    public PropagatedInformation updateSeen(PropagatedInformation oldInfo, PropagatedInformation newInfo);
    
    
    /**
     * Updates a piece of information which was previously discarded.
     * 
     * @param oldInfo Piece of information with the discarded info.
     * @param newInfo Piece of information with the new info.
     * @return the updated piece of information
     */
    public PropagatedInformation updateDiscarded(PropagatedInformation oldInfo, PropagatedInformation newInfo);
}
