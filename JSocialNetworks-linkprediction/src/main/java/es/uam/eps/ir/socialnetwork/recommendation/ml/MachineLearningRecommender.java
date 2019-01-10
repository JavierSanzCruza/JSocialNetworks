/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml;

import es.uam.eps.ir.socialnetwork.recommendation.*;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;

/**
 * Abstract class for user recommendation in social networks.
 * 
 * @author Javier Sanz-Cruzado Puig.
 * @param <U> Type of the users
 */
public class MachineLearningRecommender<U> extends UserFastRankingRecommender<U>
{
    /**
     * Pattern set.
     */
    private final GraphPatternSet<U> testSet;
    
    /**
     * Classifier
     */
    private final Classifier classifier;
    
    /**
     * Name of the positive class (the class we want to predict).
     */
    public final static String POSITIVECLASS = "1";
    /**
     * Name of the negative class (the class we do not want to predict.
     */
    public final static String NEGATIVECLASS = "0";
    
    /**
     * Constructor.
     * @param graph Graph.
     * @param classifier The classifier.
     * @param trainSet The set of patterns the model is trained with.
     * @param testSet The set of pattern used for recommendation.
     */
    public MachineLearningRecommender(FastGraph<U> graph, Classifier classifier, PatternSet trainSet, GraphPatternSet<U> testSet) 
    {
        super(graph);
        classifier.train(trainSet);
        this.testSet = testSet;
        this.classifier = classifier;
    }

    @Override
    public Int2DoubleMap getScoresMap(int i) 
    {
        Int2DoubleMap scores = new Int2DoubleOpenHashMap();
        scores.defaultReturnValue(Double.NEGATIVE_INFINITY);
        U u = this.uidx2user(i);
        this.testSet.getAdjacentPatterns(u).forEach(pat -> {
            scores.put(this.item2iidx(pat.v1()), classifier.computeScore(pat.v2(), POSITIVECLASS));
        });
        
        return scores;
    }
}
