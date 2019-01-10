/* 
 *  Copyright (C) 2015 Information Retrieval Group at Universidad AutÃ³noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.utils.generator;

/**
 * Generator examples.
 * @author Javier Sanz-Cruzado Puig.
 */
public class Generators 
{
    /**
     * Integer generator.
     */
    public static Generator<Integer> intgen = new Generator<Integer>() 
    {
        /**
         * Previous generated element
         */
        int prev = -1;
        @Override
        public Integer generate() 
        {
            ++prev;
            return prev;
        }
        
        @Override
        public void reset()
        {
            prev = -1;
        }
    };
    
    /**
     * Long generator.
     */
    public static Generator<Long> longgen = new Generator<Long>()
    {
        /**
         * Previous generated element
         */
        long prev = -1L;
        @Override
        public Long generate()
        {
            ++prev;
            return prev;
        }
        
        @Override
        public void reset()
        {
            prev = -1L;
        }
    };
    
    /**
     * Float generator.
     */
    public static Generator<Float> floatgen = new Generator<Float>()
    {
        /**
         * Previous generated element
         */
        float prev = -1f;

        @Override
        public Float generate()
        {
            ++prev;
            return prev;        
        }
        
          @Override
        public void reset()
        {
            prev = -1f;
        }
        
    };
    
    /**
     * Double generator.
     */
    public static Generator<Double> doublegen = new Generator<Double>()
    {
        /**
         * Previous generated element
         */
        double prev = -1.0;

        @Override
        public Double generate()
        {
            ++prev;
            return prev;        
        }
        
          @Override
        public void reset()
        {
            prev = -1.0;
        }
        
    };
    
    /**
     * String generator.
     */
    public static Generator<String> stringgen = new Generator<String>()
    {
        /**
         * Previous generated element
         */
        int prev = -1;
        
        @Override
        public String generate()
        {
            ++prev;
            return ""+prev;
        }
        
        @Override
        public void reset()
        {
            prev = -1;
        }
        
    };
}
