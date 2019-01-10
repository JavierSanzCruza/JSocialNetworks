/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation;

import es.uam.eps.ir.ranksys.socialextension.FastFilterInverterRecommenderRunner;
import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSelector;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridReader;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.data.GraphSimpleFastPreferenceData;
import es.uam.eps.ir.socialnetwork.recommendation.filter.SocialFastFilters;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
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
 * Recommends users using a grid.
 * @author Javier Sanz-Cruzado Puig
 */
public class GridRecommendation 
{
    /**
     * Identifier for the user selection mode generates recommendations for all users in the training graph.
     */
    private final static String ALLUSERS = "all";
    /**
     * Identifier for the user selection mode which generates recommendations for all users which create a link in the test graph.
     */
    private final static String TESTUSERS = "test";
    /**
     * Computes the recommendations
     * @param args <ul>
     * <li><b>Train:</b> Training data </li>
     * <li><b>Test:</b> Test data</li>
     * <li><b>Grid:</b> Location of the XML grid file</li>
     * <li><b>Output:</b> Directory for storing the recommendations</li>
     * <li><b>Directed:</b> "true" if the graph is directed, "false" if not</li>
     * <li><b>Weighted:</b> "true" if the graph is directed, "false" if not</li>
     * <li><b>User Selection:</b> "all" if we want to generate recommendations to all nodes in train, "test" if we only
     * want to generate recommendations for all users which have created a new link in test</li>
     * <li><b>Max. Length:</b> maximum length of the recommendation ranking for a single user.</li>
     * </ul>
     */
    public static void main(String[] args) throws InterruptedException, ExecutionException
    {
        if(args.length < 11)
        {
            System.err.println("Invalid arguments.");
            System.err.println("Usage:");
            System.err.println("\ttrain: Training data");
            System.err.println("\ttest: Test data");
            System.err.println("\tgrid: Location of the XML grid file");
            System.err.println("\toutput: Directory for storing the recommendations");
            System.err.println("\tdirected: true if the graph is directed, false if not");
            System.err.println("\tweighted: true if the graph is weighted, false if not");
            System.err.println("\tuserSelection : all if all users in train, test if all users which have ratings in test");
            System.err.println("\tcandidateSelection: all if all users, test if all users with indegree > 0");
            System.err.println("\tmaxLength: maximum length of the recommendation ranking");
            System.err.println("\tinvert: true if the ranking has to be inverted");
            System.err.println("\toverwrite: true if files have to be overwritten");
            System.err.println("\tcores: -core number : optional parameter to delimit the number of cores");
            return;
        }
        
        String trainDataPath = args[0];
        String testDataPath = args[1];
        String gridPath = args[2];
        String outputPath = args[3];
        boolean directed = args[4].equalsIgnoreCase("true");
        boolean weighted = args[5].equalsIgnoreCase("true");
        String userSelection = args[6];
        String candidateSelection = args[7];
        int maxLength = Parsers.ip.parse(args[8]);
        boolean invert = args[9].equalsIgnoreCase("true");
        boolean overwrite = args[10].equalsIgnoreCase("true");
        
        int numCores = Runtime.getRuntime().availableProcessors();
        if(args.length > 11)
        {
            if(args[11].equalsIgnoreCase("-core"))
            {
                numCores = Parsers.ip.parse(args[12]);
            }
        }
        
        long timea = System.currentTimeMillis();
        
        // Read the user and item indexes      
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, true, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(trainDataPath, weighted, false);       
        FastGraph<Long> graph = (FastGraph<Long>)Adapters.removeAutoloops(auxgraph);
        FastGraph<Long> complgraph = (FastGraph<Long>)Adapters.addAllAutoloops(graph);
        auxgraph = greader.read(testDataPath, weighted, false);
        FastGraph<Long> testGraph = (FastGraph<Long>) Adapters.onlyTrainUsers(auxgraph, graph);
        
        long timeb = System.currentTimeMillis();
        System.out.println("Data read (" +(timeb-timea) + " ms.)");
        timea = System.currentTimeMillis();

        // Read the training and test data
        FastPreferenceData<Long, Long> trainData;
        trainData = GraphSimpleFastPreferenceData.load(graph);
        
        FastPreferenceData<Long, Long> testData;
        testData = GraphSimpleFastPreferenceData.load(testGraph);
        GraphIndex<Long> index = new FastGraphIndex<>(graph);
        
        // Read the XML containing the parameter grid for each algorithm
        AlgorithmGridReader gridreader = new AlgorithmGridReader(gridPath);
        gridreader.readDocument();
        Map<String, Supplier<Recommender<Long,Long>>> recMap = new HashMap<>();
        // Get the different recommenders to execute
        gridreader.getAlgorithms().forEach(algorithm -> 
        {
            AlgorithmGridSelector<Long> ags = new AlgorithmGridSelector<>();
            if(algorithm.startsWith("Complementary"))
            {
                recMap.putAll(ags.getRecommenders(algorithm, gridreader.getGrid(algorithm), complgraph, trainData));
            }
            else
            {
                recMap.putAll(ags.getRecommenders(algorithm, gridreader.getGrid(algorithm), graph, trainData));
            }
        });
        
        timeb = System.currentTimeMillis();
        System.out.println("Algorithms selected (" +(timeb-timea) + " ms.)");

        // Select the set of users to be recommended, the format, and the filters to apply to the recommendation
        Set<Long> targetUsers = selectUsers(userSelection, trainData, testData);
        
        System.out.println("Num. target users: " + targetUsers.size());
        RecommendationFormat<Long, Long> format = new TRECRecommendationFormat<>(lp,lp);
        
        Function<Long,IntPredicate> filter;
        if(candidateSelection.equalsIgnoreCase(ALLUSERS))
        {
            filter = FastFilters.and(FastFilters.notInTrain(trainData), FastFilters.notSelf(index));
           // filter = FastFilters.and(FastFilters.notInTrain(trainData), FastFilters.notSelf(index), SocialFastFilters.notReciprocal(graph,index));
        }
        else
        {
            filter = FastFilters.and(FastFilters.notInTrain(trainData), FastFilters.notSelf(index), SocialFastFilters.notReciprocal(graph,index), SocialFastFilters.onlyFollowedUsers(testGraph)); 
        }        
        
        RecommenderRunner<Long,Long> runner = invert ?  new FastFilterInverterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength) :
                                                        new FastFilterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength);        

        ForkJoinPool customThreadPool = new ForkJoinPool(numCores);
        customThreadPool.submit(() ->
            recMap.entrySet().parallelStream().forEach(entry -> 
            {
                String name = entry.getKey();
                File f = new File(outputPath + name + ".txt");
                if(!f.exists() || overwrite)
                {

                    System.out.println("Preparing " + name);
                    Supplier<Recommender<Long,Long>> recomm = entry.getValue();

                    long a = System.currentTimeMillis();
                    Recommender<Long, Long> rec = recomm.get();
                    long b = System.currentTimeMillis();
                    System.out.println("Prepared " + name + " (" + (b-a) + " ms.)");

                    try(RecommendationFormat.Writer<Long, Long> writer = format.getWriter(outputPath + name + ".txt"))
                    {
                        System.out.println("Running " + name);
                        a = System.currentTimeMillis();
                        runner.run(rec, writer);
                        b = System.currentTimeMillis();
                        System.out.println("Done " + name + " (" + (b-a) + " ms.)");
                    }
                    catch(IOException ioe)
                    {
                        System.err.println("Algorithm " + name + " failed");
                    }
                }
                else
                {
                    System.out.println("Recommendation " + name + " already computed");
                }
            })
        ).get();
    }
    
    /**
     * Select the users to be recommended.
     * @param userSelection "all" if we want to generate recommendations to all nodes in train, "test" if we only
     * want to generate recommendations for all users which have created a new link in test. In other case, the set 
     * will be empty.
     * @param trainData training data.
     * @param testData test data.
     * @return the selected set of users.
     */
    private static Set<Long> selectUsers(String userSelection, FastPreferenceData<Long, Long> trainData, FastPreferenceData<Long, Long> testData)
    {
        String sel = userSelection.toLowerCase();
        Set<Long> users;
        switch(sel)
        {
            case ALLUSERS:
                users = trainData.getAllUsers().collect(Collectors.toCollection(HashSet::new));
                break;
            case TESTUSERS:
                users = testData.getUsersWithPreferences().collect(Collectors.toCollection(HashSet::new));
                break;
            default:
                users = new HashSet<>();
        }
        return users;
    }
}
