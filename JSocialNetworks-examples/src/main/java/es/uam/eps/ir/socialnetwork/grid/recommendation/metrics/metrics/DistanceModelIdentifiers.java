/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.metrics;

/**
 * Identifiers for Item Distance Models
 * @author Javier Sanz-Cruzado Puig
 * 
 * @see es.uam.eps.ir.ranksys.novdiv.distance.ItemDistanceModel
 */
public class DistanceModelIdentifiers 
{
    /**
     * Identifier for the Cosine distance
     * @see es.uam.eps.ir.ranksys.novdiv.distance.CosineFeatureItemDistanceModel
     */
    public final static String COSINE = "Cosine";
    /**
     * Identifier for the Jaccard distance
     * @see es.uam.eps.ir.ranksys.novdiv.distance.JaccardFeatureItemDistanceModel
     */
    public final static String JACCARD = "Jaccard";
    
}
