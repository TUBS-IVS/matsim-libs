package com.graphhopper.jsprit.core.algorithm.box;

import java.util.List;
import java.util.Random;
import java.util.HashSet;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the iteration-scoped perturbation contract used by native Freight. */
public class InsertionNoiseCompatibilityTest {
    @Test
    public void activatesOncePerIterationAndLeavesConstructionUnperturbed() {
        var random = new ScriptedRandom(.1, .25, .75, .9);
        var noise = new InsertionNoiseMaker(null, 100, .2, .5);
        noise.setRandom(random);
        assertEquals(0, noise.getCosts(null, null, null, null, 0));
        assertEquals(0, random.calls);
        noise.informIterationStarts(1, null, List.of());
        assertEquals(5, noise.getCosts(null, null, null, null, 0));
        assertEquals(15, noise.getCosts(null, null, null, null, 0));
        assertEquals(3, random.calls);
        noise.informIterationStarts(2, null, List.of());
        assertEquals(0, noise.getCosts(null, null, null, null, 0));
        assertEquals(0, noise.getCosts(null, null, null, null, 0));
        assertEquals(4, random.calls);
    }

    @Test
    public void zeroProbabilityNeverAddsCosts() {
        var random = new ScriptedRandom(0, .9);
        var noise = new InsertionNoiseMaker(null, 100, .2, 0);
        noise.setRandom(random);
        for (int iteration=1; iteration<=2; iteration++) {
            noise.informIterationStarts(iteration, null, List.of());
            assertEquals(0, noise.getCosts(null,null,null,null,0));
        }
        assertEquals(2, random.calls);
    }

    @Test
    public void fullProbabilityAddsCostsOnlyAfterIterationStarts() {
        var noise = new InsertionNoiseMaker(null, 100, .2, 1);
        noise.setRandom(new ScriptedRandom(.99,.25,.99,.75));
        assertEquals(0, noise.getCosts(null,null,null,null,0));
        noise.informIterationStarts(1,null,List.of());
        assertEquals(5, noise.getCosts(null,null,null,null,0));
        noise.informIterationStarts(2,null,List.of());
        assertEquals(15, noise.getCosts(null,null,null,null,0));
    }

    @Test
    public void serialAndConcurrentSearchRetainAllJobsWithinCapacity() {
        for (int threads : new int[]{1,2}) {
            var type=VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,2).setCostPerDistance(1).build();
            var vehicle=VehicleImpl.Builder.newInstance("vehicle").setType(type).setStartLocation(Location.newInstance(0,0)).build();
            var builder=VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle);
            for(int i=1;i<=4;i++) builder.addJob(Service.Builder.newInstance("job"+i).addSizeDimension(0,1).setLocation(Location.newInstance(i,0)).build());
            var algorithm=Jsprit.Builder.newInstance(builder.build()).setRandom(new Random(4711)).setProperty(Jsprit.Parameter.THREADS,String.valueOf(threads)).buildAlgorithm();
            algorithm.setMaxIterations(20);
            var solution=Solutions.bestOf(algorithm.searchSolutions());
            assertTrue(solution.getUnassignedJobs().isEmpty());
            assertTrue(Double.isFinite(solution.getCost()) && solution.getCost()>0);
            var jobs=new HashSet<String>();
            for(var route:solution.getRoutes()) {
                int demand=0;
                for(var activity:route.getActivities()) if(activity instanceof TourActivity.JobActivity job) {
                    assertTrue(jobs.add(job.getJob().getId()));
                    demand+=job.getJob().getSize().get(0);
                }
                assertTrue(demand<=2);
            }
            assertEquals(4,jobs.size());
        }
    }

    static final class ScriptedRandom extends Random {
        final double[] values;
        int calls;
        ScriptedRandom(double... values) { this.values = values; }
        @Override public double nextDouble() { return values[calls++]; }
    }

    public static void main(String[] args) {
        new InsertionNoiseCompatibilityTest().activatesOncePerIterationAndLeavesConstructionUnperturbed();
        System.out.println("ITERATION_NOISE_CONTRACT_PASSED");
    }
}
