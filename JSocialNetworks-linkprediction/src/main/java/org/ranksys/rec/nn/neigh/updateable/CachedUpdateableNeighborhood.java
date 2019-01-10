/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.neigh.updateable;

import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import org.ranksys.core.util.topn.IntDoubleTopN;
import org.ranksys.rec.nn.sim.updateable.UpdateableSimilarity;

/**
 *
 * @author Javier
 */
public abstract class CachedUpdateableNeighborhood implements UpdateableNeighborhood
{
    protected List<Map<Integer, Integer>> idxmap;
    protected List<List<Integer>> idxla;
    protected List<List<Double>> simla;
    
    private final UpdateableSimilarity sim;
    private int n;
    /**
     * Constructor that calculates and caches neighborhoods.
     * @param n the number of user/items.
     * @param sim similarity
     */
    public CachedUpdateableNeighborhood(int n, UpdateableSimilarity sim)
    {
        this.idxla = new ArrayList<>();
        this.simla = new ArrayList<>();
        this.idxmap = new ArrayList<>();
        this.sim = sim;
        this.n = n;
        
        IntStream.range(0,n).forEach(idx -> 
        {
            Map<Integer, Integer> idx1 = new HashMap<>();
            IntArrayList idx3 = new IntArrayList();
            DoubleArrayList idx2 = new DoubleArrayList();
            
            IntDoubleTopN topN = new IntDoubleTopN(n);
            sim.similarElems(idx).forEach(topN::add);
            
            topN.stream().forEach(elem -> 
            {
                idx1.put(elem.v1, idx1.size());
                idx2.add(elem.v2);
                idx3.add(elem.v1);
            });
            
            idxla.add(idx3);
            simla.add(idx2);
            idxmap.add(idx1);
            
        });
    }

    @Override
    public void updateAddElement() 
    {
        this.sim.updateAddElement();
        this.idxla.add(new IntArrayList());
        this.simla.add(new DoubleArrayList());
        this.idxmap.add(new HashMap<>());
        ++this.n;
    }

    @Override
    public void updateAdd(int uidx, int iidx, double val) 
    {
        Tuple2oo<List<Integer>,List<Pair<Integer>>> tuple = this.sim.updateAdd(uidx, iidx, val);
        List<Integer> list = tuple.v1();
        
        // First, update the neighborhoods of the corresponding elements.
        list.forEach(vidx -> 
        {
            IntArrayList idx1 = new IntArrayList();
            DoubleArrayList idx2 = new DoubleArrayList();
            Map<Integer,Integer> idx3 = new HashMap<>();
            
            IntDoubleTopN topN = new IntDoubleTopN(n);
            sim.similarElems(vidx).forEach(topN::add);
            topN.stream().forEach(elem -> 
            {
                idx1.add(elem.v1);
                idx2.add(elem.v2);
                idx3.put(elem.v1, idx3.size());
            });
            
            idxla.set(vidx,idx1);
            simla.set(vidx,idx2);
            idxmap.set(vidx, idx3);
        });
        
        List<Pair<Integer>> pairs = tuple.v2();
        
        pairs.forEach(pair -> {
            int vidx1 = pair.v1();
            int vidx2 = pair.v2();
            double s = sim.similarity(vidx1, vidx2);
            this.updateSim(vidx1, vidx2, s);
        });
    }

    @Override
    public void updateDelete(int uidx, int iidx) 
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void updateSim(int uidx, int vidx, double s) 
    {
        Map<Integer, Integer> map = this.idxmap.get(uidx);
        List<Integer> pos = this.idxla.get(uidx);
        List<Double> sims = this.simla.get(uidx);
        
        int cursor = -1;
        if(sims.isEmpty())
        {
            sims.add(s);
            map.put(vidx,0);
            pos.add(vidx);
        }
        else
        {
            int low = 0;
            int high = sims.size();
            
            boolean inserted = false;

            while(high > low && !inserted)
            {
                int middle = (low + high)/2;
                if(sims.get(middle).equals(s))
                {
                    cursor = middle;
                    inserted = true;
                    break;
                }
                else if(sims.get(middle) < s)
                {
                    if(sims.size() > (middle+1) && sims.get(middle+1) >= s)
                    {
                        cursor = middle + 1;
                        inserted = true;
                        break;
                    }
                    else
                    {
                        low = middle + 1;
                    }
                }
                else
                {
                    if(middle > 0 && sims.get(middle - 1) <= s)
                    {
                        cursor = middle;
                        inserted = true;
                        break;
                    }
                    else
                    {
                        high = middle - 1;
                    }
                }
            }
            
            if(!inserted)
            {
                if(high == -1)
                {
                    cursor = 0;
                }
                else if(low >= pos.size())
                {
                    cursor = pos.size();
                }
                else
                {
                    if(sims.get(low) > s)
                    {
                        cursor = low;
                    }
                    else if(sims.get(low + 1) < s)
                    {
                        cursor = low + 1;
                    }
                    else
                    {
                        cursor = -1;
                    }
                }
            }
            
            int oldcursor = map.getOrDefault(vidx, -1);
            if(oldcursor == -1)
            {
                for(int i = cursor; i < pos.size(); ++i)
                {
                    map.put(pos.get(i), i+1);
                }
                sims.add(cursor, s);
                pos.add(cursor, vidx);
                map.put(vidx, cursor);
                
                if(sims.size() != map.size())
                {
                    System.err.println("FALLA AQUI");
                }
            }
            else if(cursor > oldcursor)
            {
                for(int i = oldcursor; i < cursor - 1; ++i)
                {
                    map.put(pos.get(i+1), i);
                }

                sims.add(cursor, s);
                pos.add(cursor, vidx);
                sims.remove(oldcursor);
                pos.remove(oldcursor);
                map.put(vidx, cursor-1);
                
                if(sims.size() != map.size())
                {
                    System.err.println("FALLA AQUI");
                }

            }
            else
            {
                for(int i = cursor; i < oldcursor - 1; ++i)
                {
                    if(i == pos.size())
                        System.err.println("ERROR");
                    map.put(pos.get(i),i+1);
                }

                if(cursor == -1)
                    System.err.println("ERROR");
                sims.add(cursor, s);
                pos.add(cursor, vidx);
                sims.remove(oldcursor+1);
                pos.remove(oldcursor+1);
                map.put(vidx, cursor);
                
                if(sims.size() != map.size())
                {
                    System.err.println("FALLA AQUI");
                }
            }
            
        }
        
    }
    
}
