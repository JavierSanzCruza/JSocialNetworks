/*
 *  Copyright (C) 2016 Information Retrieval Group at Universidad Aut�noma
 *  de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.examples.informationpropagation;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.io.TextGraphReader;
import es.uam.eps.ir.socialnetwork.grid.informationpropagation.SimulationParamReader;
import es.uam.eps.ir.socialnetwork.grid.informationpropagation.SimulatorSelector;
import es.uam.eps.ir.socialnetwork.informationpropagation.data.Data;
import es.uam.eps.ir.socialnetwork.informationpropagation.data.filter.DataFilter;
import es.uam.eps.ir.socialnetwork.informationpropagation.io.DataReader;
import es.uam.eps.ir.socialnetwork.informationpropagation.io.backup.BinarySimulationWriter;
import es.uam.eps.ir.socialnetwork.informationpropagation.io.backup.SimulationWriter;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Simulation;
import es.uam.eps.ir.socialnetwork.informationpropagation.simulation.Simulator;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Tuple2oo;
import java.io.IOException;
import org.ranksys.formats.parsing.Parsers;

/**
 * Executes recommendations over a graph, extended by a contact recommendation output.
 * @author Javier Sanz-Cruzado Puig
 */
public class GridInformationPropagationSimulatorFromRecommendation 
{
    /**
     * Executes a simulation
     * @param args Execution parameters
     * <ol>
     *  <li><b>User Index Path:</b> Route to the file containing the list of users</li>
     *  <li><b>Information Index Path:</b> Route to the file containing the identifiers of the information pieces</li>
     *  <li><b>Graph file:</b> The route to the file where the training graph is stored </li>
     *  <li><b>Test graph file:</b> The route to the file where the test graph is stored </li>
     *  <li><b>Multigraph:</b> True if the graph is multigraph, false if it is not.</li>
     *  <li><b>Directed:</b> True if the graph is directed, false if it is not</li>
     *  <li><b>Weighted:</b> True if the graph is weighted, false if it is not</li>
     *  <li><b>Read Types:</b> True if the types of the edges have to be read, false it not</li>
     *  <li><b>Recommendation file:</b> File containing the recommended files, in TREC format</li>
     *  <li><b>Top N:</b> The maximum number of links to pick on each recommendation</li>
     *  <li><b>Information file: </b> Route to the file where the relation between users and information pieces is stored </li>
     *  <li><b>User feature files: </b> Separated by commas, the list of files which contain the features for the nodes in the graph </li>
     *  <li><b>Information pieces features files: </b> Separated by commas, the list of files containing the features for the information pieces</li>
     *  <li><b>Real propagated information:</b> A file containing a relation between users and propagated information</li>
     *  <li><b>Configuration:</b> Configuration file for the simulations to run</li>
     *  <li><b>Output folder: </b> Folder in which the simulations are stored.</li>
     *  <li><b>Number of reps.: </b> Times a simulation will be repeated.</li>
     *  <li><b>Backup file:</b> File for storing the backup of the simulation</li>
     * </ol>
     * @throws java.io.IOException if something fails while reading / writing.
     */
    public static void main(String[] args) throws IOException
    {
        if(args.length < 17)
        {
            System.err.println("Invalid arguments.");
            System.err.println("Usage:");
            System.err.println("\tuIndexPath: Path to the a file containing the list of users");
            System.err.println("\tiIndexPath: Path to a file containing a list of information pieces identifiers");
            System.err.println("\tgraphFile: Path to a file containing a graph");
            System.err.println("\ttestGraphFile: Path to a file containing the test graph");
            System.err.println("\tmultigraph: Indicates if the graph is (true) or not (false) a multigraph");
            System.err.println("\tdirected: Indicates if the graph is directed (true) or not (false)");
            System.err.println("\tweighted: Indicates if the graph is weighted (true) or not (false)");
            System.err.println("\treadTypes: Indicates if the graph types have to be read from file (true) or not (false)");
            System.err.println("\trecFile: Path to the recommendation (in TREC format)");
            System.err.println("\ttopN: Maximum number of users to obtain from each recommendation");
            System.err.println("\tinfoFile: File containing the relation between users and info. pieces");
            System.err.println("\tuserFeatureFiles: Comma-separated, files containing the features for the nodes in the graph");
            System.err.println("\tinfoPiecesFeatureFiles: Comma-separated, files containing the features for the information pieces");
            System.err.println("\trealPropagatedInfoFile: A file containing a relation between users and propagated information");
            System.err.println("\tconfiguration: XML file containing the configuration for the simulation");
            System.err.println("\toutput: Directory for storing the outcomes of the simulation");
            System.err.println("\tnumReps: Number of times each simulation will be executed");
            System.err.println("\tbackupFile: File for storing the backup of the simulation");            
            return;
        }
        
        String uIndexPath = args[0];
        String iIndexPath = args[1];
        String graphFile = args[2];
        String testGraphFile = args[3];
        boolean multigraph = args[4].equalsIgnoreCase("true");
        boolean directed = args[5].equalsIgnoreCase("true");
        boolean weighted = args[6].equalsIgnoreCase("true");
        boolean readTypes = args[7].equalsIgnoreCase("true");
        String recFile = args[8];
        int topN = Parsers.ip.parse(args[9]);
        String infoFile = args[10];
        String[] userParamFiles = args[11].split(",");
        String[] infoPiecesParamFiles = args[12].split(",");
        String realPropInfo = args[13];
        String configuration = args[14];
        String output = args[15];
        int numReps = Parsers.ip.parse(args[16]);
        String backupFile = (args.length > 17 ? args[17] : null);
                
        // Read the data
        long timea = System.currentTimeMillis();
        DataReader<Long, Long, Long> datareader = new DataReader<>(); 
        Data<Long,Long,Long> data = datareader.readData(multigraph, directed, weighted, readTypes, uIndexPath, iIndexPath, graphFile, recFile, topN, infoFile, userParamFiles, infoPiecesParamFiles, realPropInfo, Parsers.lp, Parsers.lp, Parsers.lp);
        
        TextGraphReader<Long> greader = new TextGraphReader<>(multigraph, directed, weighted, false, "\t", Parsers.lp);
        Graph<Long> testGraph = greader.read(testGraphFile, weighted, readTypes);
        long timeb = System.currentTimeMillis();
        
        System.out.println("Data read (" + (timeb-timea) + " ms.)");
        
        String[] recRoute = recFile.split("/");
        //String[] recRoute = recFile.split("\\Q\\\\E");
        String rec = recRoute[recRoute.length -1];
        System.out.println(rec + ": Data read (" + (timeb-timea) + " ms.)");
        
        SimulationParamReader simReader = new SimulationParamReader(configuration);
        simReader.readDocument();
        
        SimulatorSelector<Long, Long, Long> simSel = new SimulatorSelector<>(Parsers.lp);
        

        for(int i = 0; i < simReader.numberSimulations(); ++i)
        {
            System.out.println(simReader.printSimulation(i));
            Tuple2oo<Simulator<Long,Long,Long>,DataFilter<Long,Long,Long>> pair = simSel.select(simReader, i, Long.MAX_VALUE, testGraph);
            Simulator<Long,Long,Long> sim = pair.v1();
            DataFilter<Long,Long,Long> filter = pair.v2();
            
            Data<Long,Long,Long> filteredData = filter.filter(data);
            for(int j = 0; j < numReps; ++j)
            {
                timea = System.currentTimeMillis();
                sim.initialize(filteredData);
                timeb = System.currentTimeMillis();
                System.out.println("Simulation " + j + " initialized (" + (timeb-timea) + " ms.)");
                Simulation<Long,Long,Long> simulation = sim.simulate(backupFile);
                // Write the simulation
                SimulationWriter<Long,Long,Long> simwriter = new BinarySimulationWriter<>();
                simwriter.initialize(output + i + "-" + rec + "-" + j + ".txt");
                simwriter.writeSimulation(simulation);
                simwriter.close();  

                timeb = System.currentTimeMillis();
                System.out.println("Conf: " + i + ": Simulation " + j + "finished (" + (timeb-timea) + " ms.)");
            }
            
            System.out.println("Conf. " + i + " finished.");
        }
        
        System.out.println("Finished");
    }
}
