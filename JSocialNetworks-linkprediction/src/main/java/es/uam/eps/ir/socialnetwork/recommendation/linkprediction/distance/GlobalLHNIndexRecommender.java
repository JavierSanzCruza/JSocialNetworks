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
import java.util.Map;
import java.util.Map.Entry;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVectorSub;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.NotConvergedException;
import no.uib.cipr.matrix.SVD;
import no.uib.cipr.matrix.sparse.ArpackSym;

/**
 * Recommender that uses the second Leicht-Holme-Newman index for predicting links.
 * 
 * Leicht, E.A., Holme, P., Newman, M.E.J., Vertex Similarity in Networks, Physical Review E 73, 026120 (2006)
 * 
 * @author Javier Sanz-Cruzado Puig
 * 
 * @param <U> type of the users.
 */
public class GlobalLHNIndexRecommender<U> extends UserFastRankingRecommender<U>
{

    /**
     * Selection of the direction of the paths from node u
     */
    private final EdgeOrientation uSel;
    /**
     * Selection of the direction of the paths from node v
     */
    private final EdgeOrientation vSel;
    /**
     * Main eigenvalue of the adjacency matrix of the graph.
     */
    private final double eigenvalue;
    /**
     * Auxiliary matrix for computing the scores.
     */
    private final Matrix lhtMatrix;
    
    /**
     * Constructor
     * @param graph Graph of the network.
     * @param phi Controls the balance between the components of the similarity
     * @param uSel Selection of the target user neighbours
     * @param vSel Selection of the candidate user neighbours
     * @throws no.uib.cipr.matrix.NotConvergedException in case it fails.
     */
    public GlobalLHNIndexRecommender(FastGraph<U> graph, double phi, EdgeOrientation uSel, EdgeOrientation vSel) throws Exception 
    {
        super(graph);
        
        this.uSel = uSel;
        this.vSel = vSel;
        
        // Compute the main eigenvalue of the adjacency matrix.
        /*DoubleMatrix2D auxMatrix = this.getGraph().getAdjacencyMatrix(this.uSel);
        EigenvalueDecomposition eigen = new EigenvalueDecomposition(auxMatrix);
        this.eigenvalue = eigen.getRealEigenvalues().viewSorted().get(auxMatrix.columns()-1);
        
        // Initialize the LHT Matrix (I - phi/eigenvalue * A)^-1, where A is the adjacency matrix.
        Algebra alg = new Algebra();
        DoubleMatrix2D identity = DoubleFactory2D.sparse.identity((int) this.getGraph().getVertexCount());
        auxMatrix.assign(Mult.mult(phi/eigenvalue));
        auxMatrix.assign(identity, (x,y) -> y - x);
        this.matrix = alg.inverse(auxMatrix);*/
        this.eigenvalue = computeMaxEigenvalue();
        this.lhtMatrix = computeLHTMatrix(phi);
        
    }

    /**
     * For the undirected case
     * @param graph Graph of the network.
     * @param phi Controls the balance between the components of the similarity
     * @throws Exception In case something fails.
     */
    public GlobalLHNIndexRecommender(FastGraph<U> graph, double phi) throws Exception
    {
        super(graph);
        this.uSel = EdgeOrientation.UND;
        this.vSel = EdgeOrientation.UND;
        
        this.eigenvalue = computeMaxEigenValueSym();
        this.lhtMatrix = computeLHTMatrix(phi);
    }
    
    /**
     * Computes the maximum eigenvalue of the adjacency matrix.
     * @return the maximum eigenvalue
     * @throws NotConvergedException if the value could not be found
     */
    private double computeMaxEigenvalue() throws NotConvergedException
    {
        Matrix matrix = this.getGraph().getAdjacencyMatrixMTJ(uSel);
        DenseMatrix dense = new DenseMatrix(matrix);
        SVD svd = new SVD(matrix.numRows(), matrix.numColumns(),false);
        svd.factor(dense);
        return svd.getS()[0];
    }
    
    /**
     * Compute the maximum eigenvalue in a symmetric matrix
     * @return the maximum eigenvalue in a symmetric matrix
     */
    private double computeMaxEigenValueSym()
    {
        Matrix matrix = this.getGraph().getAdjacencyMatrixMTJ(uSel);
        ArpackSym svd = new ArpackSym(matrix);
        Map<Double, DenseVectorSub> eigen = svd.solve(1, ArpackSym.Ritz.LM);
        
        //Only an eigenvalue is computed
        for(Entry<Double, DenseVectorSub> entry : eigen.entrySet())
        {
            return entry.getKey();
        }
        return 0.0;
    }
    
    /**
     * Computes the Leicht-Home-Newman similarity matrix.
     * @param phi parameter phi of the matrix. 
     * @return the LHT similarity matrix
     */
    private Matrix computeLHTMatrix(double phi)
    {
        Matrix identity = Matrices.identity(this.numUsers());
        if(this.eigenvalue != 0)
        {
            identity.add(-phi/this.eigenvalue, this.getGraph().getAdjacencyMatrixMTJ(uSel));
        
            Matrix aux = Matrices.identity(this.numUsers());
            identity.solve(Matrices.identity(this.numUsers()), aux);
            return aux;
        }
        return identity;
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int uIdx) {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        
        long numEdges = this.getGraph().getEdgeCount();
        long uNeigh = this.getGraph().getNeighbourhoodSize(this.uIndex.uidx2user(uIdx), uSel);
        
        this.uIndex.getAllUidx().forEach(vIdx -> {
            double score = 0.0;
            long vNeigh = this.getGraph().getNeighbourhoodSize(this.uIndex.uidx2user(vIdx), vSel);
            double aux = 2.0*numEdges*eigenvalue/(uNeigh*vNeigh + 1.0);
            if(vIdx == uIdx)
            {
                score += 1-aux;
            }
            score += aux*this.lhtMatrix.get(uIdx, vIdx);
            scores.put(vIdx, score);
        });
        
        return scores;
    }
}
