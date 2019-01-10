/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.distance;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;

/**
 * Local Path Index (LPI) recommendation method, adapted from the link prediction algorithm.
 * 
 * Lü, L., Jin, C., Zhou, T. Similarity Index Based on Local Paths for Link Prediction of Complex Networks. Physical Review E 80(4) : 046122, October 2009, pp
 * Lü, L., Zhou. T. Link Prediction in Complex Networks: A survey. Physica A: Statistical Mechanics and its Applications, 390(6), March 2011, pp. 1150-1170.
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 */
public class LocalPathIndexRecommender<U> extends UserFastRankingRecommender<U> 
{

    /**
     * Discount variable (weight between 0 and 1)
     */
    private final double b;
    /**
     * Direction to take for the adjacency matrix (usual : OUT)
     */
    private final EdgeOrientation uSel;
    /**
     * Maximum exponent of the matrix
     */
    private final int n;
    /**
     * Score matrix
     */
    private Matrix matrix;
    
    /**
     * Constructor.
     * @param graph Graph.
     * @param b Discount variable (weight between 0 and 1)
     * @param uSel Direction to take for the adjacency matrix (usual : OUT)
     * @param n Maximum exponent of the matrix
     */
    public LocalPathIndexRecommender(FastGraph<U> graph, double b, EdgeOrientation uSel, int n) 
    {
        super(graph);
        this.b = b;
        this.uSel = uSel;
        this.n = n;
        
        this.initializeMatrix();
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) {
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        
        this.uIndex.getAllUidx().forEach(iidx -> scoresMap.put(iidx, matrix.get(uidx, iidx)));
        return scoresMap;
    }
    
    /**
     * Computes the score matrix.
     */
    private void initializeMatrix()
    {
        Matrix lsm = this.getGraph().getAdjacencyMatrixMTJ(uSel);
        Matrix aux = lsm;
        this.matrix = new LinkedSparseMatrix(this.uIndex.numUsers(), this.uIndex.numUsers());
        for(int i = 2; i <= n; ++i)
        {
            Matrix aux2 = new LinkedSparseMatrix(this.uIndex.numUsers(), this.uIndex.numUsers());
            lsm.mult(b, aux, aux2);
            aux = aux2;
            this.matrix.add(aux);
        }
        
    }

    
    
}
