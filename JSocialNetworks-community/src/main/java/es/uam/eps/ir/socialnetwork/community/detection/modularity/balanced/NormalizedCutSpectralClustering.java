/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.community.detection.modularity.balanced;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;

/**
 * Community detection algorithm for balanced communities. It uses the normalized cut laplacian.
 * 
 * Zafarani, R., Abassi, M.A., Liu, H. Social Media Mining: An Introduction. Chapter 6. 2014
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class NormalizedCutSpectralClustering<U> extends SpectralClustering<U> 
{
   
    /**
     * Constructor.
     * @param k The number of clusters we want to find
     */
    public NormalizedCutSpectralClustering(int k)
    {
        super(k);
    }
    
    @Override
    protected DoubleMatrix2D laplacian(DoubleMatrix2D degree, DoubleMatrix2D adjacency)
    {
        DoubleMatrix2D laplacian = new SparseDoubleMatrix2D(degree.rows(), degree.columns());
        DoubleMatrix2D aux = new SparseDoubleMatrix2D(degree.toArray());
        for(int i = 0; i < degree.rows(); ++i)
        {
            laplacian.setQuick(i, i, 1.0);
            aux.setQuick(i, i, Math.sqrt(aux.getQuick(i,i)));
        }
        
        DoubleMatrix2D z1 = new SparseDoubleMatrix2D(degree.rows(), degree.columns());
        DoubleMatrix2D z2 = new SparseDoubleMatrix2D(degree.rows(), degree.columns());
        aux.zMult(adjacency, z1);
        z1.zMult(aux,z2);
        
        laplacian.assign(z2, (x,y)->(x-y));
        
        return laplacian;
    }
    
    
    
    
}
