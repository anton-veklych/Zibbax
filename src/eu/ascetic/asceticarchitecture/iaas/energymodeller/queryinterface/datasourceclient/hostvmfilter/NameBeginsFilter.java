
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class NameBeginsFilter implements ZabbixHostVMFilter {

    public static final String CONFIG_FILE = "filter.properties";

    private String begins = "testnode";
    private boolean isHost = true;

    /**
     * This creates a name filter that checks to see if the start of a host name
     * matches particular criteria or not. if it does then it will indicate accordingly
     * that the Zabbix host is a Energy modeller host or not.
     */
    public NameBeginsFilter() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            begins = config.getString("iaas.energy.modeller.filter.begins", begins);
            config.setProperty("iaas.energy.modeller.filter.begins", begins);
            isHost = config.getBoolean("iaas.energy.modeller.filter.isHost", isHost);
            config.setProperty("iaas.energy.modeller.filter.isHost", isHost);
        } catch (ConfigurationException ex) {
            Logger.getLogger(NameBeginsFilter.class.getName()).log(Level.INFO, "Error loading the configuration of the name begins filter");
        }
    }

    @Override
    public boolean isHost(Host host) {
        if (isHost) { //Testing by giving a common name to hosts
            return (host.getHost().startsWith(begins));
        } else { //testing for by providing a common name to VMs
            return (!host.getHost().startsWith(begins));
        }
    }

}
