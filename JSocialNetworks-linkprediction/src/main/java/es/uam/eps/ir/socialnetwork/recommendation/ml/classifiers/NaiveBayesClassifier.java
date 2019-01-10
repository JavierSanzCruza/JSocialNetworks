/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml.classifiers;

import es.uam.eps.ir.socialnetwork.recommendation.ml.attributes.AttrType;
import es.uam.eps.ir.socialnetwork.recommendation.ml.Classifier;
import es.uam.eps.ir.socialnetwork.recommendation.ml.Pattern;
import es.uam.eps.ir.socialnetwork.recommendation.ml.PatternSet;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.ranksys.core.util.Stats;

/**
 * Classifier which applies the Naive Bayes method. In case the attributes are continuous,
 * Gaussian Naive Bayes is applied to compute the scores.
 * 
 * Note: For computing the means and variances, an incremental algorithm is used. This algorithm
 * is documented in:
 * 
 * 
 * 
 * @author Javier Sanz-Cruzado Puig
 */
public class NaiveBayesClassifier implements Classifier
{    
    /**
     * Indicates if the training of the classifier has been done.
     */
    private boolean trained;
    
    /**
     * Times each class appears in the training set.
     */
    private List<Double> priori;
    
    /**
     * A list of matrices which contains the information for each attribute in the training set.
     * For each matrix, the columns represent the different classes.
     * 
     * In the case of nominal attributes, each row represents a different value of the attributes,
     * and each cell contains the number of different examples which share the same value of the attribute
     * and class.
     * 
     * In the case of continuous attributes, first row represents the mean of the sample, and second row
     * represents the variance of the sample.
     */
    private List<Double[][]> frequencies;
    /**
     * List of classes
     */
    private List<String> classes;
    /**
     * List of attributes
     */
    private List<Tuple2oo<String, AttrType>> attributes;
    
    /**
     * Constant index for the mean
     */
    private final static int MEAN = 0;
    /**
     * Constant index for the variance
     */
    private final static int SIGMA = 1;
    /**
     * Number of parameters in 
     */
    private final static int NUMCONT = 2;
    /**
     * The number of patterns
     */
    private int numPatterns;
    /**
     * The stats for each attribute
     */
    private List<Stats> stats;
    /**
     * Indicates if attributes have to be normalized or not.
     */
    private final boolean normalize;

    /**
     * Constructor.
     */
    public NaiveBayesClassifier() 
    {
       this.trained = false;
       this.normalize = false;
    }
    
    /**
     * Constructor
     * @param normalize indicates if attributes have to be normalized
     */
    public NaiveBayesClassifier(boolean normalize)
    {
        this.normalize = normalize;
    }

    @Override
    public void train(PatternSet trainSet) {
        
        if(normalize)
            this.stats = trainSet.getStats();
        
        
        this.trained = false;
        // Store the classes.
        this.classes = trainSet.getClasses();
        int nClasses = trainSet.getNumClasses();
                
        this.priori = new ArrayList<>();
        for(int i = 0; i < nClasses; ++i)
            this.priori.add(0.0);
        
        this.attributes = trainSet.getAttributes();
        int nAttrib = trainSet.getNumAttrs();
        this.frequencies = new ArrayList<>();
        for(int i = 0; i < trainSet.getNumAttrs(); ++i)
        {
            if(attributes.get(i).v2().equals(AttrType.NOMINAL))
            {
                int numValues = trainSet.numValues(attributes.get(i).v1());
                this.frequencies.add(new Double[numValues + 1][nClasses]);
                for(int j = 0; j < numValues + 1; ++j)
                {
                    for(int k = 0; k < nClasses; ++k)
                    {
                        this.frequencies.get(i)[j][k] = 0.0;
                    }
                }
            }
            else //if(attributes.get(i).getSecond().equals(AttrType.CONTINUOUS))
            {
                this.frequencies.add(new Double[NUMCONT][nClasses]);
                for(int j = 0; j < nClasses; ++j)
                {
                     this.frequencies.get(i)[MEAN][j] = 0.0;
                     this.frequencies.get(i)[SIGMA][j] = 0.0;
                }
            }
        }
        
        this.numPatterns = trainSet.getNumPatterns();
        
        trainSet.getPatterns().forEach(pattern -> 
        {
            int category = pattern.getCategory();
            // Update the prioris
            this.priori.set(category, this.priori.get(category) + 1.0);
            
            for(int i = 0; i < nAttrib; ++i)
            {
                
                if(this.attributes.get(i).v2().equals(AttrType.NOMINAL))
                {
                    int atribValue = pattern.getValues().get(i).intValue();
                    this.frequencies.get(i)[atribValue][category]++;
                    this.frequencies.get(i)[this.frequencies.get(i).length - 1][category]++; // Counters
                }
                else // if(this.attributes.get(i).getSecond().equals(AttrType.CONTINUOUS)
                {
                    double oldMean = this.frequencies.get(i)[MEAN][category];
                    double oldVariance = this.frequencies.get(i)[SIGMA][category];
                    double value =  pattern.getValue(i);

                    if(this.normalize)
                    {
                        value = (value - stats.get(i).getMean())/(stats.get(i).getStandardDeviation());
                    }

                    this.frequencies.get(i)[MEAN][category] += (value - oldMean)/(this.priori.get(category));
                    this.frequencies.get(i)[SIGMA][category] += (value - oldMean)*(value - this.frequencies.get(i)[MEAN][category]);
                }
            }
        });
        
        // Update the mean and variance values for continuous attributes.
        for(int i = 0; i < nAttrib; ++i)
        {
            if(this.attributes.get(i).v2().equals(AttrType.CONTINUOUS))
            {
                for(int j = 0; j < nClasses; ++j)
                {
                    this.frequencies.get(i)[SIGMA][j] /= (this.priori.get(j) - 1.0);
                }
            }
        }
        
        this.trained = true;
    }

    @Override
    public Map<String, Double> computeScores(Pattern pattern) 
    {
        if(!this.trained)
            return null;
        
        int nClasses = this.classes.size();
        
        Map<String, Double> scores = new HashMap<>();
        for(int i = 0; i < nClasses; ++i)
        {
            scores.put(this.classes.get(i), this.computeScore(pattern, this.classes.get(i)));
        }
        
        return scores;
    }

    @Override
    public double computeScore(Pattern pattern, String category) {
        if(!this.trained || !this.classes.contains(category))
            return Double.NaN;      
        
        int cat = this.classes.indexOf(category);
        int nAttrib = this.attributes.size();
        double score = 1.0;
        for(int i = 0; i < nAttrib; ++i)
        {
            if(this.attributes.get(i).v2().equals(AttrType.NOMINAL))
            {
                int val = pattern.getValues().get(i).intValue();
                
                int l = this.frequencies.get(i).length;
                double aux = (this.frequencies.get(i)[val][cat] + 1.0)/(this.frequencies.get(i)[l-1][cat] + l + 0.0);

                // Apply the Laplace smoothing
                score *= aux;  
            }
            else // if(this.attributes.get(i).getSecond().equals(AttrType.CONTINUOUS))
            {
                double mean = this.frequencies.get(i)[MEAN][cat];
                double var = this.frequencies.get(i)[SIGMA][cat];
                
                if(var == 0.0)
                    continue;
                
                double value = pattern.getValue(i);
                if(this.normalize)
                    value = (pattern.getValue(i) - stats.get(i).getMean())/stats.get(i).getStandardDeviation();
                double aux = (value - mean)*(value - mean)/(2*var);
                double aux1 = Math.exp(-aux);
                double aux2 = (value - mean)*(value - mean)/(2*var);
                aux =  Math.exp(-aux)/ Math.sqrt(2*Math.PI*var);
                
                score *= aux;
            }
        }
        
        return score*this.priori.get(cat)/(this.numPatterns+0.0);
    }

    @Override
    public String classify(Pattern pattern) {
        if(!this.trained)
            return null;
        Map<String, Double> scores = this.computeScores(pattern);
        
        String currentClass = "";
        double max = Double.NEGATIVE_INFINITY;
        
        for(String category : scores.keySet())
        {
            if(scores.get(category) > max)
            {
                max = scores.get(category);
                currentClass = category;
            }
        }
        
        return currentClass;
    }

    
}
