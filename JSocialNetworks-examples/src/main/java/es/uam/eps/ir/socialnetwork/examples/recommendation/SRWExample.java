/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation;

import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimpleFastPreferenceData;
import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.filter.SocialFastFilters;
import es.uam.eps.ir.socialnetwork.recommendation.ml.ModifiablePattern;
import es.uam.eps.ir.socialnetwork.recommendation.ml.SupervisedRandomWalkRecommender;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.stream.Collectors;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.evaluation.runner.RecommenderRunner;
import org.ranksys.evaluation.runner.fast.FastFilterRecommenderRunner;
import org.ranksys.evaluation.runner.fast.FastFilters;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;
import org.ranksys.recommenders.Recommender;

/**
 * Uses the Supervised Random Walk recommender
 * @author Javier Sanz-Cruzado Puig
 */
public class SRWExample 
{
    /**
     * Program that applies the Supervised Random Walk recommender method
     * @param args Execution arguments
     * <ul>
     *  <li><b>Training data:</b> Route to the training graph</li>
     *  <li><b>Training patterns:</b> Route to the training patterns</li>
     *  <li><b>Test patterns:</b> Route to the test patterns</li>
     *  <li><b>Classes:</b> Route to the classes file for the different users</li>
     *  <li><b>Num. Patt.:</b> Number of patterns</li>
     * </ul>
     * @throws IOException if something fails while reading the data or writing the recommendations
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 5)
        {
            System.err.println("Usage: <trainData> <trainPat> <testPat> <classes> <numPat>");
            return;
        }

        double b = 1.0;
        double lambda = 1.0;
        double alpha = 0.3;
        
        Map<Long,Map<Long,ModifiablePattern>> trainPatterns = new HashMap<>();
        Map<Long,Map<Long,ModifiablePattern>> testPatterns = new HashMap<>();
        Map<Long, Pair<List<Long>>> classes = new HashMap<>();      
        
        String trainDataPath = args[0];
        String trainPatternsPath = args[1];
        String testPatternsPath = args[2];
        String classesPath = args[3];
        int numPatterns = new Integer(args[4]);
        
        // Read the training patterns
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(trainPatternsPath))))
        {
            br.lines().forEach(line -> {
                String[] split = line.split("\t");
                Long u = new Long(split[0]);
                Long v = new Long(split[1]);
                String[] vector = split[2].split(",");
                
                
                ModifiablePattern instance = new ModifiablePattern(vector.length);
                for(int i = 0; i < vector.length; ++i)
                {
                    instance.setValue(i, new Double(vector[i]));
                }
                
                if(!trainPatterns.containsKey(u))
                    trainPatterns.put(u, new HashMap<>());
                trainPatterns.get(u).put(v, instance);
            });
        }
        
        // Read the test patterns
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(testPatternsPath))))
        {
            br.lines().forEach(line -> {
                String[] split = line.split("\t");
                Long u = new Long(split[0]);
                Long v = new Long(split[1]);
                String[] vector = split[2].split(",");
                
                
                ModifiablePattern instance = new ModifiablePattern(vector.length);
                for(int i = 0; i < vector.length; ++i)
                {
                    instance.setValue(i, new Double(vector[i]));
                }
                
                if(!testPatterns.containsKey(u))
                    testPatterns.put(u, new HashMap<>());
                testPatterns.get(u).put(v, instance);
            });
        }
        
        
        // Read the classes
        try(BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(classesPath))))
        {
            br.lines().forEach(line -> {
                String[] split = line.split("\t");
                Long u = new Long(split[0]);
                
                if(!classes.containsKey(u))
                {
                    classes.put(u, new Pair<>(new ArrayList<>(), new ArrayList<>()));
                }
                
                if(!split[1].equals("-1"))
                {
                    String[] positives = split[1].split(",");
                    for(String v : positives)
                    {
                        classes.get(u).v1().add(new Long(v));
                    }
                }
                
                if(!split[2].equals("-1"))
                {
                    String[] negatives = split[2].split(",");
                    for(String v : negatives)
                    {
                        classes.get(u).v2().add(new Long(v));
                    }
                }
            });
        }
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, false, false, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(trainDataPath, false, false);
        FastGraph<Long> graph = (FastGraph<Long>) Adapters.removeAutoloops(auxgraph);
        
        FastPreferenceData<Long,Long> trainData  = GraphSimpleFastPreferenceData.load(graph);
        GraphIndex<Long> index = new FastGraphIndex<>(graph);
//        FastPreferenceData<Long,Long> testData  = SimpleFastPreferenceData.load(SimpleRatingPreferencesReader.get().read(testDataPath, lp, lp), userIndex, itemIndex);
                
        System.out.println("Preparing SRW_"+alpha+"_"+lambda+"_"+b);
        Long ta = System.currentTimeMillis();
        Recommender<Long,Long> rec = new SupervisedRandomWalkRecommender<>(graph,trainPatterns,testPatterns,classes,numPatterns,alpha,lambda,b);
        Long tb = System.currentTimeMillis();
        System.out.println("Prepared SRW_"+alpha+"_"+lambda+"_" + b + "(" + (tb - ta) + " ms.)");
        
        Set<Long> targetUsers = index.getAllUsers().collect(Collectors.toSet());
        RecommendationFormat<Long,Long> format = new TRECRecommendationFormat<>(lp,lp);
        Function<Long, IntPredicate> filter = FastFilters.and(FastFilters.notInTrain(trainData),FastFilters.notSelf(index),SocialFastFilters.notReciprocal(graph,index));
        int maxLength = 10;
        RecommenderRunner<Long,Long> runner = new FastFilterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength);
        
        System.out.println("Running SRW_" + alpha + "_" + lambda + "_" + b);
        ta = System.currentTimeMillis();
        try(RecommendationFormat.Writer<Long, Long> writer = format.getWriter("SRW_"+alpha+"_"+lambda+"_" + b)) {
                runner.run(rec, writer);
            }
        catch(IOException ioe)
        {
            System.out.println("Running SRW_" + alpha + "_" + lambda + "_" + b + " failed");
        }
        tb = System.currentTimeMillis();
        System.out.println("Done SRW_"+alpha+"_"+lambda+"_" + b + "(" + (tb - ta) + " ms.)");

    }
    
    
}

