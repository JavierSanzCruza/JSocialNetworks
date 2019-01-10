/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.communities;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.SparseDoubleMatrix2D;
import java.io.*;
import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.openide.util.Exceptions;
import org.ranksys.formats.parsing.Parsers;

/**
 * Analyzes the hashtags of a social network dataset, for each of the communities.
 * @author Javier Sanz-Cruzado Puig
 */
public class TopHashtagsCommunitiesExample 
{
    public static void main(String args[]) throws Exception
    {
        if(args.length < 5)
        {
            System.out.println("Usage: <communitiesFile> <hashtagFile> <hashtagUser> <outputFile> <topN>");
        }
        
        // Read the communities file
        CommunitiesReader<Long> reader = new CommunitiesReader<>();
        Communities<Long> comm = reader.read(args[0], "\t", Parsers.lp);
        Map<Long, String> hashmaps = new HashMap<>();
        Map<Integer, Long2LongMap> htCommunities = new HashMap<>();
        int topN = new Integer(args[4]);
        comm.getCommunities().forEach((c) -> 
        {
            Long2LongMap auxMap = new Long2LongOpenHashMap();
            auxMap.defaultReturnValue(0L);
            htCommunities.put(c, auxMap);
        });
        
        // Read the hashtags file
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]))))
        {
            // read the header line of the file
            String line = br.readLine();
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                hashmaps.put(new Long(split[0]), split[1]);
            }
        }
        long sum = 0;
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2]))))
        {
            String line;
            while((line = br.readLine())!=null) 
            {
                String [] split = line.split("\t");
                Long t = new Long(split[1]);
                Integer c = comm.getCommunity(t);
                if(c != null && c != -1)
                {
                    Long2LongMap auxMap = htCommunities.get(c);
                    long ht = new Long(split[0]);
                    auxMap.put(ht, auxMap.get(ht) + new Long(split[2]));
                    sum += new Long(split[2]);
                }
            };
        }
        
        
        
        
        //Analysis:
        
        // Step 1: Top N hashtags by community
        System.out.println("Starting: Top " + topN + " hashtags by community");
        Map<Integer, List<Tuple2oo<Long,Long>>> hashtagcomm = new HashMap<>();
        
        htCommunities.keySet().forEach((key)->{
            List<Entry<Long, Long>> list = htCommunities
                .get(key)
                .entrySet()
                .stream()
                .sorted((Entry<Long, Long> t, Entry<Long, Long> t1) -> (int) (t1.getValue() - t.getValue()))
                .collect(Collectors.toCollection(ArrayList::new));
            
            List<Tuple2oo<Long,Long>> topHashtags = new ArrayList<>();
            int top = Math.min(topN, list.size());
            for(int i = 0; i < top; ++i)
            {
                Entry<Long,Long> entry = list.get(i);
                topHashtags.add(new Tuple2oo<>(entry.getKey(), entry.getValue()));
            }
            
            hashtagcomm.put(key, topHashtags);
            
            
        });
        
        
        // Step 2: Matrix
        System.out.println("Starting: Ratio of shared hashtags");
        DoubleMatrix2D matrix = new SparseDoubleMatrix2D(comm.getNumCommunities(), comm.getNumCommunities());
        for(int i = 0; i < comm.getNumCommunities(); ++i)
        {
            Set<Long> setA = htCommunities.get(i).keySet();
            for(int j = 0; j < comm.getNumCommunities(); ++j)
            {
                Set<Long> setB = htCommunities.get(j).keySet();
                
                Set<Long> intersection = new HashSet<>(setA);
                intersection.retainAll(setB);
                
                if(setA.size() > 0)
                    matrix.setQuick(i, j, (intersection.size() + 0.0)/(setA.size()));
            }
        }
        
        // Step3: Matrix over top 50 for each community
        System.out.println("Starting: Ratio of shared hashtag (top " + topN + ")");
        DoubleMatrix2D matrix2 = new SparseDoubleMatrix2D(comm.getNumCommunities(), comm.getNumCommunities());
        for(int i = 0; i < comm.getNumCommunities(); ++i)
        {
            Set<Long> setA = hashtagcomm.get(i).stream().map(tuple -> tuple.v1()).collect(Collectors.toCollection(HashSet::new));
            for(int j = 0; j < comm.getNumCommunities(); ++j)
            {
                Set<Long> setB = hashtagcomm.get(j).stream().map(tuple -> tuple.v1()).collect(Collectors.toCollection(HashSet::new));
                
                Set<Long> intersection = new HashSet<>(setA);
                intersection.retainAll(setB);
                
                if(setA.size() > 0)
                    matrix2.setQuick(i, j, (intersection.size() + 0.0)/(setA.size()));
            }
        }
        
        // Step4: Matrix over top 50 for each community (checking over the full other community tags)
        System.out.println("Starting: Ratio of shared hashtag (top " + topN + ") over all tagged tweets");
        DoubleMatrix2D matrix3 = new SparseDoubleMatrix2D(comm.getNumCommunities(), comm.getNumCommunities());
        for(int i = 0; i < comm.getNumCommunities(); ++i)
        {
            Set<Long> setA = hashtagcomm.get(i).stream().map(tuple -> tuple.v1()).collect(Collectors.toCollection(HashSet::new));
            for(int j = 0; j < comm.getNumCommunities(); ++j)
            {
                Set<Long> setB = htCommunities.get(j).keySet().stream().collect(Collectors.toCollection(HashSet::new));
                
                Set<Long> intersection = new HashSet<>(setA);
                intersection.retainAll(setB);
                
                if(setA.size() > 0)
                    matrix3.setQuick(i, j, (intersection.size() + 0.0)/(setA.size()));
            }
        }
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[3]))))
        {
            bw.write("NUMBER OF HASHTAGS BY COMMUNITY\n");
            bw.write("community\tnumHashtags\tnumDistinctHashtags\tnumUsers\n");
            comm.getCommunities().forEach(c ->
            {
                try {
                    bw.write(c + "\t");
                    long count = htCommunities.get(c).values().stream().mapToLong(val -> val).sum();
                    bw.write(count + "\t" + htCommunities.get(c).size() + "\t" + comm.getUsers(c).count() + "\n");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
                
            bw.write("\n\nTOP " + topN + " HASHTAGS BY COMMUNITY\n");
            comm.getCommunities().sorted().forEach(i->{;
                try {
                    bw.write(i + "\t\t");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
            
            bw.write("\n");
            
            
            IntStream.range(0,topN).forEach(i -> 
            {
                comm.getCommunities().sorted().forEach((j) -> 
                {
                    try 
                    {
                        if(hashtagcomm.get(j).size() > i)
                            bw.write(hashtagcomm.get(j).get(i).v1() + "\t" + hashtagcomm.get(j).get(i).v2());
                        else
                            bw.write("\t");
                        bw.write("\t");
                    } 
                    catch (IOException ex) 
                    {
                            Exceptions.printStackTrace(ex);
                    }      
                });
                
                try 
                {
                    bw.write("\n");
                } 
                catch (IOException ex) 
                {
                    Exceptions.printStackTrace(ex);
                }
            });
            
            bw.write("\n\nRATIO OF SHARED HASHTAGS\n");
            for(int i = 0; i < matrix.rows(); ++i)
            {
                for(int j = 0; j < matrix.columns(); ++j)
                {
                    bw.write(matrix.getQuick(i,j)+"\t");
                }
                bw.write("\n");
            }
            
            bw.write("\n\nRATIO OF SHARED HASHTAGS (top " + topN + ")\n");
            for(int i = 0; i < matrix.rows(); ++i)
            {
                for(int j = 0; j < matrix.columns(); ++j)
                {
                    bw.write(matrix2.getQuick(i,j)+"\t");
                }
                bw.write("\n");
            }
            
            bw.write("\n\nRATIO OF SHARED HASHTAGS (top " + topN + " of all comms)\n");
            for(int i = 0; i < matrix.rows(); ++i)
            {
                for(int j = 0; j < matrix.columns(); ++j)
                {
                    bw.write(matrix3.getQuick(i,j)+"\t");
                }
                bw.write("\n");
            }
        }
        
    }
}
