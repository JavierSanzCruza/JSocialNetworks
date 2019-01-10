/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.ranksys.socialextension;

import java.util.ArrayList;
import java.util.List;
import java.util.function.ToDoubleFunction;
import org.ranksys.core.Recommendation;
import org.ranksys.core.util.tuples.Tuple2od;
import org.ranksys.metrics.SystemMetric;
import org.ranksys.novdiv.distance.ItemDistanceModel;

/**
 * Global version of EILD.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users.
 * @param <I> Type of the items.
 */
public class ModEILD<U,I> implements SystemMetric<U,I> 
{
    
    private List<Recommendation<U,I>> list = new ArrayList<>();
        
    private final int cutoff;
    private final ItemDistanceModel<I> distModel;


    /**
     * Constructor.
     * @param cutoff maximum length of recommendation lists to evaluate
     * @param distModel item distance model
     */
    public ModEILD(int cutoff, ItemDistanceModel<I> distModel)
    {
        this.cutoff = cutoff;
        this.distModel = distModel;
    }
    
    @Override
    public void add(Recommendation<U, I> r) 
    {
        this.list.add(r);    
    }

    @Override
    public double evaluate() 
    {
        double value = 0.0;
        double counter = 0.0;
        for(Recommendation<U,I> rec : list)
        {
            
            List<Tuple2od<I>> items = rec.getItems();
            int N = Math.min(cutoff, items.size());
            counter += N + 0.0;
            
            double eild = 0.0;
            for(int i = 0; i < N; ++i)
            {
                double ieild = 0.0;
                ToDoubleFunction<I> iDist = distModel.dist(items.get(i).v1);
                for(int j = 0; j < N; ++j)
                {
                    if(i == j)
                    {
                        continue;
                    }
                    double dist = iDist.applyAsDouble(items.get(j).v1);
                    if(!Double.isNaN(dist))
                    {
                        ieild += dist;
                    }
                }
                if(N > 0)
                {
                    eild += ieild/(N+0.0);
                }
            }            
            value += eild;
        } 
        if(counter > 0)
        {
            value /= counter;
        }
        System.out.println(counter + "\t" + value);
        return value;
    }

    @Override
    public void combine(SystemMetric<U, I> sm) 
    {
        ModEILD<U,I> otherM = (ModEILD<U,I>) sm;
        list.addAll(otherM.list);
    }

    @Override
    public void reset() 
    {
        this.list = new ArrayList<>();
    }
}
