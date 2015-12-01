
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.ENERGY_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;


public class HostMeasurement extends Measurement {

    private Host host;

    /**
     * This creates a host measurement.
     *
     * @param host The host the measurement is for
     */
    public HostMeasurement(Host host) {
        this.host = host;
    }

    /**
     * This creates a host measurement.
     *
     * @param host The host the measurement is for
     * @param clock The time when the measurement was taken, this is in unix time. 
     * i.e. Calendar.
     */
    public HostMeasurement(Host host, long clock) {
        this.host = host;
        setClock(clock);
    }

    /**
     * The gets the host that the measurement is for.
     * @return The host that the measurement is for.
     */
    public Host getHost() {
        return host;
    }

    /**
     * The sets the host that the measurement is for.
     * @param host The host that the measurement is for.
     */
    public void setHost(Host host) {
        this.host = host;
    }

    @Override
    public String toString() {
        return host.toString() + " Time: " + getClock() + " Metric Count: " + getMetricCount() + " Clock Diff: " + getMaximumClockDifference();
    }

    /**
     * This provides rapid access to power values from a host measurement.
     *
     * @return The power consumed when the measurement was taken.
     */
    public double getPower() {
        return this.getMetric(POWER_KPI_NAME).getValue();
    }

    /**
     * This provides rapid access to energy value from a host measurement.
     *
     * @return The energy consumed when the measurement was taken, going back to
     * an unspecified period of time. To be used like a meter reading that you
     * might give to an energy company.
     */
    public double getEnergy() {
        return this.getMetric(ENERGY_KPI_NAME).getValue();
    }

}
