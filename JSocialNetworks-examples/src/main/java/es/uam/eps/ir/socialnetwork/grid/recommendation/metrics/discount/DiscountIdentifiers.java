/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.recommendation.metrics.discount;

/**
 * Identifiers for the different ranking discount models
 * available in the library
 * @author Javier Sanz-Cruzado Puig
 */
public class DiscountIdentifiers 
{
    // Non stop simulators (until there is no more propagation)
    public final static String EXPONENTIAL = "Exponential";
    public final static String LOGARITHMIC = "Logarithmic";
    public final static String NODISCOUNT = "No Discount";
    public final static String RECIPROCAL = "Reciprocal";
    

   
    
    /**
     * Prints the list of available ranking discount models
     */
    public static void printSelectionMechanismList()
    {
        System.out.println("Discount Models:");
        System.out.println("\t" + EXPONENTIAL);
        System.out.println("\t" + LOGARITHMIC);
        System.out.println("\t" + NODISCOUNT);
        System.out.println("\t" + RECIPROCAL);
    }
}
