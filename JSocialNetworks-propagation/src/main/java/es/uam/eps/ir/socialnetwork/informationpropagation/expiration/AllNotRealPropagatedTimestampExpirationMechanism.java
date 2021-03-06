/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.expiration;

import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.UserState;
import java.io.Serializable;
import java.util.stream.Stream;

/**
 * If current timestamp is greater than the timestamp of the pieces, the elements 
 * are discarded
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the features.
 */
public class AllNotRealPropagatedTimestampExpirationMechanism<U extends Serializable, I extends Serializable, P> implements ExpirationMechanism<U,I,P> 
{
    
    @Override
    public Stream<Integer> expire(UserState<U> user, Data<U,I,P> data, int numIter, Long timestamp)
    {
        U u = user.getUserId();
        if(timestamp != null)
        {
            return user.getReceivedInformation().filter(piece -> 
            {
                I i = data.getInformationPiecesIndex().idx2object(piece.getInfoId());
                return !(data.isRealRepropagatedPiece(user.getUserId(), i) && data.getRealPropagatedTimestamp(u, i) >= timestamp);
            }).map(piece -> piece.getInfoId());
        }
        else //If there is no timestamp, we assume that all pieces were created before the timestamp
        {
            return user.getReceivedInformationIds();
        }
    }
}
