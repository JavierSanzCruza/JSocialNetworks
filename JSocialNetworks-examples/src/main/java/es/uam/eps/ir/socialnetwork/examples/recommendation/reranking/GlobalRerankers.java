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
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.metrics.pair.Embededness;
import es.uam.eps.ir.socialnetwork.metrics.vertex.Degree;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.GlobalReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.communities.GlobalCommGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.communities.GlobalInverseCommunitySizeReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.communities.GlobalModularityReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.edge.GlobalInverseEdgeMetricReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.graph.GlobalClusteringCoefficientReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.user.GlobalOriginalInverseUserMetricReranker;
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
public class GlobalRerankers 
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
        
        // Reranker for inverse popularity
        rerankersMap.put("GlobalInversePopularity" + 0.5, () -> 
        {
            return new GlobalOriginalInverseUserMetricReranker(0.5, cutOff, true, graph, new Degree<>(EdgeOrientation.IN));
        });
        
        rerankersMap.put("GlobalInversePopularity" + 1.0, () -> 
        {
            return new GlobalOriginalInverseUserMetricReranker(1.0, cutOff, true, graph, new Degree<>(EdgeOrientation.IN));
        });
        
        // Reranker for community Gini
        rerankersMap.put("GlobalCommunityGini" + 0.5, () ->
        {
           return new GlobalCommGiniReranker(0.5, cutOff, true, graph, communities, EdgeOrientation.IN);
        });
        
        rerankersMap.put("GlobalCommunityGini" + 1.0, () ->
        {
           return new GlobalCommGiniReranker(1.0, cutOff, true, graph, communities, EdgeOrientation.IN);
        });
        /*rerankersMap.put("GlobalCommunityPairGini" + 0.5, () ->
        {
            return new GlobalCommPairGiniReranker(0.5, cutOff, true, graph, communities);
        });*/
        
        // Reranker for modularity
        rerankersMap.put("GlobalModularity" + 0.5, () -> 
        {
            return new GlobalModularityReranker(0.5, cutOff, true, graph, communities);
        });
        
        rerankersMap.put("GlobalModularity" + 1.0, () -> 
        {
            return new GlobalModularityReranker(1.0, cutOff, true, graph, communities);
        });
        
        // Reranker for inverse number of communities
        rerankersMap.put("GlobalInverseCommunitySize" + 0.5, () ->
        {
            return new GlobalInverseCommunitySizeReranker(0.5, cutOff, true, graph, communities);
        });
        
        // Reranker for inverse number of communities
        rerankersMap.put("GlobalInverseCommunitySize" + 1.0, () ->
        {
            return new GlobalInverseCommunitySizeReranker(1.0, cutOff, true, graph, communities);
        });
        
        // Reranker for inverse embededness
        rerankersMap.put("GlobalInverseEmbededness" + 0.5, () -> {
            return new GlobalInverseEdgeMetricReranker(0.5, cutOff, true, graph, new Embededness<>(EdgeOrientation.OUT, EdgeOrientation.IN));
        });
        rerankersMap.put("GlobalInverseEmbededness" + 1.0, () -> {
            return new GlobalInverseEdgeMetricReranker(1.0, cutOff, true, graph, new Embededness<>(EdgeOrientation.OUT, EdgeOrientation.IN));
        });
        
        // Reranker for inverse clustering coefficient.
        rerankersMap.put("GlobalClusteringCoefficient" + 0.5, () -> {
            return new GlobalClusteringCoefficientReranker(0.5, cutOff, true, graph);
        });
        
        // Reranker for inverse clustering coefficient.
        rerankersMap.put("GlobalClusteringCoefficient" + 1.0, () -> {
            return new GlobalClusteringCoefficientReranker(1.0, cutOff, true, graph);
        });
        
        String recName = new File(recFile).getName();

        
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        rerankersMap.forEach(Unchecked.biConsumer((name, rerankerSupplier) -> {
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
