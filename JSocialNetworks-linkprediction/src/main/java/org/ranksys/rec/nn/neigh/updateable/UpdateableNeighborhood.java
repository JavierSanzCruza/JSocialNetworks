/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.neigh.updateable;

import java.util.stream.Stream;
import org.ranksys.core.util.tuples.Tuple2id;

/**
 *
 * @author Javier
 */
public interface UpdateableNeighborhood 
{
    public Stream<Tuple2id> getNeighbors(int idx);
    
    public void updateAddElement();
    
    public void updateAdd(int uidx, int iidx, double val);
    public void updateDelete(int uidx, int iidx);
}
