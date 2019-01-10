/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml;

import cern.colt.matrix.DoubleFactory2D;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix3D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix3D;
import cern.colt.matrix.impl.SparseDoubleMatrix1D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix3D;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.UserFastRankingRecommender;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Combines machine learning techniques and random walks for recommending users.
 * 
 * Backstrom, L., Leskovec, J. Supervised Random Walks: Predicting and Recommending Links in Social Networks. 4th Annual International Conference on Web Search and Data Mining (WSDM 2011), pp. 635-644.
 * 
 * @author Javier Sanz-Cruzado Puig.
 * 
 * @param <U> Type of the users
 */
public class SupervisedRandomWalkRecommender<U> extends UserFastRankingRecommender<U>
{

    /**
     * Set of patterns
     */
    private final Map<U, Map<U, ModifiablePattern>> testPatterns;
    
    /**
     * Weights
     */
    private final ModifiablePattern weights;
    /**
     * Teleport rate for the random walks.
     */
    private final double alpha;
    /**
     * Regularization parameter. Controls the balance between the complexity
     * of the weights vector (weights) and the loss function.
     */
    private final double lambda;
    /**
     * Margin of the lost function
     */
    private final double b;
    
    /**
     * Number of parameters
     */
    private final int numParams;

    /**
     * Convergence threshold.
     */
    private final static double THRESHOLD = 1e-4;
    
    /**
     * Maximum number of iterations.
     */
    private final static double MAXITER = 50;
    /**
     * Maximum number of iterations for PageRank.
     */
    private final static double MAXITERPR = 50;
    
    
    /**
     * Constructor.
     * @param graph The graph.
     * @param train Patterns for the training
     * @param test Patterns for the test.
     * @param classes classes for the training.
     * @param numparams Number of parameters of the instances.
     * @param alpha The teleport rate.
     * @param lambda the regularization parameter.
     * @param b margin of the loss function
     */
    public SupervisedRandomWalkRecommender(FastGraph<U> graph, Map<U, Map<U,ModifiablePattern>> train, Map<U, Map<U,ModifiablePattern>> test, Map<U,Pair<List<U>>> classes, int numparams, double alpha, double lambda, double b) 
    {
        super(graph);
        this.testPatterns = test;
        
        this.numParams = numparams;
        this.weights = new ModifiablePattern(numparams);
        this.alpha = alpha;
        this.lambda = lambda;
        this.b = b;
        this.train(train, classes);
        

    }

    @Override
    public Int2DoubleMap getScoresMap(int uidx) 
    {
        U user = this.uidx2user(uidx);
        Int2DoubleMap scoresMap = new Int2DoubleOpenHashMap();
        // Compute the Google Matrix and its derivatives.
        DoubleMatrix2D Q = this.computeQ(user,this.testPatterns);

        
        
        // Vectors where we are storing the values of PageRank.
        DoubleMatrix1D oldPR;
        DoubleMatrix1D newPR = new SparseDoubleMatrix1D(this.numUsers());
        
        uIndex.getAllUidx().forEach(uIdx -> {
            newPR.setQuick(uIdx, 1.0/(this.numUsers()+0.0));
        });
        
        boolean convergedPR = false;

        // Estimation of PageRank
        while(!convergedPR)
        {
            oldPR = newPR;
            double diff = estimatePRIteration(newPR, oldPR, Q);
            if(diff < THRESHOLD)
                convergedPR = true;
        }
        
        IntStream.range(0, newPR.size()).forEach(i -> scoresMap.put(i, newPR.getQuick(i)));
        
        return scoresMap;
    }

    /**
     * Trains the supervised algorithm.
     * @param train The training instances.
     * @param classes The classes.
     */
    private void train(Map<U, Map<U, ModifiablePattern>> train, Map<U,Pair<List<U>>> classes) 
    {
        // Initialize the weights at random
        Random r = new Random();
        double LEARNINGRATE = 0.01;
        for(int i = 0; i < numParams; ++i)
        {
            weights.setValue(i, r.nextDouble());
        }
        
        boolean convergence = false;
        int numIter = 0;

        // Initializing the hessian approximation.
        DoubleMatrix2D hessian = DoubleFactory2D.sparse.identity(this.numParams);
        // Initialize weight vector
        DoubleMatrix1D x = new SparseDoubleMatrix1D(this.numParams);
        // Initialize derivate vector.
        DoubleMatrix1D y = new SparseDoubleMatrix1D(this.numParams);
        // Previous derivations.
        DoubleMatrix1D oldDerF = new SparseDoubleMatrix1D(this.numParams);
        // Increase of the estimated parameter.
        DoubleMatrix1D Ax = new SparseDoubleMatrix1D(this.numParams);
        /*for(int i = 0; i < this.numParams; ++i)
        {
            x.setQuick(i,this.weights.getValue(i));
            hessian.setQuick(i, i, 1.0/this.weights.getValue(i));
        }*/
        
        //Algebra algebra = new Algebra();
        
        // Computations of the Quasi-Newton BFGS algorithm for optimization of the weights.
        /*while(!convergence)
        {
            // Compute the derivatives of the F function.
            ModifiablePattern derivativesF = calculateDerivativesF(train, classes);   
            
            for(int i = 0; i < numParams; ++i)
            {
                // Obtain the y_k vector
                y.setQuick(i,derivativesF.getValue(i)-oldDerF.getQuick(i));
                // Update the derivative (gradient f(x_k))
                oldDerF.setQuick(i, derivativesF.getValue(i));
            }
            
            // Compute the new Hessian
            DoubleMatrix2D aux = new SparseDoubleMatrix2D(this.numParams, this.numParams);
            aux = algebra.multOuter(Ax,y,aux);
            
            DoubleMatrix2D aux2 = new SparseDoubleMatrix2D(this.numParams, this.numParams);
            aux2 = algebra.multOuter(y, Ax, aux2);
            
            double innerProd = algebra.mult(y, Ax);
            aux.assign(Mult.div(-innerProd));
            aux2.assign(Mult.div(-innerProd));
            
            // Finish the computations for the auxiliar matrices
            aux.assign(DoubleFactory2D.sparse.identity(this.numParams), (double x1, double y1) -> x1 + y1);
            aux2.assign(DoubleFactory2D.sparse.identity(this.numParams), (double x1, double y1) -> x1 + y1);
            
            DoubleMatrix2D aux3 = new SparseDoubleMatrix2D(this.numParams, this.numParams);
            aux3 = algebra.multOuter(Ax, Ax, aux3);
            
            aux = algebra.mult(aux, hessian);
            aux = algebra.mult(aux, aux2);
            aux.assign(aux3, (double x1, double y1) -> x1 + y1);
            hessian = aux;
            
            Ax = algebra.mult(hessian, oldDerF);
            double diff = 0.0;
            for(int i = 0; i < numParams; ++i)
            {
                x.setQuick(i, x.getQuick(i)-Ax.getQuick(i));
                diff += Math.abs(Ax.getQuick(i));
            }
            
            numIter++;
            
            // Check the convergence condition
            if(numIter > MAXITER || diff < THRESHOLD)
            {
                convergence = true;
            }
            
            for(int i = 0; i < numParams; ++i)
            {
                weights.setValue(i, x.getQuick(i));
            }
            
            System.out.println("Num Iter.: " + numIter + " diff: " + diff);
        }*/
        while(!convergence)
        {
            System.out.println("Starting iteration " + numIter);
            // Compute the derivatives of the F function.
            ModifiablePattern derivativesF = calculateDerivativesF(train, classes);   
            double diff= 0;
            
            for(int i = 0; i < numParams; ++i)
            {
                weights.setValue(i, weights.getValue(i) - LEARNINGRATE*derivativesF.getValue(i));
                diff += Math.abs(LEARNINGRATE*derivativesF.getValue(i));
            }
            
            if(numIter > MAXITER || diff < THRESHOLD)
            {
                convergence = true;
            }
            System.out.println("Num Iter.: " + numIter + " diff: " + diff);
            numIter++;
        }
        
    }

    /**
     * Calculates the derivative of the target function to minimize
     * @param train training patterns.
     * @param classes classes  for each pattern.
     * @return An instance containing the derivatives of the F function.
     */
    private ModifiablePattern calculateDerivativesF(Map<U, Map<U,ModifiablePattern>> train, Map<U,Pair<List<U>>> classes) 
    {
        System.out.println("Computing F derivatives");
        ModifiablePattern derivatives = new ModifiablePattern(this.numParams);
        
        // The estimated pageranks for every user
        DoubleMatrix2D estimatedPageRanks = new DenseDoubleMatrix2D(this.uIndex.numUsers(), this.uIndex.numUsers());
        // The estimated derivatives of page rank for every user
        DoubleMatrix3D estimatedPageRanksDer = new DenseDoubleMatrix3D(this.uIndex.numUsers(), this.uIndex.numUsers(), this.numParams);
        
        // Compute PageRank and its derivatives
        uIndex.getAllUsers().forEach(user -> {
            optimizePageranks(user, estimatedPageRanks, estimatedPageRanksDer, train);
        });
        
        
        // Compute the derivatives
        IntStream.range(0, this.numParams).forEach(w-> 
        {
            double value = 2*weights.getValue(w);
            value += this.lambda * uIndex.getAllUsers().mapToDouble(u -> 
            {
                int uidx = uIndex.user2uidx(u);
                
                double dSum = 0.0;
                if(classes.get(u).v1() != null)
                {
                    dSum = classes.get(u).v1().stream().mapToDouble(d -> 
                    {
                        int didx = uIndex.user2uidx(d);
                        double pd = estimatedPageRanks.get(uidx, didx);
                        double dpd = estimatedPageRanksDer.get(uidx, didx, w);
                        double lSum = classes.get(u).v1().stream().mapToDouble(l -> 
                        {
                            int lidx = uIndex.user2uidx(l);
                            double pl = estimatedPageRanks.get(uidx, lidx);
                            double dpl = estimatedPageRanksDer.get(uidx, lidx, w);
                            return this.dh(pl - pd)*(dpl-dpd);
                        }).sum();
                        return lSum;
                    }).sum();
                }
                return dSum;
            }).sum();
            
            
            derivatives.setValue(w, value); 
        });
        
        System.out.println("Computed F derivatives");
        return derivatives;
        
    }

    /**
     * Computes the pageranks for a given user.
     * @param user The corresponding user.
     * @param estimatedPageRanks A matrix where the pageRanks will be stored
     * @param estimatedPageRanksDer A tensor where the pageRanks derivatives will be stored.
     * @param train The training instances.
     */
    private void optimizePageranks(U user, DoubleMatrix2D estimatedPageRanks, DoubleMatrix3D estimatedPageRanksDer, Map<U, Map<U,ModifiablePattern>> train) {
        int index = this.uIndex.user2uidx(user);
        
        System.out.println("Computing pageranks " + user);
        int numIter = 0;
        // Compute the Google Matrix and its derivatives.
        long a = System.currentTimeMillis();
        DoubleMatrix2D Q = this.computeQ(user,train);
        long bt = System.currentTimeMillis();
        System.out.println("Computed Q (" + (bt-a) + " ms.)");
        DoubleMatrix3D dQ = this.computeDerivQ(user,train);
        a = System.currentTimeMillis();
        System.out.println("Computed dQ (" + (a-bt) + " ms.)");

        List<Integer> usersIndex = uIndex.getAllUidx().boxed().collect(Collectors.toCollection(ArrayList::new));
        
        // Vectors where we are storing the values of PageRank.
        DoubleMatrix1D oldPR = new SparseDoubleMatrix1D(this.numUsers());
        DoubleMatrix1D newPR = new SparseDoubleMatrix1D(this.numUsers());
        
        // Vectors where we are storing the values of the PageRank derivatives.
        DoubleMatrix2D oldPRderiv = new SparseDoubleMatrix2D(this.numUsers(), this.numParams);
        DoubleMatrix2D newPRderiv = new SparseDoubleMatrix2D(this.numUsers(), this.numParams);
        
        for(int uIdx : usersIndex)
        {
            if(uIdx < 0 || index < 0 || uIdx >= estimatedPageRanks.columns() || index >= estimatedPageRanks.rows())
            {
                System.out.println("AQUI FALLA");
            }
            
            double value = 1.0/(this.numUsers()+0.0);
            estimatedPageRanks.setQuick(index, uIdx, value);
            
            oldPR.setQuick(uIdx, 1.0/(this.numUsers()+0.0));
        }
        
        for(int i = 0; i < oldPR.size(); ++i)
        {
            oldPR.setQuick(i, 1.0/(this.numUsers()+0.0));
        }
        
        boolean convergedPR = false;
        boolean convergedDerPR = false;
        
        
        // Estimation of PageRank
        while(!convergedPR)
        {
            long c = System.currentTimeMillis();
            
            // PageRank Iteration. Store in new
            double diff = 0;
            for(int uIdx : usersIndex)
            {
                double value = 0.0;
                for(int vIdx : usersIndex)
                {
                    double pr = oldPR.getQuick(vIdx);
                    double aux = Q.getQuick(vIdx, uIdx);
                    aux = aux*pr;
                    value += aux;
                }
                
                newPR.setQuick(uIdx, value);
                diff += Math.abs(oldPR.getQuick(uIdx) - value);
            }

            if(diff < THRESHOLD || numIter > MAXITERPR)
                convergedPR = true;
            long d = System.currentTimeMillis();
            
            System.out.println("Finished pr iteration " + numIter + "("  + (d-c) + " ms.)");
            long e = System.currentTimeMillis();
            if(!convergedDerPR)
            {
                for(int uIdx : usersIndex)
                {
                    diff = 0;
                    for(int wIdx = 0; wIdx < this.numParams; ++wIdx)
                    {
                        double value = 0.0;
                        for(int vIdx : usersIndex)
                        {
                            value += Q.getQuick(vIdx, uIdx)*oldPRderiv.getQuick(vIdx, wIdx) + oldPR.getQuick(vIdx)*dQ.getQuick(vIdx, uIdx, wIdx);
                        }
                        
                        newPRderiv.setQuick(uIdx, wIdx, value);
                        diff+= Math.abs(oldPRderiv.get(uIdx,wIdx) - value);
                    }
                }
                
                if(diff < THRESHOLD || numIter > MAXITERPR)
                    convergedDerPR = true;
            }
            
            long f = System.currentTimeMillis();
            System.out.println("Finished dpr iteration " + numIter + "("  + (f-e) + " ms.)");
            numIter++;
            
            
            oldPR = newPR;
            newPR = new SparseDoubleMatrix1D(this.numUsers());
            oldPRderiv = newPRderiv;
            newPRderiv = new SparseDoubleMatrix2D(this.numUsers(),this.numParams);
        }
        
        // Store the new PageRanks
        
        for(int uIdx : usersIndex)
            estimatedPageRanks.setQuick(index, uIdx, newPR.get(uIdx));
        
        // Complete the estimation of the derivatives
        while(!convergedDerPR)
        {
            double diff = 0.0;
            
            for(int uIdx : usersIndex)
            {
                for(int wIdx = 0; wIdx < this.numParams; ++wIdx)
                {
                   double value = 0.0;
                   for(int vIdx : usersIndex)
                   {
                       value += Q.getQuick(vIdx,uIdx)*oldPRderiv.getQuick(vIdx,wIdx) + oldPR.get(vIdx)*dQ.getQuick(vIdx,uIdx,wIdx);
                   }
                   
                   newPRderiv.setQuick(uIdx, wIdx, value);
                   diff += Math.abs(oldPRderiv.get(uIdx, wIdx) - value);
                }
            }
            
            if(diff < THRESHOLD || numIter > MAXITERPR)
                convergedDerPR = true;
            
            for(int i = 0; i < newPRderiv.rows(); ++i)
            {
                for(int j = 0; j < newPRderiv.columns(); ++j)
                {
                    oldPRderiv.setQuick(i, j, newPRderiv.getQuick(i, j));
                }
            }
            numIter++;
        }
        
        // Store the new PageRank derivatives
        for(int uIdx : usersIndex) 
        {
            for(int wIdx = 0; wIdx < this.numParams; ++wIdx) 
            {
                estimatedPageRanksDer.setQuick(index, uIdx, wIdx, newPRderiv.get(uIdx, wIdx));
            }
        }
        
        bt = System.currentTimeMillis();
        System.out.println("Computed pagerank " + user + " (" + (bt-a) + " ms.)");
    }

    /**
     * Executes an iteration of the estimation of the Weighted PageRank.
     * @param newPR New pageRank. It is the value to update.
     * @param oldPR Old pageRank.
     * @param Q The matrix
     * @return The difference between old and new pageranks.
     */
    public double estimatePRIteration(DoubleMatrix1D newPR, DoubleMatrix1D oldPR, DoubleMatrix2D Q)
    {
        double diff = uIndex.getAllUidx().mapToDouble(uIdx -> 
        {
            double value = uIndex.getAllUidx().mapToDouble(i -> 
            {
                double pr = oldPR.getQuick(i);
                double aux = Q.getQuick(i, uIdx);
                aux = aux * pr;
                return aux;
            }).sum();
            newPR.setQuick(uIdx, value);
            return Math.abs(oldPR.get(uIdx) - value);            
        }).sum();
        return diff;
    }
    
    /**
     * Executes an iteration of the estimation of the Weighted PageRank derivatives.
     * @param newPRderiv A matrix where to store the new estimation.
     * @param oldPRderiv The previous estimation.
     * @param pr The corresponding pageRank estimation
     * @param Q The Google Matrix
     * @param dQ The derivatives of the Google Matrix
     * @return The difference between old and new PageRank derivatives.
     */
    public double estimateDerPRIteration(DoubleMatrix2D newPRderiv, DoubleMatrix2D oldPRderiv, DoubleMatrix1D pr, DoubleMatrix2D Q, DoubleMatrix3D dQ)
    {
        return uIndex.getAllUidx().mapToDouble(uIdx -> 
        {
           return IntStream.range(0, this.numParams).mapToDouble(wIdx -> {
               double value = uIndex.getAllUidx().mapToDouble(vIdx -> {
                   return Q.get(vIdx, uIdx)*oldPRderiv.get(vIdx, wIdx) + pr.get(vIdx)*dQ.getQuick(vIdx,uIdx,wIdx);
               }).sum();
               
               newPRderiv.setQuick(uIdx, wIdx, value);
               return Math.abs(oldPRderiv.get(uIdx, wIdx) - value);
           }).sum();
        }).sum();
    }
    
    
    
    /**
     * Computes the Weighted PageRank Google Matrix.
     * @param user The source user.
     * @param train The training set.
     * @return A matrix containing the Google matrix.
     */
    private DoubleMatrix2D computeQ(U user, Map<U, Map<U,ModifiablePattern>> train) {
        DoubleMatrix2D functionValues = new SparseDoubleMatrix2D(this.uIndex.numUsers(), this.uIndex.numUsers());
        int useridx = this.uIndex.user2uidx(user);
        
        // Construction of the transition matrix for computing PageRank.
        this.getGraph().getAllNodes().forEach(u -> 
        {
            int uIdx = this.uIndex.user2uidx(u);
            if(train.containsKey(u))
            {
                double sum = train.get(u).keySet().stream().map(v -> 
                {
                    int vIdx = this.uIndex.user2uidx(v);
                    double fn = this.f(train.get(u).get(v));
                    functionValues.setQuick(uIdx, vIdx, fn);
                    return fn;
                    
                }).reduce(0.0, (x,y)-> x + y);
                
                
                // Multiply the values for the probability of not teleporting.
                train.get(u).keySet().stream().forEach(v -> 
                {
                    int vIdx = this.uIndex.user2uidx(v);
                    functionValues.setQuick(uIdx, vIdx, functionValues.getQuick(uIdx,vIdx) / sum);
                    double value = (1.0- this.alpha) * functionValues.getQuick(uIdx, vIdx);
                    functionValues.setQuick(uIdx, vIdx, value);
                });
                
                // Add the restart probability to the node.
                functionValues.setQuick(uIdx, useridx, functionValues.getQuick(uIdx, useridx) + this.alpha);
            }
            else // Sink treatment.
            {
                functionValues.setQuick(uIdx, useridx, 1.0);
            }
        });       

        return functionValues;
    }

    /**
     * Computes the derivatives of the Google matrix respect to the weights.
     * @param user the user
     * @param train Training patterns
     * @return A tensor containing all the derivatives value. Rows and columns represent
     * the users of the network, and the slices represent each weight.
     */
    private DoubleMatrix3D computeDerivQ(U user, Map<U, Map<U,ModifiablePattern>> train) {
        
        DoubleMatrix3D tensor = new SparseDoubleMatrix3D(this.numUsers(), this.numItems(), numParams);

        uIndex.getAllUsers().forEach(u -> 
        {
            int uIdx = uIndex.user2uidx(u);
            List<Integer> index = new ArrayList<>();
            List<Double> fn = new ArrayList<>();
            List<ModifiablePattern> dFn = new ArrayList<>();
            
            if(train.containsKey(u))
            {
                // Obtain the values for the f function and its derivatives.
                train.get(u).entrySet().forEach(v ->
                {
                    index.add(uIndex.user2uidx(v.getKey()));
                    fn.add(this.f(v.getValue()));
                    dFn.add(this.df(v.getValue()));
                });
                
                // Obtain the sums
                double sumFn = fn.stream().mapToDouble(val -> val).sum();
                ModifiablePattern sumDfn = new ModifiablePattern(numParams);
                IntStream.range(0, this.numParams).forEach(i ->
                {
                    sumDfn.setValue(i, dFn.stream().mapToDouble(ins -> ins.getValue(i)).sum());
                });
                
                
                // Compute the values of the derivatives and store them in the tensor.
                
                IntStream.range(0, index.size()).forEach(i -> 
                {
                    int vIdx = index.get(i);
                    IntStream.range(0, numParams).forEach(wIdx -> 
                    {
                       try
                       {
                           if(Objects.equals(dFn, null))
                           {
                               System.err.println("dFn is null");
                           }
                           else if(Objects.equals(dFn.get(i), null))
                           {
                               System.err.println("dFn " + i + " is null");
                           }
                           else if(Objects.equals(dFn.get(i).getValue(wIdx), null))
                           {
                               System.err.println("dFn " + i + " " + wIdx + " is null");
                           }
                           else if(Objects.equals(sumFn, null))
                           {
                               System.err.println("sumFn is null");
                           }
                           else if(Objects.equals(fn, null))
                           {
                               System.err.println("fn is null");
                           }
                           else if(Objects.equals(fn.get(i), null))
                           {
                               System.err.println("fn " + i + " is null");
                           }
                           else if(Objects.equals(sumDfn, null))
                           {
                               System.err.println("sumDfn is null");
                           }
                           else if(Objects.equals(sumDfn.getValue(wIdx), null))
                           {
                               System.err.println("sumDfn " + wIdx + " is null");
                           }
                           

                          
                            double value = dFn.get(i).getValue(wIdx)*sumFn - fn.get(i)*sumDfn.getValue(wIdx);
                            value *= (1-this.alpha)/(sumFn*sumFn);

                            tensor.setQuick(uIdx, vIdx, wIdx, value);
                           }
                       catch(IndexOutOfBoundsException e)
                       {
                           System.out.println("uIdx " + uIdx + " vIdx " + vIdx + " wIdx " + wIdx );
                           throw e;
                       }
                    });
                });
            }
            
            // In the rest of cases, as the matrix is sparse, the value will be zero.
        });
        
        return tensor;
    }

    /**
     * Computes the edge strength function
     * @param instance Argument of the function.
     * @return The edge strength.
     */
    private double f(ModifiablePattern instance) 
    {
        double value = 0.0;
        if(instance != null)
        {
            for(int i = 0; i < this.numParams; ++i)
            {
                value += this.weights.getValue(i)*instance.getValue(i);
            }
            
            value = 1.0 / (1.0 + Math.exp(-value));
        }
        return value;
    }
    
    /**
     * Computes the derivatives of the f function, and evaluates them in a given point.
     * @param instance The given point.
     * @return An instance containing the value of the derivatives.
     */
    private ModifiablePattern df(ModifiablePattern instance)
    {
        ModifiablePattern deriv = new ModifiablePattern(numParams);
        for(int i = 0; i < numParams; ++i)
            deriv.setValue(i, this.f(instance)*(1-this.f(instance)*instance.getValue(i)));
        return deriv;
    }
    
    /**
     * Computes the loss function
     * @param value The point we want to compute the function for
     * @return The value of the loss function
     */
    private double h(double value)
    {
        return 1.0/(1.0+Math.exp(-value/this.b));
    }
    
    /**
     * Computes the derivative of the loss function
     * @param value The point we want to compute the function for
     * @return The value of the derivative of the lost function.
     */
    private double dh(double value)
    {
        return h(value)*(1-h(value))/this.b;
    }
}
