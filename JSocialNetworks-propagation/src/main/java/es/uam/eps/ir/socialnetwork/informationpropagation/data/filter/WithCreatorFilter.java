/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.informationpropagation.data.filter;

import es.uam.eps.ir.socialnetwork.index.Index;
import es.uam.eps.ir.socialnetwork.index.fast.FastIndex;
import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The only information pieces that remain are the ones which have an associated creator.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users.
 * @param <I> type of the information pieces.
 * @param <P> type of the parameters.
 */
public class WithCreatorFilter<U extends Serializable, I extends Serializable, P> extends AbstractDataFilter<U,I,P> 
{
    /**
     * Constructor.
     */
    public WithCreatorFilter()
    {
    }
    
    @Override
    protected Index<U> filterUsers(Data<U, I, P> data) 
    {
        Index<U> uIndex = new FastIndex<>();
        data.getAllUsers().sorted().forEach(u -> uIndex.addObject(u));
        return uIndex;
    }

    @Override
    protected Index<I> filterInfoPieces(Data<U, I, P> data) 
    {
        Index<I> iIndex = new FastIndex<>();
        Set<I> informationPieces = new HashSet<>();
        data.getAllInformationPieces().forEach(i -> 
        {
            if(data.getCreators(i).count() > 0)
            {
                informationPieces.add(i);
            }
        });
        informationPieces.stream().sorted().forEach(i -> iIndex.addObject(i));
        return iIndex;
    }

    @Override
    protected Index<P> filterParameters(Data<U, I, P> data, String name, Index<I> iIndex) 
    {
        if(data.isUserFeature(name))
        {
            Index<P> pIndex = new FastIndex<>();
            data.getAllFeatureValues(name).sorted().forEach(p -> pIndex.addObject(p));
            return pIndex;
        }
        else
        {
            Index<P> pIndex = new FastIndex<>();
            Set<P> parameters = new HashSet<>();
            data.getAllInformationPieces().forEach(i -> {
                data.getInfoPiecesFeatures(i, name).forEach(p -> parameters.add(p.v1));
            });
            parameters.stream().sorted().forEach(p -> pIndex.addObject(p));
            return pIndex;
        }
    }
    
}
