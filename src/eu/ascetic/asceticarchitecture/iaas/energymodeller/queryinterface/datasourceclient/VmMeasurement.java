
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;


public class VmMeasurement extends Measurement {

    private VmDeployed vm;

    /**
     * This creates a vm measurement.
     *
     * @param vm The vm the measurement is for
     */
    public VmMeasurement(VmDeployed vm) {
        this.vm = vm;
    }

    /**
     * This creates a vm measurement.
     *
     * @param vm The vm the measurement is for
     * @param clock The time when the measurement was taken
     */
    public VmMeasurement(VmDeployed vm, long clock) {
        this.vm = vm;
        setClock(clock);
    }

    /**
     * This gets the VM that this measurement is for
     * @return The deployed Vm that this measurement is for
     */
    public VmDeployed getVm() {
        return vm;
    }

    /**
     * This sets the VM that this measurement is for
     * @param vm The deployed Vm that this measurement is for
     */
    public void setVm(VmDeployed vm) {
        this.vm = vm;
    }

    @Override
    public String toString() {
        return vm.toString() + " Time: " + getClock() + " Metric Count: " + getMetricCount() + " Clock Diff: " + getMaximumClockDifference();
    }

}
