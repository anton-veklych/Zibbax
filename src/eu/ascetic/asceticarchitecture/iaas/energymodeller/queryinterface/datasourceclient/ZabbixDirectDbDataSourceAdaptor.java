
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.datastore.MySqlDatabaseConnector;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.BOOT_TIME_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_COUNT_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_IDLE_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_SPOT_USAGE_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.DISK_TOTAL_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.MEMORY_TOTAL_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.POWER_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.VM_PHYSICAL_HOST_NAME;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.EnergyUsageSource;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;


public class ZabbixDirectDbDataSourceAdaptor extends MySqlDatabaseConnector implements HostDataSource {

    /**
     * Get max item id values for history items select itemid, max(clock) from
     * history group by itemid;
     *
     * Get all history item names for a named host/s
     *
     * select itemid, items.name from hosts, items where hosts.hostid =
     * items.hostid and hosts.host = "testgrid3"; select itemid, items.name from
     * hosts, items where hosts.hostid = items.hostid and hosts.hostid = 10084;
     * select itemid, items.name from hosts, items where hosts.hostid =
     * items.hostid and hosts.hostid IN (10084, 10105, 10106);
     *
     * SELECT history.itemid, clock, value FROM history INNER JOIN ( select
     * itemid, max(clock) AS mostrecent from history group by itemid) ms ON
     * history.itemid = ms.itemid AND clock = mostrecent WHERE history.itemid IN
     * (select itemid from hosts, items where hosts.hostid = items.hostid and
     * hosts.hostid = 10084);
     */
    private Connection connection;
    /**
     * This query lists all hosts data items.
     * status <> 3 excludes templates
     * available = 0 is for templates, available = 1 = true available = 2 = false
     */
    private static final String ALL_ZABBIX_HOSTS = "SELECT hostid, host FROM hosts WHERE status <> 3 AND available = 1";
    /**
     * This query searches for a named host and provides it's current latest
     * items.
     *
     * The order of the ? is as follows: table, table, hostid
     *
     * It returns the item id, clock, item name, item key and item value.
     */
    private static final String QUERY_DATA_BY_ID = "SELECT h.itemid, h.clock, i.name, i.key_, h.value "
            + "FROM items i, XXXX h, "
            + "(SELECT hs.itemid, max(hs.clock) AS mostrecent FROM XXXX hs GROUP BY hs.itemid) ms "
            + "WHERE h.itemid = ms.itemid AND "
            + "h.clock = mostrecent AND "
            + "h.itemid = i.itemid AND "
            + "h.itemid IN (SELECT it.itemid FROM hosts, items it "
            + "WHERE hosts.hostid = it.hostid AND "
            + "hosts.hostid = ?)";

    /**
     * This query searches for a named double valued history item for a given
     * host between a range of specified times.
     *
     * The order of the ? is as follows: hostid, item name, clock start, clock
     * end
     */
    private static final String HISTORY_QUERY = "SELECT h.itemid, h.clock, h.value "
            + "FROM history h "
            + "WHERE h.clock >= ? AND "
            + "h.clock <= ? AND "
            + "h.itemid = ("
            + "SELECT it.itemid "
            + "FROM hosts, items it "
            + "WHERE hosts.hostid = it.hostid AND "
            + "hosts.hostid = ? AND "
            + "it.key_ = ?)";
    private static final HashSet<String> HISTORY_TABLES = new HashSet<>();
    /**
     * The url to contact the database.
     */
    private static String databaseURL = "jdbc:mysql://10.10.0.1/zabbix";
    /**
     * The driver to be used to contact the database.
     */
    private static String databaseDriver = "com.mysql.jdbc.Driver";
    /**
     * The user details to contact the database.
     */
    private static String databaseUser = "zabbixinfo";
    /**
     * The user's password to contact the database.
     */
    private static String databasePassword = "readonly";
    /**
     * The filter string, if a host/VM begins with this then it is a host, if
     * isHost equals true.
     */
    private static String begins = "testnode";
    private static boolean isHost = true;
    private static final String CONFIG_FILE = "zabbix_db_adaptor.properties";
    private static final Logger DB_LOGGER = Logger.getLogger(ZabbixDirectDbDataSourceAdaptor.class.getName());

    /**
     * This creates a new database connector for use. It establishes a database
     * connection immediately ready for use.
     */
    public ZabbixDirectDbDataSourceAdaptor() {
        HISTORY_TABLES.add("history");
        HISTORY_TABLES.add("history_str");
        HISTORY_TABLES.add("history_uint");
        HISTORY_TABLES.add("history_text");

        try {
            PropertiesConfiguration config;
            if (new File(CONFIG_FILE).exists()) {
                config = new PropertiesConfiguration(CONFIG_FILE);
            } else {
                config = new PropertiesConfiguration();
                config.setFile(new File(CONFIG_FILE));
            }
            config.setAutoSave(true); //This will save the configuration file back to disk. In case the defaults need setting.
            databaseURL = config.getString("iaas.energy.modeller.zabbix.db.url", databaseURL);
            config.setProperty("iaas.energy.modeller.zabbix.db.url", databaseURL);
            databaseDriver = config.getString("iaas.energy.modeller.zabbix.db.driver", databaseDriver);
            config.setProperty("iaas.energy.modeller.zabbix.db.driver", databaseDriver);
            databasePassword = config.getString("iaas.energy.modeller.zabbix.db.password", databasePassword);
            config.setProperty("iaas.energy.modeller.zabbix.db.password", databasePassword);
            databaseUser = config.getString("iaas.energy.modeller.zabbix.db.user", databaseUser);
            config.setProperty("iaas.energy.modeller.zabbix.db.user", databaseUser);
            begins = config.getString("iaas.energy.modeller.filter.begins", begins);
            config.setProperty("iaas.energy.modeller.filter.begins", begins);
            isHost = config.getBoolean("iaas.energy.modeller.filter.isHost", isHost);
            config.setProperty("iaas.energy.modeller.filter.isHost", isHost);

        } catch (ConfigurationException ex) {
            DB_LOGGER.log(Level.SEVERE, "Error loading the configuration of the IaaS energy modeller");
        }
        try {
            connection = getConnection();
        } catch (IOException | SQLException | ClassNotFoundException ex) {
            DB_LOGGER.log(Level.SEVERE, "Failed to establish the connection to the Zabbix DB", ex);
        }
    }

    /**
     * Establishes a connection to the database.
     *
     * @return Connection object representing the connection
     * @throws IOException if properties file cannot be accessed
     * @throws SQLException if connection fails
     * @throws ClassNotFoundException if the database driver class is not found
     */
    @Override
    protected final Connection getConnection() throws IOException, SQLException, ClassNotFoundException {
        // Define JDBC driver
        System.setProperty("jdbc.drivers", databaseDriver);
        //Ensure that the driver has been loaded
        Class.forName(databaseDriver);
        return DriverManager.getConnection(databaseURL,
                databaseUser, databasePassword);
    }

    @Override
    public Host getHostByName(String hostname) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                ALL_ZABBIX_HOSTS + " AND name = ?")) {
            preparedStatement.setString(1, hostname);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
                for (ArrayList<Object> hostData : results) {
                    Host answer = new Host(((Long) hostData.get(0)).intValue(), (String) hostData.get(1));
                    answer = fullyDescribeHost(answer, getHostData(answer).getMetrics().values());
                    if (isHost((String) hostData.get(1))) {
                        return answer;
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException ex) {
            DB_LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This indicates if a hostname belongs to a host or to a VM or not.
     *
     * @param hostname The hostname
     * @return If the host belongs to a Host or a VM.
     */
    public boolean isHost(String hostname) {
        if (isHost) { //Testing by giving a common name to hosts
            return (hostname.startsWith(begins));
        } else { //testing for by providing a common name to VMs
            return (!hostname.startsWith(begins));
        }
    }

    @Override
    public VmDeployed getVmByName(String name) {
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                ALL_ZABBIX_HOSTS + " AND name = ?")) {
            preparedStatement.setString(1, name);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
                for (ArrayList<Object> hostData : results) {
                    VmDeployed answer = new VmDeployed(((Long) hostData.get(0)).intValue(), (String) hostData.get(1));
                    answer = fullyDescribeVM(answer, getVmData(answer).getMetrics().values());
                    if (!isHost((String) hostData.get(1))) {
                        return answer;
                    } else {
                        return null;
                    }
                }
            }
        } catch (SQLException ex) {
            DB_LOGGER.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /**
     * This gathers more information about a host than is available by querying
     * the host list table directly.
     *
     * @param host The host to populate with more information
     * @param items The data items to enrich the host with
     * @return The host object with more information attached.
     */
    private Host fullyDescribeHost(Host host, Collection<MetricValue> items) {
        for (MetricValue item : items) {
            if (item.getKey().equals(MEMORY_TOTAL_KPI_NAME)) { //Convert to Mb
                //Original value given in bytes. 1024 * 1024 = 1048576
                host.setRamMb((int) (item.getValue() / 1048576));
            }
            if (item.getKey().equals(DISK_TOTAL_KPI_NAME)) { //Convert to Mb            
                //Original value given in bytes. 1024 * 1024 * 1024 = 1073741824
                host.setDiskGb((item.getValue() / 1073741824));
            }
        }
        return host;
    }

    /**
     * This gathers more information about a vm than is available by querying
     * the host list table directly.
     *
     * @param vm The vm to populate with more information
     * @param items The data for a given vm.
     * @return The vm object with more information attached.
     */
    private VmDeployed fullyDescribeVM(VmDeployed vm, Collection<MetricValue> items) {
        for (MetricValue item : items) {
            if (item.getKey().equals(MEMORY_TOTAL_KPI_NAME)) { //Convert to Mb
                //Original value given in bytes. 1024 * 1024 = 1048576
                vm.setRamMb((int) (Double.valueOf(item.getValue()) / 1048576));
            }
            if (item.getKey().equals(DISK_TOTAL_KPI_NAME)) { //covert to Gb
                //Original value given in bytes. 1024 * 1024 * 1024 = 1073741824
                vm.setDiskGb((Double.valueOf(item.getValue()) / 1073741824));
            }
            if (item.getKey().equals(BOOT_TIME_KPI_NAME)) {
                Calendar cal = new GregorianCalendar();
                //This converts from milliseconds into the correct time value
                cal.setTimeInMillis(TimeUnit.SECONDS.toMillis(Long.valueOf(item.getValueAsString())));
                vm.setCreated(cal);
            }
            if (item.getKey().equals(VM_PHYSICAL_HOST_NAME)) {
                vm.setAllocatedTo(getHostByName(item.getValueAsString()));
            }
            if (item.getKey().equals(CPU_COUNT_KPI_NAME)) {
                vm.setCpus(Integer.valueOf(item.getValueAsString()));
            }
            //TODO set the information correctly below!
            vm.setIpAddress("127.0.0.1");
            vm.setState("Work in Progress");

        }
        //A fall back incase the information is not available!
        if (vm.getCpus() == 0) {
            vm.setCpus(Integer.valueOf(1));
        }
        return vm;
    }

    @Override
    public List<Host> getHostList() {
        List<Host> answer = new ArrayList<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(ALL_ZABBIX_HOSTS);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
            for (ArrayList<Object> hostData : results) {
                if (isHost((String) hostData.get(1))) {
                    Host host = new Host(((Long) hostData.get(0)).intValue(), (String) hostData.get(1));
                    host = fullyDescribeHost(host, getHostData(host).getMetrics().values());
                    answer.add(host);
                }
            }
        } catch (SQLException ex) {
            DB_LOGGER.log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    @Override
    public List<EnergyUsageSource> getHostAndVmList() {
        List<EnergyUsageSource> answer = new ArrayList<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(ALL_ZABBIX_HOSTS);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
            for (ArrayList<Object> hostData : results) {
                if (isHost((String) hostData.get(1))) {
                    Host host = new Host(((Long) hostData.get(0)).intValue(), (String) hostData.get(1));
                    host = fullyDescribeHost(host, getHostData(host).getMetrics().values());
                    answer.add(host);
                } else {
                    VmDeployed vm = new VmDeployed(((Long) hostData.get(0)).intValue(), (String) hostData.get(1));
                    vm = fullyDescribeVM(vm, getVmData(vm).getMetrics().values());
                    answer.add(vm);
                }
            }
        } catch (SQLException ex) {
            DB_LOGGER.log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    @Override
    public List<VmDeployed> getVmList() {
        List<VmDeployed> answer = new ArrayList<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(ALL_ZABBIX_HOSTS);
                ResultSet resultSet = preparedStatement.executeQuery()) {
            ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
            for (ArrayList<Object> hostData : results) {
                if (!isHost((String) hostData.get(1))) {
                    VmDeployed vm = new VmDeployed(((Long) hostData.get(0)).intValue(), (String) hostData.get(1));
                    vm = fullyDescribeVM(vm, getVmData(vm).getMetrics().values());
                    answer.add(vm);
                }
            }
        } catch (SQLException ex) {
            DB_LOGGER.log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    @Override
    public HostMeasurement getHostData(Host host) {
        HostMeasurement answer = new HostMeasurement(host);
        long clock = 0;
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        for (String historyTable : HISTORY_TABLES) {
            String query = QUERY_DATA_BY_ID.replace("XXXX", historyTable);
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, host.getId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
                    for (ArrayList<Object> dataItem : results) {
                        if ((int) dataItem.get(1) > clock) {
                            clock = (int) dataItem.get(1);
                            answer.setClock(clock);
                        }
                        //itemid | clock | name | key_ | value   
                        MetricValue value = new MetricValue(
                                (String) dataItem.get(2),
                                (String) dataItem.get(3),
                                dataItem.get(4) + "",
                                (Integer) dataItem.get(1));
                        answer.addMetric(value);
                    }
                }
            } catch (SQLException ex) {
                DB_LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return answer;
    }

    @Override
    public List<HostMeasurement> getHostData() {
        List<HostMeasurement> answer = new ArrayList<>();
        for (Host host : getHostList()) {
            answer.add(getHostData(host));
        }
        return answer;
    }

    @Override
    public List<HostMeasurement> getHostData(List<Host> hostList) {
        List<HostMeasurement> answer = new ArrayList<>();
        for (Host host : hostList) {
            answer.add(getHostData(host));
        }
        return answer;
    }

    @Override
    public VmMeasurement getVmData(VmDeployed vm) {
        VmMeasurement answer = new VmMeasurement(vm);
        long clock = 0;
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        for (String historyTable : HISTORY_TABLES) {
            String query = QUERY_DATA_BY_ID.replace("XXXX", historyTable);
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setInt(1, vm.getId());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
                    for (ArrayList<Object> dataItem : results) {
                        if ((int) dataItem.get(1) > clock) {
                            clock = (int) dataItem.get(1);
                            answer.setClock(clock);
                        }
                        //itemid | clock | name | key_ | value  
                        MetricValue value = new MetricValue(
                                (String) dataItem.get(2), //name
                                (String) dataItem.get(3), //key
                                dataItem.get(4) + "",//value
                                (Integer) dataItem.get(1)); //clock
                        answer.addMetric(value);
                    }
                }
            } catch (SQLException ex) {
                DB_LOGGER.log(Level.SEVERE, null, ex);
            }
        }
        return answer;
    }

    @Override
    public List<VmMeasurement> getVmData() {
        List<VmMeasurement> answer = new ArrayList<>();
        for (VmDeployed vm : getVmList()) {
            answer.add(getVmData(vm));
        }
        return answer;
    }

    @Override
    public List<VmMeasurement> getVmData(List<VmDeployed> vmList) {
        List<VmMeasurement> answer = new ArrayList<>();
        for (VmDeployed host : vmList) {
            answer.add(getVmData(host));
        }
        return answer;
    }

    @Override
    public double getLowestHostPowerUsage(Host host) {
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(new GregorianCalendar().getTimeInMillis());
        long timeInPast = currentTime - TimeUnit.MINUTES.toSeconds(10);
        //NOTE: The semantics do not match the other Zabbix Datasource adaptor
        List<Double> energyData = getHistoryDataItems(POWER_KPI_NAME, host.getId(), timeInPast, currentTime);
        double lowestValue = Double.MAX_VALUE;
        for (Double current : energyData) {
            if (current < lowestValue) {
                lowestValue = current;
            }
        }
        return lowestValue;
    }

    @Override
    public double getHighestHostPowerUsage(Host host) {
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(new GregorianCalendar().getTimeInMillis());
        long timeInPast = currentTime - TimeUnit.MINUTES.toSeconds(10);
        //NOTE: The semantics do not match the other Zabbix Datasource adaptor
        List<Double> energyData = getHistoryDataItems(POWER_KPI_NAME, host.getId(), timeInPast, currentTime);
        double highestValue = Double.MIN_VALUE;
        for (Double current : energyData) {
            if (current > highestValue) {
                highestValue = current;
            }
        }
        return highestValue;
    }

    @Override
    public double getCpuUtilisation(Host host, int durationSeconds) {
        long currentTime = TimeUnit.MILLISECONDS.toSeconds(new GregorianCalendar().getTimeInMillis());
        long timeInPast = currentTime - durationSeconds;
        List<Double> spotCpuData = getHistoryDataItems(CPU_SPOT_USAGE_KPI_NAME, host.getId(), timeInPast, currentTime);
        if (spotCpuData != null && !spotCpuData.isEmpty()) {
            double usage = removeNaN(sumArray(spotCpuData) / ((double) spotCpuData.size()));
            return usage / 100;
        }
        List<Double> idleData = getHistoryDataItems(CPU_IDLE_KPI_NAME, host.getId(), timeInPast, currentTime);
        double idle = removeNaN(sumArray(idleData) / ((double) idleData.size()));
        return 1 - ((idle) / 100);
    }

    /**
     * In the case that no data is provided a NaN value will be given, this
     * needs to be stopped. It occurs when the getCpuUtilisation method is given
     * too small a time window for gathering CPU utilisation data.
     */
    private double removeNaN(double number) {
        if (Double.isNaN(number)) {
            return 0.0;
        } else {
            return number;
        }
    }

    /**
     * This query returns a set of history items for querying.
     *
     * @param key The key of the data item to return
     * @param hostId The host id that the data is associated with
     * @param startTime The start time of the search
     * @param endTime The end time of the search
     * @return The list of double values one for each data item point.
     */
    private List<Double> getHistoryDataItems(String key, int hostId, long startTime, long endTime) {
        List<Double> answer = new ArrayList<>();
        connection = getConnection(connection);
        if (connection == null) {
            return null;
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement(HISTORY_QUERY)) {
            //hostid, item name, clock start, clock end
            preparedStatement.setLong(1, startTime);
            preparedStatement.setLong(2, endTime);
            preparedStatement.setInt(3, hostId);
            preparedStatement.setString(4, key);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                ArrayList<ArrayList<Object>> results = resultSetToArray(resultSet);
                for (ArrayList<Object> hostData : results) {
                    Double value = ((Double) hostData.get(2));
                    answer.add(value);
                }
            }
        } catch (SQLException ex) {
            DB_LOGGER.log(Level.SEVERE, null, ex);
        }
        return answer;
    }

    /**
     * This function sums a list of numbers for historic data.
     *
     * @param list The ArrayList to calculate the values of.
     * @return The sum of the values.
     */
    private double sumArray(List<Double> list) {
        double answer = 0.0;
        for (Double current : list) {
            answer = answer + current.doubleValue();
        }
        return answer;
    }
}
