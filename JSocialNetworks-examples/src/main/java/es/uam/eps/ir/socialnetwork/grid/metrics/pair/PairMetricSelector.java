/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.pair;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.pair.PairMetricIdentifiers.*;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import es.uam.eps.ir.socialnetwork.metrics.PairMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.graph.aggregate.AggregatePairMetric;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Class that translates from a grid to the different contact recommendation algorithns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class PairMetricSelector<U>
{
     /**
     * Obtains the grid configurator for a metric.
     * @param metric the name of the metric.
     * @return the grid configurator for the metric if it exists, null otherwise.
     */
    public PairMetricGridSearch<U> getGridSearch(String metric)
    {
        PairMetricGridSearch<U> gridsearch;
        switch(metric)
        {
            case DISTANCE:
                gridsearch = new DistanceGridSearch();
                break;
            case GEODESICS:
                gridsearch = new GeodesicsGridSearch();
                break;
            case DISTANCEWITHOUTLINK:
                gridsearch = new DistanceWithoutLinkGridSearch();
                break;
            case EMBEDEDNESS:
                gridsearch = new EmbedednessGridSearch();
                break;
            case RECIP:
                gridsearch = new ReciprocityRateGridSearch();
                break;
            case RECIPROCALSPL:
                gridsearch = new ReciprocalShortestPathLengthGridSearch();
                break;
            // Default behavior
            default:
                gridsearch = null;
        }
        
        return gridsearch;
    }
    
        /**
     * Obtains the different variants of a given pair metric depending on the 
     * parameters selected in a grid.
     * @param name the name of the metric.
     * @param grid the grid containing the different parameters.
     * @param distcalc a distance calculator.
     * @return a map containing the different metric suppliers.
     */
    public Map<String, Supplier<PairMetric<U>>> getMetrics(String name, Grid grid, DistanceCalculator<U> distcalc)
    {
        PairMetricGridSearch<U> gridsearch = this.getGridSearch(name);
        
        if(gridsearch != null)
            return gridsearch.grid(grid, distcalc);
        return new HashMap<>();
    }
    
    /**
     * Obtains the different variants of a given pair metric depending on the 
     * parameters selected in a grid.
     * @param name the name of the metric.
     * @param grid the grid containing the different parameters.
     * @return a map containing the different metric suppliers, which work given a distance calculator
     */
    public Map<String, Function<DistanceCalculator<U>, PairMetric<U>>> getMetrics(String name, Grid grid)
    {
        PairMetricGridSearch<U> gridsearch = this.getGridSearch(name);
        
        if(gridsearch != null)
            return gridsearch.grid(grid);
        return new HashMap<>();
    }
    
    /**
     * Obtains the aggregate variants of a given pair metric, given the parameters selected
     * in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @param distCalc a distance calculator.
     * @return a map, indexed by metric name, containing the different variants of the metric selected in the grid.
     */
    public Map<String, Supplier<GraphMetric<U>>> getGraphMetrics(String metric, Grid grid, DistanceCalculator<U> distCalc)
    {
        PairMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
        {
            Map<String, Supplier<PairMetric<U>>> metrics = gridsearch.grid(grid, distCalc);
            Map<String, Supplier<GraphMetric<U>>> graphMetrics = new HashMap<>();
            metrics.entrySet().stream().forEach(entry -> 
            {
                String name = entry.getKey();
                graphMetrics.put(name, () -> new AggregatePairMetric<>(entry.getValue().get()));
            });
            return graphMetrics;
        }
        return new HashMap<>();
    }
    
    /**
     * Obtains the aggregate variants of a given pair metric, given the parameters selected
     * in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @return a map, indexed by metric name, containing the different variants of the metric selected in the grid.
     */
    public Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> getGraphMetrics(String metric, Grid grid)
    {
        PairMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
        {
            Map<String, Function<DistanceCalculator<U>,PairMetric<U>>> metrics = gridsearch.grid(grid);
            Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> graphMetrics = new HashMap<>();
            metrics.entrySet().stream().forEach(entry -> 
            {
                String name = entry.getKey();
                graphMetrics.put(name, (distCalc) -> new AggregatePairMetric<>(entry.getValue().apply(distCalc)));
            });
            return graphMetrics;
        }
        return new HashMap<>();
    }
}
