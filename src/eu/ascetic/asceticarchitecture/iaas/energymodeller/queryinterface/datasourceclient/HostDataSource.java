
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import java.util.List;


public interface HostDataSource {

    /**
     * This returns a host given its unique name.
     *
     * @param hostname The name of the host to get.
     * @return The object representation of a host in the energy modeller.
     */
    public Host getHostByName(String hostname);

    /**
     * This returns a host given its unique name.
     *
     * @param name The name of the host to get.
     * @return The object representation of a host in the energy modeller.
     */
    public VmDeployed getVmByName(String name);

    /**
     * This provides a list of hosts for the energy modeller
     *
     * @return A list of hosts for the energy modeller.
     */
    public List<Host> getHostList();

    /**
     * This provides a list of hosts and VMs for the energy modeller
     * @return A list of energy users for the energy modeller.
     */
    public List<EnergyUsageSource> getHostAndVmList();
    
    /**
     * This provides a list of VMs for the energy modeller
     *
     * @return A list of vms for the energy modeller.
     */
    public List<VmDeployed> getVmList();

    /**
     * This provides for the named host all the information that is available.
     *
     * @param host The host to get the measurement data for.
     * @return The host measurement data
     */
    public HostMeasurement getHostData(Host host);

    /**
     * This lists for all host all the metric data on them.
     *
     * @return A list of host measurements
     */
    public List<HostMeasurement> getHostData();

    /**
     * This takes a list of hosts and provides all the metric data on them.
     *
     * @param hostList The list of hosts to get the data from
     * @return A list of host measurements
     */
    public List<HostMeasurement> getHostData(List<Host> hostList);

    /**
     * This provides for the named vm all the information that is available.
     *
     * @param vm The vm to get the measurement data for.
     * @return The vm measurement data
     */
    public VmMeasurement getVmData(VmDeployed vm);

    /**
     * This lists for all vms all the metric data on them.
     *
     * @return A list of vm measurements
     */
    public List<VmMeasurement> getVmData();

    /**
     * This takes a list of vms and provides all the metric data on them.
     *
     * @param vmList The list of vms to get the data from
     * @return A list of vm measurements
     */
    public List<VmMeasurement> getVmData(List<VmDeployed> vmList);

    /**
     * This finds the lowest/resting power usage by a client.
     *
     * @param host The host to get the lowest power usage data for.
     * @return The lowest i.e. resting power usage of a host
     */
    public double getLowestHostPowerUsage(Host host);

    /**
     * This finds the highest power usage by a host.
     *
     * @param host The host to get the highest power usage data for.
     * @return The highest power usage of a host
     */
    public double getHighestHostPowerUsage(eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host);
    
    /**
     * This finds the cpu utilisation of a host, over the last n minutes.
     * @param host The host to get the cpu utilisation data for.
     * @param durationSeconds The amount of seconds to get the data for
     * @return The average utilisation of the host.
     */
    public double getCpuUtilisation(eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host host, int durationSeconds);
    
}
