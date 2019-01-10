package es.uam.eps.ir.socialnetwork.community.graph;

import es.uam.eps.ir.socialnetwork.graph.Graph;
import es.uam.eps.ir.socialnetwork.community.Communities;
import es.uam.eps.ir.socialnetwork.graph.generator.EmptyMultiGraphGenerator;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorBadConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.generator.exception.GeneratorNotConfiguredException;
import es.uam.eps.ir.socialnetwork.graph.multigraph.MultiGraph;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates a multi-graph which contains all communities as nodes and all links between
 * different communities as edges.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> type of the nodes
 */
public class InterCommunityGraphGenerator<U> implements CommunityGraphGenerator<U>
{
    
    @Override
    public MultiGraph<Integer> generate(Graph<U> graph, Communities<U> communities)
    {
        try {
            EmptyMultiGraphGenerator<Integer> gg = new EmptyMultiGraphGenerator<>();
            
            boolean directed = graph.isDirected();
            boolean weighted = false;
            
            gg.configure(directed, weighted);
            MultiGraph<Integer> commGraph = (MultiGraph<Integer>) gg.generate();
            
            communities.getCommunities().forEach((comm)->{
                commGraph.addNode(comm);
            });
            
            if(directed)
            {
                graph.getAllNodes().forEach((orig) -> {
                    graph.getAdjacentNodes(orig).forEach((dest)->{
                        int origComm = communities.getCommunity(orig);
                        int destComm = communities.getCommunity(dest);
                        if(origComm != destComm)
                            commGraph.addEdge(origComm, destComm);
                    });
                });
            }
            else
            {
                List<U> visited = new ArrayList<>();
                graph.getAllNodes().forEach((orig) -> {
                    graph.getAdjacentNodes(orig).forEach((dest)->{
                        if(!visited.contains(dest))
                        {
                            int origComm = communities.getCommunity(orig);
                            int destComm = communities.getCommunity(dest);
                            if(origComm != destComm)
                                commGraph.addEdge(origComm, destComm);
                        }
                    });
                    visited.add(orig);
                });
                
            }
            
            return commGraph;
        } catch (GeneratorNotConfiguredException | GeneratorBadConfiguredException ex) {
            return null;
        }
    }
}
