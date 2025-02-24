package org.matsim.contrib.drt.extension.eshifts.charging;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.optimizer.Request;
import org.matsim.contrib.dvrp.passenger.BusStopActivity;
import org.matsim.contrib.dvrp.passenger.PassengerHandler;
import org.matsim.contrib.dvrp.passenger.PassengerPickupActivity;
import org.matsim.contrib.dvrp.passenger.PassengerRequest;
import org.matsim.contrib.dynagent.DynActivity;
import org.matsim.contrib.dynagent.DynAgent;
import org.matsim.contrib.dynagent.FirstLastSimStepDynActivity;
import org.matsim.contrib.evrp.ChargingActivity;
import org.matsim.contrib.evrp.ChargingTask;
import org.matsim.contrib.drt.extension.shifts.schedule.ShiftBreakTask;
import org.matsim.core.mobsim.framework.MobsimPassengerAgent;

import java.util.Map;

/**
 * based on {@link BusStopActivity} and {@link ChargingActivity}
 * @author nkuehnel / MOIA
 */
public class ChargingBreakActivity extends FirstLastSimStepDynActivity implements DynActivity, PassengerPickupActivity {

    public static final String CHARGING_BREAK = "Charging Break";
    private final ChargingActivity chargingDelegate;
    private final Map<Id<Request>, ? extends PassengerRequest> dropoffRequests;
    private final Map<Id<Request>, ? extends PassengerRequest> pickupRequests;
    private final PassengerHandler passengerHandler;
    private final DynAgent driver;
	private final double endTime;

	private int passengersPickedUp = 0;

    public ChargingBreakActivity(ChargingTask chargingTask, PassengerHandler passengerHandler,
                                 DynAgent driver, ShiftBreakTask task,
                                 Map<Id<Request>, ? extends PassengerRequest> dropoffRequests,
                                 Map<Id<Request>, ? extends PassengerRequest> pickupRequests) {
        super(CHARGING_BREAK);
        chargingDelegate = new ChargingActivity(chargingTask);
        this.dropoffRequests = dropoffRequests;
        this.pickupRequests = pickupRequests;
        this.passengerHandler = passengerHandler;
        this.driver = driver;
		endTime = task.getEndTime();
	}

    @Override
    protected boolean isLastStep(double now) {
        if(chargingDelegate.getEndTime() < now && now >= endTime) {
            for (PassengerRequest request : pickupRequests.values()) {
                if (passengerHandler.tryPickUpPassenger(this, driver, request, now)) {
                    passengersPickedUp++;
                }
            }
            return passengersPickedUp == pickupRequests.size();
        }
        return false;
    }

    @Override
    public void finalizeAction(double now) {
    }

    @Override
    public void notifyPassengerIsReadyForDeparture(MobsimPassengerAgent passenger, double now) {
        if (!isLastStep(now)) {
            return;// pick up only at the end of stop activity
        }

        PassengerRequest request = getRequestForPassenger(passenger.getId());
        if (passengerHandler.tryPickUpPassenger(this, driver, request, now)) {
            passengersPickedUp++;
        } else {
            throw new IllegalStateException("The passenger is not on the link or not available for departure!");
        }
    }

    @Override
    protected void beforeFirstStep(double now) {
        // TODO probably we should simulate it more accurately (passenger by passenger, not all at once...)
        for (PassengerRequest request : dropoffRequests.values()) {
            passengerHandler.dropOffPassenger(driver, request, now);
        }
    }

    @Override
    protected void afterLastStep(double now) {

    }

    @Override
    protected void simStep(double now) {
        chargingDelegate.doSimStep(now);
    }

    private PassengerRequest getRequestForPassenger(Id<Person> passengerId) {
        return pickupRequests.values().stream()
                .filter(r -> passengerId.equals(r.getPassengerId()))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("I am waiting for different passengers!"));
    }
}
