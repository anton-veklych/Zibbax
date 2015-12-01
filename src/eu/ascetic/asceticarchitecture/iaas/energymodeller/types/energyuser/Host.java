
package eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser;

import java.util.Objects;


public class Host extends EnergyUsageSource implements Comparable<Host> {

    private int id = -1;
    private String hostName = "";
    private boolean available = true;
    private int ramMb;
    private double diskGb;

    /**
     * E_i^0: is the "idle power consumption" in Watts (with zero number of VMs
     * running) E_i^c: power consumption of a CPU cycle (or instruction)
     *
     * This value acts as the default idle power consumption for a host and is
     * used when no calibration data is available.
     *
     */
    private double defaultIdlePowerConsumption = 0.0; //i.e. 27.1w for an idle laptop.
    private int defaultIdleRamUsage = 0;


    /**
     * This creates a new instance of a host
     *
     * @param id The host id
     * @param hostName The host name
     */
    public Host(int id, String hostName) {
        this.id = id;
        this.hostName = hostName;
    }

    /**
     * This returns the host's id.
     *
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * This sets the host's id.
     *
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * This returns the host's name.
     *
     * @return the hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * This sets the hosts name.
     *
     * @param hostName the hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * This indicates if the host is currently available.
     *
     * @return the available
     */
    public boolean isAvailable() {
        return available;
    }

    /**
     * This sets the flag to state the host is available.
     *
     * @param available the available to set
     */
    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "HostID: " + id + " Host Name: " + hostName + " Available :" + available;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Host) {
            Host host = (Host) obj;
            if (hostName != null && host.getHostName() != null) {
                return this.hostName.equals(host.getHostName());
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.hostName);
        return hash;
    }

    @Override
    public int compareTo(Host o) {
        return this.getHostName().compareTo(o.getHostName());
    }

    /**
     * This returns the value for the default idle power consumption of a host.
     * This value is recorded and used in cases where calibration has yet to
     * occur.
     *
     * @return the defaultIdlePowerConsumption
     */
    public double getDefaultIdlePowerConsumption() {
        return defaultIdlePowerConsumption;
    }

    /**
     * This sets the value for the default idle power consumption of a host.
     * This value is recorded and used in cases where calibration has yet to
     * occur.
     *
     * @param defaultIdlePowerConsumption the default Idle Power Consumption to
     * set
     */
    public void setDefaultIdlePowerConsumption(double defaultIdlePowerConsumption) {
        this.defaultIdlePowerConsumption = defaultIdlePowerConsumption;
    }

    /**
     * This provides the amount of memory that is used without a VM been placed
     * on the host machine.
     *
     * @return the idleRamUsage The amount of ram used when the host is idle.
     */
    public int getDefaultIdleRamUsage() {
        return defaultIdleRamUsage;
    }

    /**
     * This sets the amount of memory that is used without a VM been placed on
     * the host machine.
     *
     * @param defaultIdleRamUsage The amount of ram used when the host is idle.
     */
    public void setDefaultIdleRamUsage(int defaultIdleRamUsage) {
        this.defaultIdleRamUsage = defaultIdleRamUsage;
    }

    /**
     * This gets the maximum amount of ram this host has.
     *
     * @return The ram this host has physically available.
     */
    public int getRamMb() {
        return ramMb;
    }

    /**
     * This sets the maximum amount of ram this host has.
     *
     * @param ramMb The ram this host has physically available.
     */
    public void setRamMb(int ramMb) {
        if (ramMb < 0) {
            throw new IllegalArgumentException("The amount of memory must not be less than zero.");
        }
        this.ramMb = ramMb;
    }

    /**
     * This gets the amount of disk space this host has available.
     *
     * @return The disk space this host has available.
     */
    public double getDiskGb() {
        return diskGb;
    }

    /**
     * This sets the amount of disk space this host has available.
     *
     * @param diskGb The disk space this host has available.
     */
    public void setDiskGb(double diskGb) {
        if (diskGb < 0) {
            throw new IllegalArgumentException("The amount of disk size must not be less than zero.");
        }
        this.diskGb = diskGb;
    }

}
