/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.grid.informationpropagation.expiration;

/**
 * Identifiers for the different expiration mechanisms for information propagation protocols available in 
 * the library
 * @author Javier Sanz-Cruzado Puig
 * @see es.uam.eps.ir.socialnetwork.informationpropagation.expiration
 */
public class ExpirationMechanismIdentifiers 
{
    // Non stop simulators (until there is no more propagation)
    public final static String INFINITETIME = "Infinite Time";
    public final static String ALLNOTPROP = "All Not Propagated";
    public final static String TIMED = "Timed";
    public final static String EXPDECAY = "Exponential Decay";
    public final static String ALLNOTREALPROP = "All Not Real Propagated";
    public final static String ALLNOTREALPROPTIMESTAMP = "All Not Real Propagated Timestamp";
   
    
    /**
     * Prints the list of available algorithms
     */
    public static void printExpirationMechanismList()
    {
        System.out.println("Expiration Mechanisms:");
        System.out.println("\t" + INFINITETIME);
        System.out.println("\t" + ALLNOTPROP);
        System.out.println("\t" + TIMED);
        System.out.println("\t" + EXPDECAY);
        System.out.println("\t" + ALLNOTREALPROP);
        System.out.println("\t" + ALLNOTREALPROPTIMESTAMP);
    }
}
