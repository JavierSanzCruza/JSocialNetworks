/*
 *  Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package es.uam.eps.ir.socialnetwork.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

/**
 * Fast implementation of an unweighted relation.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @param <W> type of the (hypothetical) weights.
 */
public abstract class FastUnweightedRelation<W> implements Relation<W>
{
    /**
     * Links from the first kind of objects to the second. Indexed by the second. Ej: Incident edges.
     */
    protected final List<List<Integer>> firstIdxList;
    /**
     * Links from the second kind of objects to the first. Indexed by the first. Ej: Adjacent edges.
     */
    protected final List<List<Integer>> secondIdxList;
    
    /**
     * Constructor.
     */
    public FastUnweightedRelation()
    {
        this.firstIdxList = new ArrayList<>();
        this.secondIdxList = new ArrayList<>();
    }
    
    /**
     * Constructor.
     * @param firstIdxList Links from the first kind of objects to the second. Indexed by the second. Ej: Incident edges.
     * @param secondIdxList Links from the second kind of objects to the first. Indexed by the first. Ej: Adjacent edges.
     */
    public FastUnweightedRelation(List<List<Integer>> firstIdxList, List<List<Integer>> secondIdxList)
    {
        this.firstIdxList = firstIdxList;
        this.secondIdxList = secondIdxList;
    }
    
    @Override
    public int numFirst()
    {
        return this.secondIdxList.size();
    }

    @Override
    public int numFirst(int secondIdx)
    {
        return this.firstIdxList.get(secondIdx).size();
    }

    @Override
    public int numSecond(int firstIdx)
    {
        return this.secondIdxList.get(firstIdx).size();
    }
    
    @Override
    public Stream<Integer> getAllFirst()
    {
        List<Integer> l = new ArrayList<>();
        for(int i = 0; i < numFirst(); ++i)
        {
            l.add(i);
        }
        
        return l.stream();
    }

    @Override
    public Stream<IdxValue<W>> getIdsFirst(int secondIdx)
    {
        return this.firstIdxList.get(secondIdx).stream().map(i -> new IdxValue<>(i, null));
    }

    @Override
    public Stream<IdxValue<W>> getIdsSecond(int firstdIdx)
    {
        return this.secondIdxList.get(firstdIdx).stream().map(i -> new IdxValue<>(i,null));
    }

    @Override
    public boolean addFirstItem(int firstIdx)
    {
        int size = this.secondIdxList.size();
        if(firstIdx < size && this.secondIdxList.get(firstIdx) != null)
            return false;
        if(firstIdx > size)
            return false;

        
        this.firstIdxList.add(new ArrayList<>());
        this.secondIdxList.add(new ArrayList<>());
        
        return true;
    }

    @Override
    public boolean addRelation(int firstIdx, int secondIdx, W weight)
    {
        if(firstIdx < 0 || secondIdx < 0)
            return false;
        else if(firstIdx >= this.secondIdxList.size() || secondIdx >= this.firstIdxList.size())
            return false;
        
        Integer value = this.binarySearch(firstIdx, secondIdx, true);
        if(value == null || value >= 0)
            return false;
        int idx = Math.abs(value + 1);
        this.firstIdxList.get(secondIdx).add(idx, firstIdx);
        
        value = this.binarySearch(firstIdx, secondIdx, false);
        if(value == null || value >= 0)
        {
            return false;
        }
        idx = Math.abs(value + 1);
        this.secondIdxList.get(firstIdx).add(idx,secondIdx);
        
        return true;
    }

    @Override
    public W getValue(int firstIdx, int secondIdx)
    {
        return null;
    }

    
    @Override
    public boolean containsPair(int firstIdx, int secondIdx)
    {
        Integer value = this.binarySearch(firstIdx, secondIdx, true);
        return (value != null && value >= 0);
    }
    
    @Override
    public boolean updatePair(int firstIdx, int secondIdx, W weight, boolean createRelation)    
    {
        if(firstIdx < 0 || secondIdx < 0)
            return false;
        else if(firstIdx >= this.secondIdxList.size() || secondIdx >= this.firstIdxList.size())
            return false;
        
        Integer value = this.binarySearch(firstIdx, secondIdx, true);
        if(value == null || (value < 0 && !createRelation))
            return false;
        else if(value >= 0)
            return true;
        else // the relation has to be created
        {
            int idx = Math.abs(value + 1);
            this.firstIdxList.get(secondIdx).add(idx, firstIdx);
        }
        
        value = this.binarySearch(firstIdx, secondIdx, false);
        int idx = Math.abs(value + 1);
        this.secondIdxList.get(firstIdx).add(idx, secondIdx);
        return true;
    }

    @Override
    public boolean removePair(int firstIdx, int secondIdx) 
    {
        Integer value = this.binarySearch(firstIdx, secondIdx, true);
        if(value == null || value < 0)
            return false;
        this.firstIdxList.get(secondIdx).remove(value.intValue());
        
        value = this.binarySearch(firstIdx, secondIdx, false);
        if(value == null || value < 0)
            return false;
        this.secondIdxList.get(firstIdx).remove(value.intValue());
       
        return true;
    }

    /**
     * Given a pair (firstIdx, secondIdx), finds it in the graph using binary search.
     * @param firstIdx the first element.
     * @param secondIdx the second element.
     * @param firstList true if the element has to be found on the list of first elements,
     * false if it has to be found on the list of second elements.
     * @return the index of the element if it exists, - (insertpoint - 1) if it does not,
     * where insertpoint is the corresponding point where the element should be added.
     */
    private Integer binarySearch(int firstIdx, int secondIdx, boolean firstList)
    {
        if(firstIdx < 0 || secondIdx < 0)
        {
            return null;
        }
        else if(firstIdx >= this.secondIdxList.size() || secondIdx >= this.firstIdxList.size())
        {
            return null;
        }
        
        int elementToAdd = firstList ? firstIdx : secondIdx;
        
        List<Integer> list = firstList ? this.firstIdxList.get(secondIdx) : this.secondIdxList.get(firstIdx);
        return Collections.binarySearch(list, elementToAdd);
    }
}
