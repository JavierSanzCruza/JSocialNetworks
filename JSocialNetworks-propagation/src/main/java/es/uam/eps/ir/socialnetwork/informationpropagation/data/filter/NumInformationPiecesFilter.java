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
 * Filter that limits the maximum number of information pieces that a single user
 * can have. In case a user has more than the given number, the filter selects the
 * desired number of information pieces randomly for the user.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the users
 * @param <I> type of the information.
 * @param <P> type of the parameters.
 */
public class NumInformationPiecesFilter<U extends Serializable, I extends Serializable, P> extends AbstractDataFilter<U,I,P> 
{
    /**
     * The maximum number of information pieces to retrieve from each user.
     */
    private final int numTweets;
    
    /**
     * Constructor.
     * @param numTweets maximum number of information pieces to retrieve from each user.
     */
    public NumInformationPiecesFilter(int numTweets)
    {
        this.numTweets = numTweets;
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
        Set<I> info = new HashSet<>();
        data.getAllUsers().forEach(u -> 
        {
            data.getPieces(u).sorted().limit(numTweets).forEach(i -> info.add(i));
        });
        
        info.stream().sorted().forEach(i -> iIndex.addObject(i));
        return iIndex;
    }

    @Override
    protected Index<P> filterParameters(Data<U, I, P> data, String name, Index<I> iIndex) {
        Index<P> pIndex = new FastIndex<>();
        
        if(data.isUserFeature(name))
        {
            data.getAllFeatureValues(name).sorted().forEach(p -> pIndex.addObject(p));
            return pIndex;    
        }
        else // Item feature
        {
            Set<P> par = new HashSet<>();
            iIndex.getAllObjects().forEach(i -> data.getInfoPiecesFeatures(i, name).forEach(p -> par.add(p.v1)));
            par.stream().sorted().forEach(p -> pIndex.addObject(p));
            return pIndex;
        }
    }
}
