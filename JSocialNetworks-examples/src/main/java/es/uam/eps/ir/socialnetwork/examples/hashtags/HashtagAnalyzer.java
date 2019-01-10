/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.hashtags;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.utils.datatypes.CompTuple2oo;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import org.openide.util.Exceptions;
import org.ranksys.formats.parsing.Parsers;

/**
 * Analyzes the hashtags of a given dataset
 * @author Javier Sanz-Cruzado Puig
 */
public class HashtagAnalyzer 
{
    /**
     * Analyzes the hashtags of a given dataset.
     * @param args Execution arguments: <ol>
     * <li>File containing a tag list (id/tag), comma separated</li>
     * <li>File containing a relation between users and information pieces ids, tag separated</li>
     * <li>File containing a relation between information pieces ids and tag ids, tag separated</li>
     * <li>Communities file</li>
     * <li>Output directory where to store the analysis</li>
     * </ol>
     * @throws java.io.IOException if something fails while reading or writing files
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 5)
        {
            System.err.println("Invalid arguments:");
            System.err.println("1: File containing a tag list (id/tag)");
            System.err.println("2: Tweets / Users");
            System.err.println("3: Tweets / Hashtags Id");
            System.err.println("4: Communities file");
            System.err.println("5: Output directory");
            return;
        }
        
        String tagFile = args[0];
        String infoFile = args[1];
        String tagInfoFile = args[2];
        String commFile = args[3];
        String output = args[4];
        
        
        
        CommunitiesReader<Long> commReader = new CommunitiesReader<>();
        Communities<Long> comm = commReader.read(commFile, "\t", Parsers.lp);
        
        /*
         * Reads the different tags (relation between an identifier and a string) 
         */
        Map<Long, String> tags = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tagFile))))
        {
            String line = br.readLine(); // Read the header
            while((line = br.readLine()) != null)
            {
                String[] split = line.split(",");
                Long tagId = Parsers.lp.parse(split[0]);
                tags.put(tagId, split[1]);
            }
        }
        
        /**
         * Reads the relation between tweets and users.
         */
        Map<Long, Long> userInfo = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(infoFile))))
        {
            String line = br.readLine();
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                Long infoId = Parsers.lp.parse(split[0]);
                Long userId = Parsers.lp.parse(split[1]);
                
                userInfo.put(infoId, userId);
            }
        }
        
        /**
         * Reads the relation between tags and tweets.
         */
        Map<Long, Set<Long>> tagInfo = new HashMap<>();
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(tagInfoFile))))
        {
            String line = br.readLine();
            while((line = br.readLine()) != null)
            {
                String[] split = line.split("\t");
                Long infoId = Parsers.lp.parse(split[0]);
                Long tagId = Parsers.lp.parse(split[1]);
                
                if(!tagInfo.containsKey(infoId))
                    tagInfo.put(infoId, new HashSet<>());
                tagInfo.get(infoId).add(tagId);
            }
        }
        
        
        //For each tag, counts the number of apparitions
        Map<Long, Long> tagCount = new HashMap<>();

        // For each tag, counts the number of apparitions on each community
        Map<Integer, Map<Long, Long>> tagCountComm = new HashMap<>();
        // For each tag, obtains the number of users that publish that tag
        Map<Long, Set<Long>> tagUser = new HashMap<>();
        // For each tag, obtains the number of users that publish that tag on each community
        Map<Integer, Map<Long, Set<Long>>> tagUserComm = new HashMap<>();
        // Obtains the number of information pieces that have a hashtag on each community
        Map<Integer, Long> infoComm = new HashMap<>();
        // Count the appearances of each tag in the set of selected information pieces
        userInfo.entrySet().forEach(entry -> 
        {
            Long infoId = entry.getKey();
            Long userId = entry.getValue();

            // Identify the user community
            Integer userComm = comm.getCommunity(userId);
            if(userComm != -1)
            {
                
                // initialize
                if(!infoComm.containsKey(userComm))
                {
                    infoComm.put(userComm, 0L);
                }
                
                if(!tagCountComm.containsKey(userComm))
                {
                    tagCountComm.put(userComm, new HashMap<>());
                    tagUserComm.put(userComm, new HashMap<>());
                }

                // If the tweet contains any information piece
                if(tagInfo.containsKey(infoId))
                {
                    infoComm.put(userComm, infoComm.get(userComm) + 1);
                    Set<Long> hashtags = tagInfo.get(infoId);
                    hashtags.forEach(hashtag -> 
                    {
                        if(!tagUser.containsKey(hashtag))
                        {
                           tagUser.put(hashtag, new HashSet<>());
                        }
                        tagUser.get(hashtag).add(userId);

                        if(!tagUserComm.get(userComm).containsKey(hashtag))
                        {
                            tagUserComm.get(userComm).put(hashtag, new HashSet<>());
                        }
                        tagUserComm.get(userComm).get(hashtag).add(userId);

                        if(!tagCount.containsKey(hashtag))
                        {
                            tagCount.put(hashtag, 0L);
                        }
                        tagCount.put(hashtag, tagCount.get(hashtag) + 1L);

                        if(!tagCountComm.get(userComm).containsKey(hashtag))
                        {
                            tagCountComm.get(userComm).put(hashtag, 0L);
                        }

                        tagCountComm.get(userComm).put(hashtag, tagCount.get(hashtag) + 1L);
                    });
                }
            }
        });
        
        
        
        // Hashtag distribution
        Queue<CompTuple2oo<Long, Long>> hashtagDistrib = new PriorityQueue<>();
        
        long numHashtags = tagCount.values().stream().mapToLong(x -> x).sum();
        tagCount.entrySet().stream().forEach(entry -> 
        {
            CompTuple2oo<Long,Long> tuple = new CompTuple2oo<>(entry.getKey(), entry.getValue());
            hashtagDistrib.add(tuple);
        });
        long numUniqueHashtags = hashtagDistrib.size();
        
        
        Set<Long> htTop50 = new HashSet<>();
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "ht-distrib.txt"))))
        {
            int i = 0;
            while(!hashtagDistrib.isEmpty())
            {
                CompTuple2oo<Long, Long> pair = hashtagDistrib.poll();
                if(i < 50)
                {
                    htTop50.add(pair.v1());
                }
                bw.write(i + "\t" + tags.get(pair.v1()) + "\t" + pair.v2() + "\t" + tagUser.get(pair.v1()).size() + "\n");
                ++i;
            }
        }

        
        // Hashtag distributions by communities
        Map<Integer, Set<Long>> htTopComm50 = new HashMap<>();
        Map<Integer, Long> commCount = new HashMap<>();
        Map<Integer, Integer> uniqueCount = new HashMap<>();
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "comms-distrib.txt"))))
        {
            tagCountComm.keySet().forEach(key -> 
            {
                Map<Long, Long> hashtags = tagCountComm.get(key);
                Queue<CompTuple2oo<Long, Long>> commHTdistrib = new PriorityQueue<>();
                htTopComm50.put(key, new HashSet<>());
                commCount.put(key, hashtags.entrySet().stream().mapToLong(entry -> 
                {
                    CompTuple2oo<Long,Long> tuple = new CompTuple2oo<>(entry.getKey(), entry.getValue());
                    commHTdistrib.add(tuple);
                    return entry.getValue();
                }).sum());
                
                uniqueCount.put(key, hashtags.size());
                
                int i = 0;
                while(!commHTdistrib.isEmpty())
                {
                    CompTuple2oo<Long, Long> pair = commHTdistrib.poll();
                    if(i < 50)
                    {
                        htTopComm50.get(key).add(pair.v1());
                    }
                    try 
                    {
                        bw.write(key + "\t" + i + "\t" + tags.get(pair.v1()) + "\t" + pair.v2() + "\t" + tagUserComm.get(key).get(pair.v1()).size() + "\n");
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    ++i;
                }
            });
        }
        
        // Write general community data
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "general.txt"))))
        {
            bw.write("Num. Hashtags\tNum. Unique\tNum. Tweets\tNum. Tweets Hashtags\tNum. Users\tNum. Comm.\n");
            bw.write(numHashtags + "\t" + numUniqueHashtags + "\t" + userInfo.size() + "\t" + infoComm.values().stream().mapToLong(x -> x).sum() + "\t"+ comm.getCommunities().mapToLong(c -> comm.getUsers(c).count()).sum() + "\t" + comm.getNumCommunities());

            bw.write("\n");
            bw.write("Comm.\tNum. Users\tNum. Hashtags\tNum. Unique\tNum. Tweets\n");
            
            for(int c = 0; c < comm.getNumCommunities(); ++c)
            { 
                bw.write(c + "\t" + comm.getUsers(c).count() + "\t" + commCount.get(c) + "\t" + uniqueCount.get(c) + "\t" + infoComm.get(c) + "\n");
            }
        }
        
        Map<Integer, Map<Integer, Double>> intersections = new HashMap<>();
        
        for(int i = 0; i < comm.getNumCommunities(); ++i)
        {
            intersections.put(i, new HashMap<>());
            for(int j = 0; j < comm.getNumCommunities(); ++j)
            {
                int jfixed = j;
                double intersection = tagCountComm.get(i).keySet().stream().mapToDouble(ht -> 
                {
                    if(tagCountComm.get(jfixed).containsKey(ht))
                        return 1.0;
                    return 0.0;
                }).sum();
                
                intersections.get(i).put(j, intersection / (0.0 + uniqueCount.get(i)));
            }
        }
        
        // Intersection of the top 50 for each community with the other communities (all hashtags in that community)
        Map<Integer, Map<Integer, Double>> intersectionsTop50All = new HashMap<>();
        
        for(int i = 0; i < comm.getNumCommunities(); ++i)
        {
            intersectionsTop50All.put(i, new HashMap<>());
            for(int j = 0; j < comm.getNumCommunities(); ++j)
            {
                int jfixed = j;
                double intersection = htTopComm50.get(i).stream().mapToDouble(ht -> {
                    if(tagCountComm.get(jfixed).containsKey(ht))
                        return 1.0;
                    return 0.0;
                }).sum();
                
                intersectionsTop50All.get(i).put(j, intersection / (htTopComm50.get(i).size() + 0.0));
            }
        }
        
        // Intersection of the top 50 for each community (only studying the top 50 of each comm.)
        Map<Integer, Map<Integer, Double>> intersectionsTop50 = new HashMap<>();
        
        for(int i = 0; i < comm.getNumCommunities(); ++i)
        {
            intersectionsTop50.put(i, new HashMap<>());
            for(int j = 0; j < comm.getNumCommunities(); ++j)
            {
                int jfixed = j;
                double intersection = htTopComm50.get(i).stream().mapToDouble(ht -> {
                    if(htTopComm50.get(jfixed).contains(ht))
                        return 1.0;
                    return 0.0;
                }).sum();
                
                intersectionsTop50.get(i).put(j, intersection / (htTopComm50.get(i).size() + 0.0));
            }
        }
        
        // Write the intersections
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output + "intersections.txt"))))
        {
            bw.write("Community intersections\n");
            for(int i = 0; i < comm.getNumCommunities(); ++i)
            {
                for(int j = 0; j < comm.getNumCommunities(); ++j)
                {
                    bw.write(intersections.get(i).get(j) + "\t");
                }
                bw.write("\n");
            }
            
            bw.write("Top 50 comm. intersections (compare with all the tweets of the second comm. -> Columns\n");
            for(int i = 0; i < comm.getNumCommunities(); ++i)
            {
                for(int j = 0; j < comm.getNumCommunities(); ++j)
                {
                    bw.write(intersectionsTop50All.get(i).get(j) + "\t");
                }
                bw.write("\n");
            }
            
            bw.write("Top 50 intersections (compare only the top 50s for each comm.)\n");
            for(int i = 0; i < comm.getNumCommunities(); ++i)
            {
                for(int j = 0; j < comm.getNumCommunities(); ++j)
                {
                    bw.write(intersectionsTop50.get(i).get(j) + "\t");
                }
                bw.write("\n");
            }
        }
    }
}
