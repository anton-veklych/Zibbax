
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.hostvmfilter;

import eu.ascetic.asceticarchitecture.iaas.zabbixApi.datamodel.Host;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class NamedList implements ZabbixHostVMFilter {

    public static final String CONFIG_FILE = "filter.properties";

    private String namedSet = "testnode1,testnode2,testnode3,testnode4,testnode5,testnode6,testnode7";
    private HashSet<String> hostNames = new HashSet<>();

    /**
     * This creates a name filter that checks to see if the start of a host name
     * matches particular criteria or not. if it does then it will indicate
     * accordingly that the Zabbix host is a Energy modeller host or not.
     */
    public NamedList() {
        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            namedSet = config.getString("iaas.energy.modeller.filter.names", namedSet);
            config.setProperty("iaas.energy.modeller.filter.names", namedSet);
            hostNames.addAll(Arrays.asList(namedSet.split(",")));
        } catch (ConfigurationException ex) {
            Logger.getLogger(NameBeginsFilter.class.getName()).log(Level.INFO, "Error loading the configuration of the named list filter");
        }
    }

    @Override
    public boolean isHost(Host host) {
        return hostNames.contains(host.getHost());
    }

}
