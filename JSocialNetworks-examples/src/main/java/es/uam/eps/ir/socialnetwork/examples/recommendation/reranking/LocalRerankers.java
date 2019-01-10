/* 
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Autónoma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation.reranking;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.io.CommunitiesReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.GlobalReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.local.communities.WeakTiesReranker;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jooq.lambda.Unchecked;
import org.ranksys.core.preference.PreferenceData;
import org.ranksys.core.preference.SimplePreferenceData;
import org.ranksys.formats.parsing.Parsers;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.formats.preference.SimpleRatingPreferencesReader;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;

/**
 * Example for generating graph based rerankers for a certain recommender.
 * @author Javier Sanz-Cruzado Puig
 */
public class LocalRerankers 
{
    /**
     * Generates graph based rerankers for a certain recommender.
     * @param args <ol>
     *  <li>recFile The recommendation file</li>
     *  <li>graphFile A file containing a graph</li>
     *  <li>outputFolder The folder where we want to store the reranker files</li>
     *  <li>cutOff The number of recommendations for Each user
     * </ol>
     * @throws IOException if something fails while reading or writing files
     */
    public static void main(String args[]) throws IOException
    {
        if(args.length < 5)
        {
            System.err.println("Usage: <recFile> <graphFile> <communitiesFile> <outputFolder> <cutOff>");
            return;
        }
        
        String recFile = args[0];
        String graphFile = args[1];
        String communitiesFile = args[2];
        String outputFolder = args[3];
        final int cutOff = new Integer(args[4]);
        
        TextGraphReader<Long> greader = new TextGraphReader<>(false, true, false, false, "\t", Parsers.lp);
        Graph<Long> graph = greader.read(graphFile, false, false);
        
        CommunitiesReader<Long> cr = new CommunitiesReader<>();
        final Communities<Long> communities = cr.read(communitiesFile, "\t", lp);
        PreferenceData<Long, Long> trainData = SimplePreferenceData.load(SimpleRatingPreferencesReader.get().read(recFile, lp, lp));
        Map<String, Supplier<GlobalReranker<Long,Long>>> rerankersMap = new HashMap<>();
        
        //Reranker for modularity
        
        double[] lambdas = {0.0,0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        for(double l : lambdas)
        {
            rerankersMap.put("mod" + l, () -> 
            {
                return new WeakTiesReranker(l, cutOff, true, true, graph, communities);
            });
        }
               
        String recName = new File(recFile).getName();
        
        
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        rerankersMap.entrySet().forEach(Unchecked.consumer((entry) -> 
        {
            String name = entry.getKey();
            Supplier<GlobalReranker<Long,Long>> rerankerSupplier = entry.getValue();
            System.out.println("Running " + name);
            String recOut = String.format("%s_%s", outputFolder + recName, name + ".txt");
            GlobalReranker<Long,Long> reranker = rerankerSupplier.get();
            try (RecommendationFormat.Writer<Long, Long> writer = format.getWriter(recOut)) 
            {
                long startTime = System.nanoTime();
                reranker.rerankRecommendations(format.getReader(recFile).readAll(), cutOff)
                        .forEach(Unchecked.consumer(writer::write));
                long difference = System.nanoTime() - startTime;
                System.out.println(name + ": " + TimeUnit.NANOSECONDS.toSeconds(difference) + "," + (TimeUnit.NANOSECONDS.toMillis(difference) - TimeUnit.NANOSECONDS.toSeconds(difference)*1000) + " s.");
            }
        }));
        
        
    }
}
