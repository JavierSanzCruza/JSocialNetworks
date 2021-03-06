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
 * Filter that removes any information feature that appears less than a fixed 
 * number of times (i.e. there are less than X information pieces represented by
 * that feature).
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the user identifiers.
 * @param <I> Type of the information pieces.
 * @param <P> Type of the parameters.
 */
public class MinimumFrequencyInformationFeatureFilter<U extends Serializable,I extends Serializable, P> extends AbstractDataFilter<U,I,P>
{
    /**
     * The definitive of tags to filter.
     */
    private final Set<P> tags;
    /**
     * The minimum number of tags.
     */
    private final long minimumPieces;
    /**
     * The name of the information parameter.
     */
    private final String featureName;
    
    
    /**
     * Constructor.
     * @param minimumPieces minimum number of information pieces that must contain the feature.
     * @param featureName the name of the feature
     */
    public MinimumFrequencyInformationFeatureFilter(long minimumPieces, String featureName)
    {
        this.minimumPieces = minimumPieces;
        this.tags = new HashSet<>();
        this.featureName = featureName;
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
        // First, we determine which tags are valid. Then, we select the corresponding tweets.
        this.selectTags(data);
        
        Index<I> iIndex = new FastIndex<>();
        Set<I> informationPieces = new HashSet<>();
        data.getAllInformationPieces().forEach(i -> 
        {
            Set<P> features = new HashSet<>();
            data.getInfoPiecesFeatures(i, featureName).forEach(p -> 
            {
                if(this.tags.contains(p.v1))
                {
                    features.add(p.v1);
                }
            });
            
            if(!features.isEmpty())
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
        Index<P> pIndex = new FastIndex<>();
        
        if(data.isUserFeature(name))
        {
            data.getAllFeatureValues(name).sorted().forEach(p -> pIndex.addObject(p));
            return pIndex;    
        }
        else if(name != null && !this.featureName.equals(name))// Item feature
        {
            Set<P> par = new HashSet<>();
            iIndex.getAllObjects().forEach(i -> data.getInfoPiecesFeatures(i, name).forEach(p -> par.add(p.v1)));
            par.stream().sorted().forEach(p -> pIndex.addObject(p));
            return pIndex;
        }
        else
        {
            this.tags.stream().sorted().forEach(p -> pIndex.addObject(p));
            return pIndex;
        }
    }

    /**
     * Selects the subset of the current tags that appear in (at least) minimumFeatures 
     * information pieces.
     * @param data the original data.
     */
    private void selectTags(Data<U,I,P> data) 
    {
        this.tags.clear();
        data.getAllFeatureValues(featureName).forEach(p -> 
        {
            long count = data.getInformationPiecesWithFeature(featureName, p).count();
            if(count >= this.minimumPieces)
            {
                this.tags.add(p);
            }
        });
    }
    
}
