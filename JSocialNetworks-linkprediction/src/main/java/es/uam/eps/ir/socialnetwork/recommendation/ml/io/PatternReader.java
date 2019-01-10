/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.recommendation.ml.io;

import es.uam.eps.ir.socialnetwork.recommendation.ml.attributes.Attribute;
import es.uam.eps.ir.socialnetwork.recommendation.ml.attributes.Attributes;
import java.util.List;
import org.ranksys.formats.parsing.Parser;

/**
 * Class for reading machine learning patterns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <S> Type of the instance set.
 * @param <I> Type of an individual instance.
 */
public interface PatternReader<U,S,I> 
{
    /**
     * Configures the attributes of the dataset
     * @param attributeFile The file containing the description for the dataset attributes.
     * @return True if everything went OK, false if not.
     */
    public boolean readAttributes(String attributeFile);
    /**
     * Reads the training set
     * @param trainFile file which contains the training patterns.
     * @return true if everything went ok, false if not.
     */
    public boolean readTrain(String trainFile);
    /**
     * Reads the test set
     * @param testFile File which contains the test patterns
     * @param parser Parser for the user identifiers.
     * @return true if everything went OK, false if not.
     */
    public boolean readTest(String testFile, Parser<U> parser);
    
    /**
     * Obtains the training set instances
     * @return the training set instances
     */
    public S getTrainSet();
    
    /**
     * Obtains the test set instances
     * @return the test set instances
     */
    public S getTestSet();
    
    /**
     * Obtains a test instance for a pair of nodes
     * @param u the first node.
     * @param v the second node.
     * @return null if the instance does not exist, a instance if it does.
     */
    public I getTestInstance(U u, U v);
    
    /**
     * Gets the attributes.
     * @return the attributes.
     */
    public Attributes getAttributes();
}
