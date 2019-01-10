/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation;

import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimplePreferenceData;
import es.uam.eps.ir.socialnetwork.recommendation.data.updateable.GraphSimpleUpdateableFastPreferenceData;
import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastDirectedUnweightedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastDirectedWeightedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.recommendation.ir.BM25TermBasedRecommender;
import org.jooq.lambda.tuple.Tuple3;
import org.ranksys.core.preferences.fast.updateable.FastUpdateablePreferenceData;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.rec.mf.updateable.MFGraphUpdateableRecommender;
import org.ranksys.rec.mf.updateable.UpdateableFactorization;
import org.ranksys.rec.mf.updateable.UpdateableFactorizer;
import org.ranksys.rec.mf.updateable.als.HKVUpdateableFactorizer;
import org.ranksys.rec.updateable.UpdateableRecommender;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation.IN;
import static es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation.OUT;
import static es.uam.eps.ir.socialnetwork.graph.edges.EdgeOrientation.UND;
import org.ranksys.core.Recommendation;
import org.ranksys.core.preference.PreferenceData;
import static org.ranksys.formats.parsing.Parsers.lp;
import org.ranksys.formats.preference.SimpleRatingPreferencesReader;
import org.ranksys.formats.rec.RecommendationFormat;
import org.ranksys.formats.rec.TRECRecommendationFormat;
import org.ranksys.rec.nn.neigh.updateable.CachedTopKUpdateableNeighborhood;
import org.ranksys.rec.nn.neigh.updateable.UpdateableNeighborhood;
import org.ranksys.rec.nn.neigh.updateable.UpdateableUserNeighborhood;
import org.ranksys.rec.nn.sim.updateable.UpdateableSimilarity;
import org.ranksys.rec.nn.sim.updateable.VectorCosineUpdateableSimilarity;
import org.ranksys.rec.nn.user.updateable.UpdateableGraphUserNeighborhoodRecommender;

/**
 * Class for comparing the speed of several recommendation algorithms when we new 
 * users and edges are added to a graph.
 * @author Javier Sanz-Cruzado Puig
 */
public class SpeedComparisonUpdatingRecommendation
{
    /**
     * Computes the corresponding speed.
     * @param args <ul>
     * <li><b>Train:</b> Training data </li>
     * <li><b>Test:</b> Test data</li>
     * <li><b>Directed:</b> "true" if the graph is directed, "false" if not</li>
     * <li><b>Weighted:</b> "true" if the graph is directed, "false" if not</li>
     * <li><b>Max. Length:</b> maximum length of the recommendation ranking for a single user.</li>
     * <li><b>Num. Reps:</b> number of times each algorithm will be executed</li>
     * <li><b>Output folder:</b> the folder in which results will be stored (recommendations and times)</li>
     * </ul>
     * @throws java.io.IOException If something fails while reading.
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 7)
        {
            System.err.println("Invalid arguments.");
            System.err.println("Usage:");
            System.err.println("\ttrain: Training data");
            System.err.println("\ttest: Test data");
            System.err.println("\tdirected: true if the graph is directed, false if not");
            System.err.println("\tweighted: true if the graph is weighted, false if not");
            System.err.println("\tmaxLength: maximum length of the recommendation ranking");
            System.err.println("\tnumReps: times each algorithm will be run");
            System.err.println("\toutputFile: folder in which to store the result");
            return;
        }
        
        // Parameter reading
        String trainDataPath = args[0];
        String testDataPath = args[1];
        boolean directed = args[2].equalsIgnoreCase("true");
        boolean weighted = args[3].equalsIgnoreCase("true");
        int maxLength = Parsers.ip.parse(args[4]);
        int numReps = Parsers.ip.parse(args[5]);
        String output = args[6];
        
        long timea = System.currentTimeMillis();
        
        // Read the graphs
        
        // Training graph
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, true, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(trainDataPath, weighted, false);       
        FastGraph<Long> graph = (FastGraph<Long>) Adapters.removeAutoloops(auxgraph);

        // Test graph
        FastUpdateablePreferenceData<Long, Long> trainData = GraphSimpleUpdateableFastPreferenceData.load(graph);
        PreferenceData<Long, Long> testData = GraphSimplePreferenceData.load(SimpleRatingPreferencesReader.get().read(testDataPath, lp,lp), directed, weighted);
        
        long timeb = System.currentTimeMillis();
        System.out.println("Data read (" +(timeb-timea) + " ms.)");

        Random rng = new Random();
        for(int i = 0; i < numReps; ++i)
        {
            // Get all users
            List<Long> users = graph.getAllNodes().sorted().collect(Collectors.toList());
            Collections.shuffle(users, rng);
            
            //Get the initial number of users (10%)
            int limit = Math.max((10*users.size())/100,1);
            for(int j = 0; j < 3; ++j)
            {
                // Configure the initial graph, containing only 10% users and edges between them.
                FastGraph<Long> initialgraph;
                Set<Long> targetUsers = new HashSet<>();

                if(j == 0)
                {
                    initialgraph = new FastDirectedWeightedGraph<>();
                }
                else
                {
                    initialgraph = new FastDirectedUnweightedGraph<>();
                }

                // Add the nodes.
                for(int k = 0;k < limit; ++k)
                {
                    initialgraph.addNode(users.get(k));
                    targetUsers.add(users.get(k));
                }
                
                // Add the edges
                initialgraph.getAllNodes().forEach(u -> 
                {
                    graph.getAdjacentNodesWeights(u).filter(w -> targetUsers.contains(w.getIdx())).forEach(w -> initialgraph.addEdge(u,w.getIdx(),w.getValue()));
                });
                
                FastUpdateablePreferenceData<Long, Long> data = GraphSimpleUpdateableFastPreferenceData.load(initialgraph);
                // Select the set of users to be recommended, the format, and the filters to apply to the recommendation
                String filename = output;
                
                if(j == 0) filename += "BM25" + "_" + i;
                else if(j == 1) filename += "HKV" + "_" + i;
                else filename += "ub" + "_" + i;

                try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filename + ".txt"))))
                {
                    bw.write("Iteration\tUpdate time\tRecomm. time");
                    bw.write("0:");
                    
                    long a = System.nanoTime();
                    UpdateableRecommender<Long, Long> rec;

                    // Initialize the recommender
                    if(j == 0)
                    {
                        rec = new BM25TermBasedRecommender<>(initialgraph, UND, IN, OUT, 0.1,1);
                    }
                    else if(j == 1)
                    {
                        UpdateableFactorizer<Long, Long> factorizer = new HKVUpdateableFactorizer<>(150.0, x -> (1 + 40.0*x),20);
                        UpdateableFactorization<Long, Long> factorization = factorizer.factorize(10, data);
                        rec = new MFGraphUpdateableRecommender<>(data, factorization, factorizer);
                    }
                    else
                    {
                        UpdateableSimilarity sim = new VectorCosineUpdateableSimilarity((GraphSimpleUpdateableFastPreferenceData<?>) data);
                        UpdateableNeighborhood neigh = new CachedTopKUpdateableNeighborhood(data.numUsers(),sim,10);
                        UpdateableUserNeighborhood unn = new UpdateableUserNeighborhood(data, neigh);
                        rec = new UpdateableGraphUserNeighborhoodRecommender(data, unn, 1);
                    }
                    long b = System.nanoTime();

                    List<Recommendation<Long,Long>> reclist = new ArrayList<>();

                    bw.write("\t" + (b-a));
                    //FastUpdateableGraphIndex<Long> index = new FastUpdateableGraphIndex<>(initialgraph);
                    //Function<Long,IntPredicate> filter = FastFilters.and(FastFilters.notInTrain(data), FastFilters.notSelf(index), SocialFastFilters.notReciprocal(initialgraph,index));
                    //RecommenderRunner<Long,Long> runner = new FastFilterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength);

                    // Generate the first recommendation
                    a = System.nanoTime();
                    //runner.run(rec, x -> reclist.add(x));
                    b = System.nanoTime();
                    bw.write("\t" + (b-a) + "\n");
                    RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
                    
                    // While there are more users, update the recommendations.
                    for(int k = limit; k < graph.getVertexCount(); ++k)
                    {
                        Long u = users.get(k);
                        targetUsers.add(u);
                        Stream<Tuple3<Long,Long,Double>> adjacent = graph.getAdjacentNodesWeights(u).filter(x -> targetUsers.contains(x.getIdx())).map(x -> new Tuple3<>(u,x.getIdx(),x.getValue()));
                        Stream<Tuple3<Long,Long,Double>> incident = graph.getIncidentNodesWeights(u).filter(x -> targetUsers.contains(x.getIdx())).map(x -> new Tuple3<>(x.getIdx(),u,x.getValue()));

                        Stream<Tuple3<Long,Long,Double>> stream = Stream.concat(adjacent, incident);
                        
                        reclist.clear();
                        
                        a = System.nanoTime();
                        rec.updateAddUser(u);
                        rec.updateAddItem(u);
                        rec.update(stream);
                        b = System.nanoTime();
                        bw.write("Iteration " + (k-limit+1) + "\t" + (b-a));
                        //runner = new FastFilterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength);
                        a = System.nanoTime();
                        //runner.run(rec, x -> reclist.add(x));
                        b = System.nanoTime();
                        bw.write("\t" + (b-a) + "\n");
                        
                        if(k % 200 == 0 || k == graph.getVertexCount() - 1)
                        {
                            try(RecommendationFormat.Writer<Long, Long> writer = format.getWriter(filename + k + ".txt"))
                            {
                                for(Recommendation<Long,Long> r : reclist)
                                {
                                    writer.write(r);
                                }
                            }
                        }                     
                    }
                    
                    System.out.println("RECOMMENDER " + ((j == 0) ? "BM25" : "HKV") + "finished");

                }
                catch(IOException ioe)
                {
                    System.err.println("ERROR");
                    return;
                }
            }
        }
    }
}
