/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.graphhopper.jsprit.core.algorithm.ruin;


import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListeners;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Random;

public abstract class AbstractRuinStrategy implements RuinStrategy {

    private final static Logger logger = LoggerFactory.getLogger(AbstractRuinStrategy.class);

    private RuinListeners ruinListeners;

    protected Random random = RandomNumberGeneration.getRandom();

    protected VehicleRoutingProblem vrp;

    public void setRandom(Random random) {
        this.random = random;
    }

    protected RuinShareFactory ruinShareFactory;

    public void setRuinShareFactory(RuinShareFactory ruinShareFactory) {
        this.ruinShareFactory = ruinShareFactory;
    }

    public RuinShareFactory getRuinShareFactory() {
        return ruinShareFactory;
    }

    protected AbstractRuinStrategy(VehicleRoutingProblem vrp) {
        this.vrp = vrp;
        ruinListeners = new RuinListeners();
    }

    @Override
    public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes) {
        ruinListeners.ruinStarts(vehicleRoutes);
        Collection<Job> unassigned = ruinRoutes(vehicleRoutes);
        logger.trace("ruin: [ruined={}]", unassigned.size());
        ruinListeners.ruinEnds(vehicleRoutes, unassigned);
        return unassigned;
    }

    public abstract Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes);

    @Override
    public void addListener(RuinListener ruinListener) {
        ruinListeners.addListener(ruinListener);
    }

    @Override
    public void removeListener(RuinListener ruinListener) {
        ruinListeners.removeListener(ruinListener);
    }

    @Override
    public Collection<RuinListener> getListeners() {
        return ruinListeners.getListeners();
    }

    protected boolean removeJob(Job job, Collection<VehicleRoute> vehicleRoutes) {
        if (jobIsInitial(job)) return false;
        for (VehicleRoute route : vehicleRoutes) {
            if (removeJob(job, route)) {
                return true;
            }
        }
        return false;
    }

    private boolean jobIsInitial(Job job) {
        return !vrp.getJobs().containsKey(job.getId()); //for initial jobs (being not contained in problem
    }

    protected boolean removeJob(Job job, VehicleRoute route) {
        if (jobIsInitial(job)) return false;
        boolean removed = route.getTourActivities().removeJob(job);
        if (removed) {
            logger.trace("ruin: {}", job.getId());
            ruinListeners.removed(job, route);
            return true;
        }
        return false;
    }
}
