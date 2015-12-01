
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;


public interface ZabbixHostVMFilter {
    
    /**
     * Indicates if the Zabbix Host, is a Energy modeller host or not. The only
     * other option is for it to be a Energy modeller Virtual machine.
     * @param host The Zabbix host.
     * @return If it is a host or not.
     */
    public boolean isHost(Host host);
    
}
