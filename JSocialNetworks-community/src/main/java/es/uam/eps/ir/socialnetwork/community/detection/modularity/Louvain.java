/*
 * Copyright (C) 2017 Information Retrieval Group at Universidad Aut�noma
 * de Madrid, http://ir.ii.uam.es
 * 
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package es.uam.eps.ir.socialnetwork.community.detection.modularity;

import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.community.detection.CommunityDetectionAlgorithm;
import es.uam.eps.ir.socialnetwork.community.graph.SimpleCommunityGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.graph.edges.EdgeWeight;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.utils.datatypes.Pair;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.gephi.appearance.api.AppearanceController;
import org.gephi.appearance.api.AppearanceModel;
import org.gephi.appearance.api.Function;
import org.gephi.appearance.api.Partition;
import org.gephi.appearance.api.PartitionFunction;
import org.gephi.appearance.plugin.PartitionElementColorTransformer;
import org.gephi.graph.api.Column;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.GraphController;

import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.project.api.ProjectController;
import org.gephi.statistics.plugin.Modularity;
import org.openide.util.Lookup;


/**
 * Class for computing the Louvain community detection algorithm. This class uses the Gephi implementation of this algorithm (gephi-toolkit 0.9.2).
 * 
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 * @author Sofia Marina Pepa
 * 
 * Basic version: Blondel, V., Guillaume, J., Lambiotte, R., Lefebvre, E. Fast unfolding of communities in large networks. Journal of Statistical Mechanics 10 (2008)
 * Incremental version: He, J., Chen, D. A fast algorithm for community detection in temporal network. Physica A 429 (2015)
 * 
 * @param <U> Type of the users.
 */
public class Louvain<U extends Serializable> implements CommunityDetectionAlgorithm<U> 
{
    @Override
    public Communities<U> detectCommunities(Graph<U> graph) 
    {
        Communities<U> communities = new Communities<>();
        // First, initialize a project and get a workspace
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.newProject();
        
        // Get all the necessary controllers and models
        GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getGraphModel();
        AppearanceController appearanceController = Lookup.getDefault().lookup(AppearanceController.class);
        AppearanceModel appearanceModel = appearanceController.getModel();
        
        // Load the graph into the Gephi workspace
        Map<U, Node> nodes = new HashMap<>();
        Map<String,U> nodeIds = new HashMap<>();
             
        // Get the graph as undirected
        graph.getAllNodes().forEach(u -> 
        {
            Node node = graphModel.factory().newNode(u.toString());
            node.setLabel(u.toString());
            graphModel.getUndirectedGraph().addNode(node);
            nodes.put(u, node);
            nodeIds.put(u.toString(), u);
        });
        
        Set<U> neighbors = new HashSet<>();
        graph.getAllNodes().forEach(u -> 
        {
            Node uNode = nodes.get(u);
            graph.getNeighbourNodes(u).forEach(v -> 
            {
                if(!neighbors.contains(v))
                {
                    Node vNode = nodes.get(v);
                    if(graph.isDirected())
                    {
                        double a = graph.getEdgeWeight(u, v);
                        double b = graph.getEdgeWeight(v, u);
                        Double weight = 0.0;
                        if(!EdgeWeight.isErrorValue(a))
                        {
                            weight += a;
                        }
                        if(!EdgeWeight.isErrorValue(b))
                        {
                            weight += b;
                        }
                        
                        Edge e = graphModel.factory().newEdge(uNode, vNode, 1, weight, false);
                        graphModel.getUndirectedGraph().addEdge(e);
                    }
                    else
                    {
                        Double a = graph.getEdgeWeight(u,v);
                        Edge e = graphModel.factory().newEdge(uNode, vNode, 1, a, false);
                        graphModel.getUndirectedGraph().addEdge(e);
                    }
                }
            });
            neighbors.add(u);
        });
        
        // Compute the modularity (and the partition using the Louvain algorithm)
        Modularity mod = new Modularity();
        mod.setRandom(true);
        mod.execute(graphModel);
        
        // Get the column for the node table including the communities data.
        Column modColumn = graphModel.getNodeTable().getColumn(Modularity.MODULARITY_CLASS);
        Function function = appearanceModel.getNodeFunction(graphModel.getGraph(), modColumn, PartitionElementColorTransformer.class);
        Partition partition = ((PartitionFunction) function).getPartition();

        // Create the partition
        int numPartitions = partition.size();
        for(int i = 0; i < numPartitions; ++i)
        {
            communities.addCommunity();
        }
        
        graphModel.getUndirectedGraph().getNodes().forEach(node -> 
        {
            String uName = node.getLabel();
            U u = nodeIds.get(uName);
            int comm = (int) node.getAttribute(modColumn);
            communities.add(u, comm);
        });
        
        return communities;
    }
    
    
    @Override
    public Communities<U> detectCommunities(Graph<U> graph, List<Pair<U>> newLinks, List<Pair<U>> disapLinks, Communities<U> previous)
    {
        try 
        {
            // First, we detect nodes with new links or links which have disappeared.
            Set<U> newLinkNodes = new HashSet<>();
            
            // Add nodes with new links
            newLinks.forEach(p ->
            { 
                newLinkNodes.add(p.v1());
                newLinkNodes.add(p.v2());
            });
            
            // Add nodes with disappeared links
            disapLinks.forEach(p ->
            {
                newLinkNodes.add(p.v1());
                newLinkNodes.add(p.v2());
            });
            
            // First, we build a new community partition, where each of the users
            // with newly added or removed links for its own community. The rest of users
            // remain in the same communities.           
            Communities<U> comms = new Communities<>();
            for(U user : newLinkNodes)
            {
                comms.addCommunity();
                comms.add(user, comms.getNumCommunities()-1);
            }
            
            previous.getCommunities().forEach(comm ->
            {
                int i = comms.getNumCommunities();
                Set<U> cUsers = previous.getUsers(comm).filter(u -> !newLinkNodes.contains(u)).collect(Collectors.toSet());
                if(cUsers != null && !cUsers.isEmpty())
                {
                    comms.addCommunity();
                    cUsers.forEach(u -> comms.add(u, i));
                }
            });
            
            // Generate a small graph: each community is a node, and links between communities have
            // weight equal to the sum of weights between communities.
            SimpleCommunityGraphGenerator<U> ggen = new SimpleCommunityGraphGenerator<>();
            ggen.configure(graph, comms, false);
            Graph<Integer> smallGraph = ggen.generate();
            
            // Find the Louvain communities for the small graph
            Louvain<Integer> louvain = new Louvain<>();
            Communities<Integer> smallgraphComms = louvain.detectCommunities(smallGraph);
            
            // Determine the new communities:
            Communities<U> defComms = new Communities<>();
            
            IntStream.range(0, smallgraphComms.getNumCommunities()).forEach(c -> 
            {
                defComms.addCommunity();
                smallgraphComms.getUsers(c).forEach(userComm -> 
                {
                    comms.getUsers(userComm).forEach(u -> 
                    {
                        defComms.add(u, c);
                    });
                });

            });
            
            return defComms;
        }
        catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) // In case this fails.
        {
            return null;
        }
    }
}
