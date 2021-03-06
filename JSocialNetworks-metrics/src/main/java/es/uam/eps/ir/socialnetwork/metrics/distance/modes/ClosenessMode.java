/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.metrics.distance.modes;

/**
 * Algorithms for computing the closeness of a node in a graph.
 * <ul>
 *  <li><b>HARMONICMEAN:</b> Computes the closeness as the harmonic mean of the distances from the node to the rest.</li>
 *  <li><b>COMPONENTS:</b> Computes the closeness inside each separate strongly connected component. In case the graph is strongly connected, it represents the classical definition</li>
 * </ul>
 * @author Javier Sanz-Cruzado Puig
 */
public enum ClosenessMode 
{
    HARMONICMEAN, COMPONENTS
}
