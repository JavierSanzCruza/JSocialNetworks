/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.ranksys.rec.nn.sim.updateable;

import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.IntToDoubleFunction;
import java.util.stream.Stream;
import org.ranksys.core.preference.fast.IdxPref;
import org.ranksys.core.preferences.fast.updateable.FastUpdateablePointWisePreferenceData;
import org.ranksys.core.util.tuples.Tuple2id;
import static org.ranksys.core.util.tuples.Tuples.tuple;

/**
 * Vector similarity which allows updating. Based on the inner product of item/user
 * profiles as vectors.
 * 
 * @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
 */
public abstract class UpdateableVectorSimilarity implements UpdateableSimilarity
{
    /**
     * User-item preferences.
     */
    protected final FastUpdateablePointWisePreferenceData<?,?> data;
    /**
     * Map which stores the similarities between elements.
     */
    protected final Map<Integer, Int2DoubleOpenHashMap> similarities;
    /**
     * Map that relates elements and vector norms.
     */
    protected final Int2DoubleMap norm2map;
    /**
     * The number of users.
     */
    protected int numusers;
    
    /**
     * Constructor.
     * @param data preference data. It has to be a fast implementation of 
     * updateable preference data, which allows access to the value for each
     * (user, item) pair.
     */
    public UpdateableVectorSimilarity(FastUpdateablePointWisePreferenceData<?,?> data)
    {
        this.data = data;
        this.similarities = new HashMap<>();
        this.norm2map = new Int2DoubleOpenHashMap();
        
        // First, we compute the initial values for the similarities, and the norms.
        data.getAllUidx().forEach(uidx -> 
        {
            double norm = getNorm2(uidx);
            this.norm2map.put(uidx, norm);
            similarities.put(uidx, this.getProductMap(uidx));
        });
        
        this.numusers = data.numUsers();
    }
    
    /**
     * Obtains the similarity between an element and the rest of them.
     * @param uidx identifier of the element.
     * @return a map containing the similarities.
     */
    private Int2DoubleOpenHashMap getProductMap(int uidx)
    {
        // Initialize the map.
        Int2DoubleOpenHashMap productMap = new Int2DoubleOpenHashMap();
        productMap.defaultReturnValue(0.0);
        
        List<Double> l = new ArrayList<>();
        data.getAllUidx().forEach(vidx -> l.add(0.0));
        
        // Computes the scalar products for determining the similarity.
        if(data.useIteratorsPreferentially())
        {
            IntIterator iidxs = data.getUidxIidxs(uidx);
            DoubleIterator ivs = data.getUidxVs(uidx);
            
            while(iidxs.hasNext())
            {
                int iidx = iidxs.nextInt();
                double iv = ivs.nextDouble();
                IntIterator vidxs = data.getIidxUidxs(iidx);
                DoubleIterator vvs = data.getIidxVs(iidx);
                while(vidxs.hasNext())
                {
                    productMap.addTo(vidxs.nextInt(), iv*vvs.nextDouble());
                }
            }
        }
        else
        {
            data.getUidxPreferences(uidx)
                .forEach(ip -> data.getIidxPreferences(ip.v1)
                    .forEach(up -> productMap.addTo(up.v1, ip.v2*up.v2)));
        }
        
        productMap.remove(uidx);
        return productMap;
    }
    
    /**
     * Computes the norm.
     * @param uidx index of the user
     * @return the norm for user uidx
     */
    private double getNorm2(int uidx) 
    {
        if(data.useIteratorsPreferentially())
        {
            DoubleIterator ivs = data.getUidxVs(uidx);
            double sum = 0;
            while(ivs.hasNext())
            {
                double iv = ivs.nextDouble();
                sum += iv*iv;
            }
            return sum;
        }
        else
        {
            return data.getUidxPreferences(uidx)
                       .mapToDouble(IdxPref::v2)
                       .map(x -> x*x)
                       .sum();
        }
    }    

    @Override
    public IntToDoubleFunction similarity(int idx1) 
    {
        Int2DoubleOpenHashMap map = new Int2DoubleOpenHashMap();
        
        data.getUidxPreferences(idx1).forEach(iv -> map.put(iv.v1, iv.v2));
        double norm2A = norm2map.get(idx1);
        
        return idx2 -> 
        {
            if(!this.similarities.containsKey(idx1)) return 0.0;
            double product = this.similarities.get(idx1).getOrDefault(idx2, this.similarities.get(idx1).defaultReturnValue());
            return sim(product, norm2A, norm2map.get(idx2));
        };
    }
    
    /**
     * Calculates the similarity value.
     * @param product value of the inner product of vectors.
     * @param norm2A square of the norm of the first vector.
     * @param norm2B square of the norm of the second vector.
     * @return similarity value.
     */
    public abstract double sim(double product, double norm2A, double norm2B);

    @Override
    public double similarity(int idx1, int idx2)
    {
        if(!this.similarities.containsKey(idx1)) return 0.0;
        double product = this.similarities.get(idx1).getOrDefault(idx2, this.similarities.get(idx1).defaultReturnValue());
        return sim(product, norm2map.get(idx1), norm2map.get(idx2));
    }

    @Override
    public Stream<Tuple2id> similarElems(int idx1) 
    {
        double norm2A = norm2map.get(idx1);
        
        return this.similarities.get(idx1).int2DoubleEntrySet().stream().map(e -> 
        {
            int idx2 = e.getIntKey();
            double product = e.getDoubleValue();
            double norm2B = norm2map.get(idx2);
            return tuple(idx2, sim(product, norm2A, norm2B));
        });
    }

    @Override
    public void updateAddElement()
    {
        int newidx = this.numusers;
        this.norm2map.put(newidx, 0.0);
        Int2DoubleOpenHashMap productMap = new Int2DoubleOpenHashMap();
        productMap.defaultReturnValue(0.0);
        this.similarities.put(newidx, productMap);
        this.numusers++;
    }
    
    @Override
    public Tuple2oo<List<Integer>, List<Pair<Integer>>> updateAdd(int idx1, int idx2, double val) 
    {
        if(idx1 < 0 || idx1 > this.numusers) 
            System.err.println("ERROR");
        if(norm2map.size() != similarities.size())
            System.err.println("ERROR");
        List<Integer> list = new ArrayList<>();
        List<Pair<Integer>> pairs = new ArrayList<>();
        
        Optional<IdxPref> opt = (Optional<IdxPref>) data.getPreference(idx1, idx2);
        double oldvalue = opt.isPresent() ? opt.get().v2 : 0.0;
        
        // If the value is really updated:
        if(oldvalue == val) return new Tuple2oo<>(list, pairs);
         
        // First, we update the module.
        this.norm2map.put(idx1, this.norm2map.get(idx1) - oldvalue*oldvalue + val*val);
        list.add(idx1);
        
        // After that, we update the similarities.
        if(data.useIteratorsPreferentially())
        {
            IntIterator vidxs = data.getIidxUidxs(idx2);
            DoubleIterator vvs = data.getIidxVs(idx2);
            while(vidxs.hasNext())
            {
                int vidx = vidxs.nextInt();
                double iv = vvs.nextDouble();
                if(vidx != idx1)
                {
                    if(this.similarities == null)
                        System.err.println("ERROR");
                    else if(this.similarities.get(idx1) == null)
                        System.err.println("ERROR");
                    else if(this.similarities.get(vidx) == null)
                        System.err.println("ERROR");
                    
                    this.similarities.get(idx1).addTo(vidx, iv*(val - oldvalue));
                    this.similarities.get(vidx).addTo(idx1, iv*(val - oldvalue));
                    Pair<Integer> pair = new Pair<>(vidx, idx1);
                    pairs.add(pair);
                }
            }
        }
        else
        {
            data.getIidxPreferences(idx2).forEach(v ->
            {
                if(this.similarities == null)
                    System.err.println("ERROR");
                else if(this.similarities.get(idx1) == null)
                    System.err.println(idx1 + "\t" + this.numusers + "\t" + similarities.size() + "\t" + norm2map.size());
                else if(this.similarities.get(v.v1) == null)
                    System.err.println("ERROR");
                
                this.similarities.get(idx1).addTo(v.v1, v.v2*(val - oldvalue));
                this.similarities.get(v.v1).addTo(idx1, v.v2*(val - oldvalue));
                Pair<Integer> pair = new Pair<>(v.v1, idx1);
                pairs.add(pair);
            });
        }
        
        return new Tuple2oo<>(list, pairs);
    }

    @Override
    public void updateDel(int idx1, int idx2) 
    {
        if(!this.norm2map.containsKey(idx1))
        {
            this.norm2map.put(idx1, 0.0);
            Int2DoubleOpenHashMap productMap = new Int2DoubleOpenHashMap();
            productMap.defaultReturnValue(0.0);
            this.similarities.put(idx1, productMap);
        }
    }
    
}
