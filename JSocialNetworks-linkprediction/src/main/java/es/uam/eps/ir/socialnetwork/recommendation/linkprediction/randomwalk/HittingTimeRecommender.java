/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.linkprediction.randomwalk;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import es.uam.eps.ir.socialnetwork.graph.DirectedGraph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.stream.IntStream;
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.LinkedSparseMatrix;

/**
 * Hitting time recommender. Computes the recommendation score as the average time a
 * random walker travels from u to v (also known as hitting time or mean passage time).
 * 
 * Liben-Nowell, D., Kleinberg, J. The Link Prediction Problem for Social Networks, CIKM, 2003
 * 
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class HittingTimeRecommender<U> extends UserFastRankingRecommender<U> {

    /**
     * Hitting time scores.
     */
    protected Matrix hittingTime;
    /**
     * Mean first passage matrix
     */
    protected DoubleMatrix2D meanFirstPassageMatrix;
    
    /**
     * Constructor
     * @param graph the graph.
     */
    public HittingTimeRecommender(FastGraph<U> graph) {
        super(graph);
        computeMatrix();
    }
    
    /**
     * Constructor.
     * @param graph the graph.
     * @param hittingTime the matrix containing the distances between pairs.
     */
    public HittingTimeRecommender(FastGraph<U> graph, Matrix hittingTime)
    {
        super(graph);
        this.hittingTime = hittingTime;
    }

    /**
     * Obtains the hitting time matrix
     * @return the hitting time matrix.
     */
    public Matrix getMatrix()
    {
        return hittingTime;
    }
    /**
     * Computes the first passage matrix for the graph
     */
    /*private void computeMatrix()
    {
        // Small probability
        double lambda = 0.001/this.numUsers();
        Algebra alg = new Algebra();

        DoubleMatrix2D transition = new SparseDoubleMatrix2D(this.uIndex.numUsers(), this.uIndex.numUsers());
        DoubleMatrix2D adjacency = this.getGraph().getAdjacencyMatrix(EdgeOrientation.OUT);
        
        // First, we compute the transition matrix
        for(int i = 0; i < this.uIndex.numUsers(); ++i)
        {
            int degree;
            if(this.getGraph().isDirected())
            {
                DirectedGraph<U> dg = (DirectedGraph<U>) this.getGraph();
                degree = dg.outDegree(uIndex.uidx2user(i));
            }
            else
            {
                degree = this.getGraph().degree(uIndex.uidx2user(i));
            }
            
            if(degree == 0)
            {
                for(int j = 0; j < this.uIndex.numUsers(); ++j)
                {
                    transition.setQuick(i, j, 1.0/(this.uIndex.numUsers() + 0.0));
                }
            }
            else
            {
                for(int j = 0; j < this.uIndex.numUsers(); ++j)
                {
                    transition.setQuick(i, j, adjacency.getQuick(i, j)/(degree+0.0));
                }
            }
        }
        
        transition.assign(Functions.mult(1-lambda));
        DoubleMatrix2D aux = DoubleFactory2D.sparse.make(this.numUsers(), this.numUsers(), lambda/(this.numUsers()+0.0));
        transition.assign(aux, (x,y)->x+y);

        // Once we have the transition matrix, it is time to compute the necessary matrices for computing the mean first
        // passage time matrix.
        
        // Compute A = I - T
        DoubleMatrix2D AMatrix = DoubleFactory2D.sparse.identity(this.numUsers());
        AMatrix.assign(transition, Functions.minus);
        
        // Compute matrix U and vector d
        DoubleMatrix2D U = AMatrix.viewPart(0, 0, AMatrix.rows()-1, AMatrix.columns()-1);
        DoubleMatrix1D d = AMatrix.viewRow(AMatrix.rows()-1).viewPart(0, AMatrix.columns()-1);
        
        // Compute the inverse of the matrix U
        DoubleMatrix2D invU = alg.inverse(U);
        
        // Compute the vector h
        DoubleMatrix1D h = alg.mult(alg.transpose(invU), d);
                
        // Compute the beta constant
        double beta = 1 - h.zSum();
        
        // Compute the stationary distribution
        DoubleMatrix1D weight = new SparseDoubleMatrix1D(AMatrix.columns());
        int i;
        for(i = 0; i < weight.size()-1; ++i)
        {
            weight.setQuick(i, -h.getQuick(i)/beta);
        }
        weight.setQuick(i, 1/beta);
        
        DoubleMatrix2D auxWeight = DoubleFactory2D.sparse.make(weight.toArray(), 1);
        // Compute the product of matrices AA#
        DoubleMatrix2D W = DoubleFactory2D.sparse.repeat(auxWeight,AMatrix.rows(),1);
        
        DoubleMatrix2D AA = DoubleFactory2D.sparse.identity(AMatrix.rows());
        AA.assign(W, Functions.minus);
        
        // Compute the pseudoinverse of matrix A
        DoubleMatrix2D AHash = new SparseDoubleMatrix2D(AMatrix.rows(), AMatrix.columns());
        for(i = 0; i < U.rows(); ++i)
        {
            for(int j = 0; j < invU.columns(); ++j)
            {
                AHash.setQuick(i, j, invU.getQuick(i, j));
            }
        }
        AHash = alg.mult(AA, AHash);
        AHash = alg.mult(AHash, AA);
        
        DoubleMatrix2D AHashDg = DoubleFactory2D.sparse.diagonal(DoubleFactory2D.sparse.diagonal(AHash));
        
        DoubleMatrix2D J = DoubleFactory2D.sparse.make(AHash.rows(), AHash.columns(), 1);
        J = alg.mult(J, AHashDg);
        J.assign(AHash, Functions.minus);
        J.assign(DoubleFactory2D.sparse.identity(AHash.rows()), Functions.plus);
        
        
        DoubleMatrix2D Pi = new SparseDoubleMatrix2D(AHash.rows(), AHash.columns());
        for(i = 0; i < AHash.rows(); ++i)
        {
            Pi.setQuick(i, i, 1.0/weight.getQuick(i));
        }
        this.meanFirstPassageMatrix = alg.mult(J,Pi);       
        
    }*/
    
    
    
    /**
     * Computes the first passage matrix for the graph
     */
    private void computeMatrix()
    {
        // Small probability
        double lambda = 0.001/this.numUsers();
        Algebra alg = new Algebra();

        Matrix transition = new LinkedSparseMatrix(this.uIndex.numUsers(), this.uIndex.numUsers());
        Matrix adjacency = this.getGraph().getAdjacencyMatrixMTJ(EdgeOrientation.OUT);
        
        // First, we compute the transition matrix
        for(int i = 0; i < this.uIndex.numUsers(); ++i)
        {
            int degree;
            if(this.getGraph().isDirected())
            {
                DirectedGraph<U> dg = (DirectedGraph<U>) this.getGraph();
                degree = dg.outDegree(uIndex.uidx2user(i));
            }
            else
            {
                degree = this.getGraph().degree(uIndex.uidx2user(i));
            }
            
            if(degree == 0)
            {
                for(int j = 0; j < this.uIndex.numUsers(); ++j)
                {
                    transition.set(i, j, 1.0/(this.uIndex.numUsers() + 0.0));
                }
            }
            else
            {
                for(int j = 0; j < this.uIndex.numUsers(); ++j)
                {
                    transition.set(i, j, lambda/(this.numUsers()+0.0) + (1-lambda)* adjacency.get(i, j)/(degree+0.0));
                }
            }
        }
        
        // Once we have the transition matrix, it is time to compute the necessary matrices for computing the mean first
        // passage time matrix.
        
        // Compute A = I - T
        Matrix AMatrix = Matrices.identity(this.numUsers());
        AMatrix.add(-1,transition);
        
        
        // Compute matrix U and vector d
        int[] selection = IntStream.range(0, this.numUsers()-1).toArray();
        Matrix U = new DenseMatrix(AMatrix.numRows()-1, AMatrix.numColumns()-1);
        for(int i = 0; i < AMatrix.numRows()-1;++i)
        {
            for(int j=0; j < AMatrix.numColumns()-1;++j)
            U.set(i,j,AMatrix.get(i,j));
        }
        Matrix aux = Matrices.identity(AMatrix.numRows());
        Vector d = Matrices.getSubVector(Matrices.getColumn(AMatrix.transpose(aux),this.numUsers()-1),selection);
        
        Matrix invU = U.solve(Matrices.identity(this.numUsers()-1), Matrices.identity(this.numUsers()-1));
        
        // Compute the inverse of the matrix U
        
        // Compute the vector h
        
        Vector h = new DenseVector(this.uIndex.numUsers()-1);
        invU.transMult(d, h);
                
        // Compute the beta constant
        double beta = 1.0;
        for(int i = 0; i < h.size(); ++i)
        {
            beta -= h.get(i);
        }
        
        // Compute the stationary distribution
        Vector weight = new DenseVector(AMatrix.numRows());
        int i;
        for(i = 0; i < weight.size()-1; ++i)
        {
            weight.set(i, -h.get(i)/beta);
        }
        weight.set(i, 1/beta);
        
        Matrix W = new DenseMatrix(AMatrix.numRows(),AMatrix.numRows());
        for(int j = 0; j < AMatrix.numRows(); ++j)
        {
            for(int k = 0; k < AMatrix.numRows(); ++k)
            {
                AMatrix.set(j,k,weight.get(k));
            }
        }
        
        Matrix AA = Matrices.identity(AMatrix.numRows());
        AA.add(-1, W);
                
        // Compute the pseudoinverse of matrix A
        Matrix AHash = new LinkedSparseMatrix(AMatrix.numRows(), AMatrix.numColumns());
        for(i = 0; i < U.numRows(); ++i)
        {
            for(int j = 0; j < invU.numColumns(); ++j)
            {
                AHash.set(i, j, invU.get(i, j));
            }
        }
        AA.mult(AHash, aux);
        aux.mult(AA, AHash);
        
        Matrix AHashDg = new LinkedSparseMatrix(AMatrix.numRows(), AMatrix.numColumns());
        for(i = 0; i < AHash.numRows(); ++i)
        {
            AHashDg.set(i,i,AHash.get(i, i));
        }
        
        Matrix J = new DenseMatrix(AMatrix.numRows(), AMatrix.numColumns());
        for(i = 0; i < J.numRows(); ++i)
        {
            for(int j = 0; j < J.numColumns(); ++j)
            {
                J.set(i,j,1.0);
            }
        }
        
        
        J.mult(AHashDg, aux);
        J = aux;
        J.add(-1.0, AHash);
        J.add(Matrices.identity(J.numRows()));
        
        
        Matrix Pi = new DenseMatrix(AHash.numRows(), AHash.numColumns());
        for(i = 0; i < AHash.numRows(); ++i)
        {
            Pi.set(i, i, 1.0/weight.get(i));
        }
        this.hittingTime = Matrices.identity(this.numUsers());
        J.mult(Pi,this.hittingTime);       
        
    }
    
    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        this.uIndex.getAllUidx().forEach(uidx -> {
            scores.put(uidx, -this.hittingTime.get(i,uidx));
        });
                
        return scores;
    }
}
