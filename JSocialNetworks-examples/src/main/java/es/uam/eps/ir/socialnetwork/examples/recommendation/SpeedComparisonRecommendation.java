/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.recommendation;

import es.uam.eps.ir.socialnetwork.recommendation.data.AuxGraphSimpleFastPreferenceData;
import es.uam.eps.ir.socialnetwork.adapters.Adapters;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridReader;
import es.uam.eps.ir.socialnetwork.grid.recommendation.AlgorithmGridSelector;
import es.uam.eps.ir.socialnetwork.recommendation.FastGraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.GraphIndex;
import es.uam.eps.ir.socialnetwork.recommendation.filter.SocialFastFilters;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.ranksys.core.Recommendation;
import org.ranksys.core.preference.fast.FastPreferenceData;
import org.ranksys.evaluation.runner.RecommenderRunner;
import org.ranksys.evaluation.runner.fast.FastFilterRecommenderRunner;
import org.ranksys.evaluation.runner.fast.FastFilters;
import org.ranksys.formats.parsing.Parsers;
import org.ranksys.recommenders.Recommender;

/**
 * Recommends users using a grid.
 * @author Javier Sanz-Cruzado Puig
 */
public class SpeedComparisonRecommendation 
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
     * <li><b>Index:</b> Path to the user index</li>
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
    public static void main(String[] args)
    {
        if(args.length < 10)
        {
            System.err.println("Invalid arguments.");
            System.err.println("Usage:");
            System.err.println("\ttrain: Training data");
            System.err.println("\ttest: Test data");
            System.err.println("\tgrid: Location of the XML grid file");
            System.err.println("\tdirected: true if the graph is directed, false if not");
            System.err.println("\tweighted: true if the graph is weighted, false if not");
            System.err.println("\tuserSelection : all if all users in train, test if all users which have ratings in test");
            System.err.println("\tcandidateSelection: all if all users, test if all users with indegree > 0");
            System.err.println("\tmaxLength: maximum length of the recommendation ranking");
            System.err.println("\tnumReps: times each algorithm will be run");
            System.err.println("\toutputFile: file in which to store the result");
            return;
        }
        
        String trainDataPath = args[0];
        String testDataPath = args[1];
        String gridPath = args[2];
        boolean directed = args[3].equalsIgnoreCase("true");
        boolean weighted = args[4].equalsIgnoreCase("true");
        String userSelection = args[5];
        String candidateSelection = args[6];
        int maxLength = Parsers.ip.parse(args[7]);
        int numReps = Parsers.ip.parse(args[8]);
        String output = args[9];
        
        long timea = System.currentTimeMillis();
        
        // Read the graphs
        TextGraphReader<Long> greader = new TextGraphReader<>(false, directed, weighted, true, "\t", Parsers.lp);
        Graph<Long> auxgraph = greader.read(trainDataPath, weighted, false);       
        FastGraph<Long> graph = (FastGraph<Long>) Adapters.removeAutoloops(auxgraph);
        FastGraph<Long> complgraph = (FastGraph<Long>)Adapters.addAllAutoloops(graph);

        auxgraph = greader.read(testDataPath, weighted, false);
        FastGraph<Long> testGraph = (FastGraph<Long>) Adapters.onlyTrainUsers(auxgraph, graph);
        
        GraphIndex<Long> index = new FastGraphIndex<>(graph);
        
        FastPreferenceData<Long, Long> trainData = AuxGraphSimpleFastPreferenceData.load(graph);
        FastPreferenceData<Long, Long> testData = AuxGraphSimpleFastPreferenceData.load(testGraph);
        
        long timeb = System.currentTimeMillis();
        System.out.println("Data read (" +(timeb-timea) + " ms.)");
        timea = System.currentTimeMillis();

        // Read the XML containing the parameter grid for each algorithm
        AlgorithmGridReader gridreader = new AlgorithmGridReader(gridPath);
        gridreader.readDocument();
        Map<String, Supplier<Recommender<Long,Long>>> recMap = new HashMap<>();
        
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
        
        Function<Long,IntPredicate> filter;
        if(candidateSelection.equalsIgnoreCase(ALLUSERS))
        {
            filter = FastFilters.and(FastFilters.notInTrain(trainData), FastFilters.notSelf(index), SocialFastFilters.notReciprocal(graph,index));
        }
        else
        {
            filter = FastFilters.and(FastFilters.notInTrain(trainData), FastFilters.notSelf(index), SocialFastFilters.notReciprocal(graph,index), SocialFastFilters.onlyFollowedUsers(testGraph)); 
        }        
        
        RecommenderRunner<Long,Long> runner = new FastFilterRecommenderRunner<>(index, index, targetUsers.stream(), filter, maxLength);
        
        try(BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output))))
        {
            bw.write("algorithm\tprep. time\trun time\ttotal time\n");
            for(Entry<String, Supplier<Recommender<Long,Long>>> entry : recMap.entrySet())
            {
                String name = entry.getKey();
                double preparation = 0;
                double running = 0;

                bw.write(name);
                System.out.println(name + " started");
                
                for(int i = 0; i < numReps; ++i)
                {
                    Supplier<Recommender<Long,Long>> recomm = entry.getValue();

                    long a = System.currentTimeMillis();
                    Recommender<Long, Long> rec = recomm.get();
                    long b = System.currentTimeMillis();
                    preparation += (b-a);

                    Consumer<Recommendation<Long,Long>> cons = (Recommendation<Long, Long> t) -> {
                    };

                    a = System.currentTimeMillis();
                    runner.run(rec, cons);
                    b = System.currentTimeMillis();
                    running += (b-a);

                }
                bw.write("\t" + preparation/(numReps+0.0));
                bw.write("\t" + running/(numReps + 0.0));
                bw.write("\t" + (running + preparation)/(numReps + 0.0) + "\n");
                System.out.println(name + " finished");
            }
        }
        catch(IOException ioe)
        {
            System.err.println("Something failed while writing");
        }
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
