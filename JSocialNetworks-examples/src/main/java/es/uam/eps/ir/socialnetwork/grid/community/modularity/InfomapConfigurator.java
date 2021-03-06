/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.community.modularity;

import es.uam.eps.ir.socialnetwork.community.detection.CommunityDetectionAlgorithm;
import es.uam.eps.ir.socialnetwork.community.detection.modularity.Infomap;
import es.uam.eps.ir.socialnetwork.grid.Parameters;
import es.uam.eps.ir.socialnetwork.grid.community.CommunityDetectionConfigurator;
import java.io.Serializable;

/**
 * Configurator for the Infomap community detection algorithm.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <U> Type of the users
 */
public class InfomapConfigurator<U extends Serializable> implements CommunityDetectionConfigurator<U> 
{
    private final static String DIREXEC = "dirExec";
    private final static String UNDIREXEC = "undirExec";
    
    @Override
    public CommunityDetectionAlgorithm<U> configure(Parameters params) 
    {
        String directed = params.getStringValue(DIREXEC);
        String undirected = params.getStringValue(UNDIREXEC);
        return new Infomap<>(directed,undirected);
    }
    
}
