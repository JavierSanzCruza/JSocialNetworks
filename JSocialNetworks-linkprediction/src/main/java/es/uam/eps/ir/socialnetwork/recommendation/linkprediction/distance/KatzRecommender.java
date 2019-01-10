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
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;


/**
 * Adaptation of Katz coefficient for link prediction.
 * 
 * Katz, L. A new status index derived from sociometric analysis. Psychmetrika 18(1), March 1953, pp. 39-43.
 * Liben-Nowell, D., Kleinberg, J. The Link Prediction Problem for Social Networks. Journal of the American Society for Information Science and Technology 58(7), May 2007.
 * 
 * @author Javier Sanz-Cruzado Puig.
 * @param <U> Type of the users.
 */
public class KatzRecommender<U> extends UserFastRankingRecommender<U> 
{
    /**
     * Katz matrix
     */
    private Matrix matrix;
    /**
     * Orientation of the links
     */
    private final EdgeOrientation uSel;
    /**
     * Parameter which balances the importance of long paths.
     */
    private final double b;
    
    /**
     * Constructor.
     * @param graph The original graph.
     * @param uSel Link orientation.
     * @param b Parameter which balances the importance of long paths.
     */
    public KatzRecommender(FastGraph<U> graph, EdgeOrientation uSel, double b) {
        super(graph);
        matrix = null;
        this.uSel = uSel;
        this.b = b;
        this.initializeMatrix();
    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
            
        
        this.uIndex.getAllUidx().forEach(iidx -> scoresMap.put(iidx, matrix.get(uidx, iidx)));
        return scoresMap;
    }

    /**
     * Initializes the Katz matrix, and computes the scores.
     */
    private void initializeMatrix()
    {
        Matrix aux = this.getGraph().getAdjacencyMatrixMTJ(uSel);
        DenseMatrix lsm = Matrices.identity(this.numUsers());
        lsm.add(-this.b, aux);
        
        this.matrix = lsm.solve(Matrices.identity(lsm.numRows()), Matrices.identity(lsm.numRows()));
    }
    
    
}
