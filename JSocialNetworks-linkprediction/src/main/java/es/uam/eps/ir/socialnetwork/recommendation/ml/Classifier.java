/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml;

import java.util.Map;

/**
 * Class that represents a binary classifier.
 * @author Javier Sanz-Cruzado Puig
 */
public interface Classifier 
{
    /**
     * Trains the classifier.
     * @param trainSet The training set.
     */
    public void train(PatternSet trainSet);
    
    /**
     * Computes the scores for an individual pattern (once the training has been done).
     * @param pattern The individual pattern. 
     * @return A score for each class.
     */
    public Map<String,Double> computeScores(Pattern pattern);
    
    /**
     * Gets the score for an individual pattern in a certain category.
     * @param category The class.
     * @param pattern The pattern.
     * @return the score.
     */
    public double computeScore(Pattern pattern, String category);
    /**
     * Obtains the most probable class for a certain instance.
     * @param pattern The individual instance.
     * @return The most probable class.
     */
    public String classify(Pattern pattern);
    
}
