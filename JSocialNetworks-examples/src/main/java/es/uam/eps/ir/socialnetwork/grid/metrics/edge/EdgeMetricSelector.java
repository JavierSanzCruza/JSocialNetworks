/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.uam.eps.ir.socialnetwork.grid.metrics.edge;

import es.uam.eps.ir.socialnetwork.grid.Grid;
import static es.uam.eps.ir.socialnetwork.grid.metrics.edge.EdgeMetricIdentifiers.*;
import es.uam.eps.ir.socialnetwork.metrics.EdgeMetric;
import es.uam.eps.ir.socialnetwork.metrics.GraphMetric;
import es.uam.eps.ir.socialnetwork.metrics.distance.DistanceCalculator;
import es.uam.eps.ir.socialnetwork.metrics.graph.aggregate.AggregateEdgeMetric;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;



/**
 * Class that translates from a grid to the different contact recommendation algorithns.
 * @author Javier Sanz-Cruzado Puig
 * @param <U> Type of the users
 */
public class EdgeMetricSelector<U>
{
    /**
     * Obtains the grid configurator for a metric.
     * @param metric the name of the metric.
     * @return the grid configurator for the metric if it exists, null otherwise.
     */
    public EdgeMetricGridSearch<U> getGridSearch(String metric)
    {
        EdgeMetricGridSearch<U> gridsearch;
        switch(metric)
        {
            case BETWEENNESS:
                gridsearch = new EdgeBetweennessGridSearch();
                break;
            case EMBEDEDNESS:
                gridsearch = new EmbedednessGridSearch();
                break;
            case WEIGHT:
                gridsearch = new EdgeWeightGridSearch();
                break;
            case FOAF:
                gridsearch = new FOAFGridSearch();
                break;
            // Default behavior
            default:
                gridsearch = null;
        }
        
        return gridsearch;
    }
    
    /**
     * Obtains the different variants of the available algorithms using the different
     * parameters in the grid.
     * @param metric the name of the metric.
     * @param grid the parameter grid.
     * @param distCalc a distance calculator.
     * @return a map containing the different metric suppliers.
     */
    public Map<String, Supplier<EdgeMetric<U>>> getMetrics(String metric, Grid grid, DistanceCalculator<U> distCalc)
    {
        EdgeMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
                
        if(gridsearch != null)
            return gridsearch.grid(grid, distCalc);
        return new HashMap<>();
    }
    
    /**
     * Obtains the different variants of a given edge metric depending on the 
     * parameters selected in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @return a map containing the different metric suppliers.
     */
    public Map<String, Function<DistanceCalculator<U>,EdgeMetric<U>>> getMetrics(String metric, Grid grid)
    {
        EdgeMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
                
        if(gridsearch != null)
            return gridsearch.grid(grid);
        return new HashMap<>();
    }
    
    /**
     * Obtains the aggregate variants of a given edge metric, given the parameters selected
     * in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @param distCalc a distance calculator.
     * @return a map, indexed by metric name, containing the different variants of the metric selected in the grid.
     */
    public Map<String, Supplier<GraphMetric<U>>> getGraphMetrics(String metric, Grid grid, DistanceCalculator<U> distCalc)
    {
        EdgeMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
        {
            Map<String, Supplier<EdgeMetric<U>>> metrics = gridsearch.grid(grid, distCalc);
            Map<String, Supplier<GraphMetric<U>>> graphMetrics = new HashMap<>();
            metrics.entrySet().stream().forEach(entry -> 
            {
                String name = entry.getKey();
                graphMetrics.put(name, () -> new AggregateEdgeMetric<>(entry.getValue().get()));
            });
            return graphMetrics;
        }
        return new HashMap<>();
    }
    
    /**
     * Obtains the aggregate variants of a given edge metric, given the parameters selected
     * in a grid.
     * @param metric the name of the metric.
     * @param grid the grid containing the different parameters.
     * @return a map, indexed by metric name, containing the different variants of the metric selected in the grid.
     */
    public Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> getGraphMetrics(String metric, Grid grid)
    {
        EdgeMetricGridSearch<U> gridsearch = this.getGridSearch(metric);
        if(gridsearch != null)
        {
            Map<String, Function<DistanceCalculator<U>,EdgeMetric<U>>> metrics = gridsearch.grid(grid);
            Map<String, Function<DistanceCalculator<U>,GraphMetric<U>>> graphMetrics = new HashMap<>();
            metrics.entrySet().stream().forEach(entry -> 
            {
                String name = entry.getKey();
                graphMetrics.put(name, (distCalc) -> new AggregateEdgeMetric<>(entry.getValue().apply(distCalc)));
            });
            return graphMetrics;
        }
        return new HashMap<>();
    }
}
