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
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.GlobalReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.local.communities.StrongTiesReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.local.communities.WeakTiesReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.CompleteCommunityDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.CompleteCommunityOuterDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.InterCommunityDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.InterCommunityOuterDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.sizenormalized.SizeNormalizedCompleteCommunityDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.sizenormalized.SizeNormalizedCompleteCommunityOuterDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.sizenormalized.SizeNormalizedInterCommunityDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.degree.sizenormalized.SizeNormalizedInterCommunityOuterDegreeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.CompleteCommunityEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.InterCommunityEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.InterCommunityOuterEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.SemiCompleteCommunityEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.SemiCompleteCommunityOuterEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.sizenormalized.SizeNormalizedCompleteCommunityEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.sizenormalized.SizeNormalizedInterCommunityEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.sizenormalized.SizeNormalizedInterCommunityOuterEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.sizenormalized.SizeNormalizedSemiCompleteCommunityEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.comm.gini.edge.sizenormalized.SizeNormalizedSemiCompleteCommunityOuterEdgeGiniReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.edge.HeuristicEmbedednessReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.graph.ClusteringCoefficientReranker;
import es.uam.eps.ir.socialnetwork.recommendation.reranking.global.swap.graph.DegreeGiniReranker;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.jooq.lambda.Unchecked;
import org.openide.util.Exceptions;
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
public class SwapRerankers 
{
    /**
     * Generates graph based rerankers for a certain recommender.
     * @param args <ul>
     *  <li><b>recFile:</b> The recommendation file</li>
     *  <li><b>graphFile:</b> A file containing a graph</li>
     *  <li><b>communitiesFile:</b> A file containing the community partition</li>
     *  <li><b>outputFolder:</b> The folder where we want to store the reranker files</li>
     *  <li><b>cutOff:</b> The number of recommendations for each user
     * </ul>
     * @throws IOException if something fails while reading/writing.
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
        
        double[] lambdas = new double[]{0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1.0};
        //double[] lambdas = new double[]{1.0};
        
        for(double l : lambdas)
        {
            double lambda = l;
            
            // Basic weak and strong ties rerankers:
            rerankersMap.put("WeakTies" + lambda, () -> 
            {
                return new WeakTiesReranker(lambda, cutOff, true, true, graph, communities);
            });
            
            rerankersMap.put("StrongTies" + lambda, () -> 
            {
                return new StrongTiesReranker(lambda, cutOff, true, graph, communities, true);
            });
            
            // Embeddedness and clustering coefficient rerankers
            rerankersMap.put("Embeddedness" + lambda, () -> 
            {
                return new HeuristicEmbedednessReranker(lambda,cutOff,  true, true, graph, 3);
            });
            
            rerankersMap.put("ClusteringCoefficient" + lambda, () -> 
            {
                return new ClusteringCoefficientReranker(lambda, cutOff, true, true, graph);
            });
            
            // In-degree reranker
            rerankersMap.put("IndegreeGini" + lambda, () -> 
            {
                return new DegreeGiniReranker(lambda, cutOff, true, true, graph, EdgeOrientation.IN);
            });
            
            // Community Edge Gini Rerankers
            // Inter-community
            rerankersMap.put("InterCommunityEdgeGini" + lambda, () -> 
            {
               return new InterCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities); 
            });
            
            rerankersMap.put("SizeNorm-InterCommunityEdgeGini" + lambda, () -> 
            {
               return new SizeNormalizedInterCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities); 
            });
            
            rerankersMap.put("InterCommunityOuterEdgeGini" + lambda, () -> 
            {
               return new InterCommunityOuterEdgeGiniReranker(lambda, cutOff, true, true, graph, communities); 
            });
            
            rerankersMap.put("SizeNorm-InterCommOuterEdgeGini" + lambda, () -> 
            {
               return new SizeNormalizedInterCommunityOuterEdgeGiniReranker(lambda, cutOff, true, true, graph, communities); 
            });
            
            // Semi-complete
            
            rerankersMap.put("SemiCompleteCommEdgeGini" + lambda, () -> 
            {
               return new SemiCompleteCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false);
            });
            
            rerankersMap.put("SizeNorm-SemiCompleteCommEdgeGini" + lambda, () -> 
            {
               return new SizeNormalizedSemiCompleteCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false); 
            });
            
            rerankersMap.put("SemiCompleteOuterCommEdgeGini" + lambda, () -> 
            {
               return new SemiCompleteCommunityOuterEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false);
            });
            
            rerankersMap.put("SizeNorm-SemiCompleteOuterCommEdgeGini" + lambda, () -> 
            {
               return new SizeNormalizedSemiCompleteCommunityOuterEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false); 
            });
            
            // Complete
            
            rerankersMap.put("CompleteCommEdgeGini" + lambda, () -> 
            {
               return new CompleteCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false);
            });
            
            rerankersMap.put("SizeNorm-CompleteCommEdgeGini" + lambda, () -> 
            {
               return new SizeNormalizedCompleteCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false); 
            });
            
            rerankersMap.put("CompleteOuterCommEdgeGini" + lambda, () -> 
            {
               return new CompleteCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false);
            });
            
            rerankersMap.put("SizeNorm-CompleteOuterCommEdgeGini" + lambda, () -> 
            {
               return new SizeNormalizedCompleteCommunityEdgeGiniReranker(lambda, cutOff, true, true, graph, communities, false); 
            });
            
            // Community Degree Gini
            // Inter-comm. links
            rerankersMap.put("InterCommunityInDegreeGini" + lambda, () -> 
            {
               return new InterCommunityDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, EdgeOrientation.IN); 
            });
            
            rerankersMap.put("SizeNorm-InterCommunityInDegreeGini" + lambda, () -> 
            {
               return new SizeNormalizedInterCommunityDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, EdgeOrientation.IN);
            });
            
            rerankersMap.put("InterCommunityOuterInDegreeGini" + lambda, () -> 
            {
               return new InterCommunityOuterDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, EdgeOrientation.IN); 
            });
            
            rerankersMap.put("SizeNorm-InterCommunityOuterInDegreeGini" + lambda, () -> 
            {
               return new SizeNormalizedInterCommunityOuterDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, EdgeOrientation.IN);
            });
            
            // Complete
            rerankersMap.put("CompleteCommunityInDegreeGini" + lambda, () -> 
            {
               return new CompleteCommunityDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, false, EdgeOrientation.IN); 
            });
            
            rerankersMap.put("SizeNorm-CompleteCommunityInDegreeGini" + lambda, () -> 
            {
               return new SizeNormalizedCompleteCommunityDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, false, EdgeOrientation.IN);
            });
            
            rerankersMap.put("CompleteCommunityOuterInDegreeGini" + lambda, () -> 
            {
               return new CompleteCommunityOuterDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, false, EdgeOrientation.IN); 
            });
            
            rerankersMap.put("SizeNorm-CompleteCommunityOuterInDegreeGini" + lambda, () -> 
            {
               return new SizeNormalizedCompleteCommunityOuterDegreeGiniReranker(lambda, cutOff, true, true, graph, communities, false, EdgeOrientation.IN);
            });
        }
        
        String recName = new File(recFile).getName();
        
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        rerankersMap.entrySet().stream().forEach(entry -> {
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
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        
        
    }
}
