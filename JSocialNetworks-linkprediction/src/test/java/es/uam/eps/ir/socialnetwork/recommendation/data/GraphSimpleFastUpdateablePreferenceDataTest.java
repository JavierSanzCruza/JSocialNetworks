/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package es.uam.eps.ir.socialnetwork.recommendation.data;

import es.uam.eps.ir.socialnetwork.graph.edges.EdgeType;
import es.uam.eps.ir.socialnetwork.graph.fast.FastDirectedWeightedGraph;
import es.uam.eps.ir.socialnetwork.graph.fast.FastGraph;
import es.uam.eps.ir.socialnetwork.recommendation.data.updateable.GraphSimpleUpdateableFastPreferenceData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import java.util.stream.IntStream;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import org.ranksys.core.index.fast.updateable.FastUpdateableItemIndex;
import org.ranksys.core.index.fast.updateable.FastUpdateableUserIndex;
import org.ranksys.core.index.fast.updateable.SimpleFastUpdateableItemIndex;
import org.ranksys.core.index.fast.updateable.SimpleFastUpdateableUserIndex;
import org.ranksys.core.preference.IdPref;
import org.ranksys.core.preference.fast.IdxPref;

/**
 * Class for testing the simple fast updateable preference data.
 * @author Javier Sanz-Cruzado Puig (javier.sanz-cruzado@uam.es)
 */
public class GraphSimpleFastUpdateablePreferenceDataTest 
{
    @Test
    public void test()
    {


        FastGraph<String> graph = new FastDirectedWeightedGraph<>();
        Random rnd = new Random();
        // Number of users.
        int N = rnd.nextInt(5000);
                
        // Generate the list of users
        List<String> users = IntStream.range(0, N).mapToObj(Integer::toString).collect(toList());
        Collections.shuffle(users);
        
        // Generate the user and item index (both are the same).
        FastUpdateableUserIndex<String> uIndex = SimpleFastUpdateableUserIndex.load(users.stream());
        FastUpdateableItemIndex<String> iIndex = SimpleFastUpdateableItemIndex.load(users.stream());
        
        // Generate the list of links (preferences)
        List<Tuple3<String,String,Double>> prefs = new ArrayList<>();
        int numPref = users.stream().mapToInt(u -> 
        {
            int K = rnd.nextInt(Math.min(500, N));
            Set<Integer> set = new HashSet<>();
            rnd.ints(K,0,N).forEach(k -> 
            {
                int aux = k;
                while(set.contains(aux))
                {
                    aux = (aux+1)%N;
                }
                set.add(aux);
                prefs.add(new Tuple3<>(u, users.get(aux),5*rnd.nextDouble()));
            });
            
            return K;
        }).sum();
        
        // Add the users and links to the graph.
        users.forEach(u -> graph.addNode(u));
        prefs.forEach(pref -> graph.addEdge(pref.v1, pref.v2, pref.v3, EdgeType.getDefaultValue(), false));
        
        // Load the preference data.
        GraphSimpleUpdateableFastPreferenceData<String> prefData = GraphSimpleUpdateableFastPreferenceData.load(graph);
        assertEquals(prefData.numUsers(), N);
        assertEquals(prefData.numItems(), N);
        assertEquals(prefData.numPreferences(), numPref);
        
        // Check the existence of links
        rnd.ints(1000, 0, numPref).forEach(i -> 
        {
            String user = prefs.get(i).v1;
            String item = prefs.get(i).v2;
            double val = prefs.get(i).v3;
            
            Optional<IdxPref> optional = prefData.getPreference(uIndex.user2uidx(user), iIndex.item2iidx(item));
            if(optional.isPresent())
            {
                assertEquals(val, optional.get().v2,0.00001);
            }
            else
            {
                assertFalse(true);
            }
            
        });
        
        List<Tuple3<String,String,Double>> extraPrefs = new ArrayList<>();
        // Add some preferences.
        int numExtraPref = users.stream().mapToInt(u -> 
        {
            int K = rnd.nextInt(Math.min(100, N - prefData.numItems(u)));
            Set<Integer> set = prefData.getUidxPreferences(prefData.user2uidx(u)).map(pref -> pref.v1).collect(Collectors.toCollection(HashSet::new));
            rnd.ints(K,0,N).forEach(k -> 
            {
                int aux = k;
                while(set.contains(aux))
                {
                    aux = (aux+1)%N;
                }
                set.add(aux);
                extraPrefs.add(new Tuple3<>(u, users.get(aux),5*rnd.nextDouble()));
            });
            
            return K;
        }).sum();
        
        prefData.update(extraPrefs.stream());

        assertEquals(prefData.numPreferences(), numPref + numExtraPref);
        rnd.ints(1000, 0, numExtraPref).forEach(i -> 
        {
            String user = extraPrefs.get(i).v1;
            String item = extraPrefs.get(i).v2;
            double val = extraPrefs.get(i).v3;

            Optional<IdxPref> optional = prefData.getPreference(uIndex.user2uidx(user), iIndex.item2iidx(item));
            if(optional.isPresent())
            {
                assertEquals(val, optional.get().v2,0.00001);
            }
            else
            {
                assertFalse(true);
            }

        });
               
        
        List<Tuple3<String,String,Double>> falsePrefs = new ArrayList<>();
        // Generate false preferences (not included items).
        int falsePref = users.stream().mapToInt(u -> 
        {
            int K = rnd.nextInt(Math.min(50, N));
            Set<Integer> set = new HashSet<>();
            rnd.ints(K,0,200).forEach(k -> 
            {
                int aux = k;
                while(set.contains(aux))
                {
                    aux = (aux+1)%200;
                }
                set.add(aux);
                falsePrefs.add(new Tuple3<>(u, Integer.toString(aux+N),5*rnd.nextDouble()));
            });
            
            return K;
        }).sum();
        
        // Generate false preferences (not included users).
        falsePref += IntStream.range(N,N+200).map(u -> 
        {
            int K = rnd.nextInt(Math.min(50, N));
            Set<Integer> set = new HashSet<>();
            rnd.ints(K,0,N).forEach(k -> 
            {
                int aux = k;
                while(set.contains(aux))
                {
                    aux = (aux+1)%N;
                }
                set.add(aux);
                falsePrefs.add(new Tuple3<>(Integer.toString(u), Integer.toString(aux),5*rnd.nextDouble()));
            });
            
            return K;
        }).sum();
        
        prefData.update(falsePrefs.stream());
        assertEquals(prefData.numUsers(), N);
        assertEquals(prefData.numItems(), N);
        assertEquals(prefData.numPreferences(), numPref + numExtraPref);
        assertEquals(uIndex.numUsers(), N);
        assertEquals(iIndex.numItems(), N);
        
        List<String> extraUsers = IntStream.range(N, N+200).mapToObj(Integer::toString).collect(toList());
        Collections.shuffle(extraUsers);
        List<String> extraItems = IntStream.range(N, N+200).mapToObj(Integer::toString).collect(toList());
        Collections.shuffle(extraItems);
        
        extraUsers.forEach(u -> prefData.updateAddUser(u));
        extraItems.forEach(i -> prefData.updateAddItem(i));
        assertEquals(prefData.numUsers(), N+200);
        assertEquals(prefData.numItems(), N+200);
        
        falsePrefs.forEach(t -> prefData.update(t.v1,t.v2,t.v3));
        prefData.update(falsePrefs.stream());

        assertEquals(prefData.numPreferences(), numPref + numExtraPref + falsePref);
               
        List<Tuple2<String,String>> prefsToDelete = new ArrayList<>();
        
        // Delete some ratings
        int numDeleted = users.stream().mapToInt(u -> 
        {
            int K = rnd.nextInt(Math.min(20, prefData.numItems(u)));
            List<IdPref<String>> ls = new ArrayList<>();
            prefData.getUserPreferences(u).forEach(pref -> ls.add(pref));
            Collections.shuffle(ls);
            
            ls.subList(0, K).forEach(pref -> prefsToDelete.add(new Tuple2<>(u, pref.v1)));
            return K;
        }).sum();

        assertEquals(numDeleted, prefsToDelete.size());
        prefData.updateDelete(prefsToDelete.stream());
        assertEquals(prefData.numUsers(), N+200);
        assertEquals(prefData.numItems(), N+200);
        assertEquals(prefData.numPreferences(), numPref+numExtraPref+falsePref-numDeleted);
        rnd.ints(1000, 0, numDeleted).forEach(i -> 
        {
            String user = prefsToDelete.get(i).v1;
            String item = prefsToDelete.get(i).v2;
            
            Optional<IdxPref> optional = prefData.getPreference(uIndex.user2uidx(user), iIndex.item2iidx(item));
            assertFalse(optional.isPresent());
        });
    }
}
