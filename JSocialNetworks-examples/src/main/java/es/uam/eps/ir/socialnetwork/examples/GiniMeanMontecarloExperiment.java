/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples;

import es.uam.eps.ir.socialnetwork.utils.indexes.GiniIndex;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Javier Sanz-Cruzado Puig
 */
public class GiniMeanMontecarloExperiment 
{
    /**
     * Main
     * @param args 
     */
    public static void main(String[] args)
    {
        int[] sizes = {10, 100, 1000};
        
        int[] values = {1, 10, 100, 1000, 10000, 100000, 1000000};
        Random rng = new Random();
        GiniIndex gi = new GiniIndex();
        for(int size : sizes)
        {
            for(int value : values)
            {
                double sum = 0;
                for(int i = 0; i < 1000; ++i)
                {
                    // a) Generate a probability distribution using size and value
                    List<Double> probs = new ArrayList();
                    for(int j = 0; j < size; ++j)
                    {
                        probs.add(0.0);
                    }
                    for(int j = 0; j < value; ++j)
                    {
                        int pos = rng.nextInt(size);
                        probs.set(pos, probs.get(pos)+1.0);
                    }

                    // b) Compute the Gini distribution
                    sum +=gi.compute(probs, true);

                }
                sum /= 1000;

                System.out.println("size " + size + "\tvalue " + value + "\t" + sum);
            }
        }
    }
}
