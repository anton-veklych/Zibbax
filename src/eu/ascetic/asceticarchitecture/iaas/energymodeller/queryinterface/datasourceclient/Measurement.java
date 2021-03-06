
package eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient;

import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_IDLE_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_INTERUPT_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_IO_WAIT_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_NICE_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_SOFT_IRQ_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_SPOT_USAGE_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_STEAL_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_SYSTEM_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.CPU_USER_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.MEMORY_AVAILABLE_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.MEMORY_TOTAL_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.NETWORK_IN_STARTS_WITH_KPI_NAME;
import static eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.KpiList.NETWORK_OUT_STARTS_WITH_KPI_NAME;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public abstract class Measurement {

    private long clock;
    private HashMap<String, MetricValue> metrics = new HashMap<>();

    /**
     * This looks at the metrics gained, for this given gathering of measurement
     * values and compares the earliest and latest timestamps to determine how
     * long it took to get all the data values.
     *
     * @return The difference between the oldest piece of data recorded and the
     * newest.
     */
    public long getMaximumClockDifference() {
        long lowest = Integer.MAX_VALUE;
        long highest = Integer.MIN_VALUE;
        for (MetricValue entry : metrics.values()) {
            long current = entry.getClock();
            if (current < lowest) {
                lowest = current;
            }
            if (current > highest) {
                highest = current;
            }
        }
        return highest - lowest;
    }

    /**
     * This looks at the metrics gained and compares two named metrics and
     * observes the difference in time between the two readings.
     *
     * @param metricName The first metric value to compare
     * @param metricName2 The second metric value to compare
     * @return The difference in seconds between the two metric values.
     */
    public long getClockDifference(String metricName, String metricName2) {
        MetricValue value1 = metrics.get(metricName);
        MetricValue value2 = metrics.get(metricName2);
        if (value1 == null || value2 == null) {
            return 0;
        }
        long first = value1.getClock();
        long second = value2.getClock();
        if (second > first) {
            return second - first;
        } else {
            return first - second;
        }
    }

    /**
     * This looks at the metrics gained and compares a named metric and a
     * specified time and indicates if the values are close enough together.
     *
     * @param metricName The first metric value to compare
     * @param time The time to compare the metric to
     * @return The difference in seconds between the metric's clock value and
     * the time specified in Unix time.
     */
    public long getClockDifference(String metricName, long time) {
        MetricValue value1 = metrics.get(metricName);
        if (value1 == null) {
            return 0;
        }
        long first = value1.getClock();
        if (time > first) {
            return time - first;
        } else {
            return first - time;
        }
    }

    /**
     * This tests to see if for this measurement record that two metrics have
     * clock values that are within an acceptable tolerance bound.
     *
     * @param metricName The first metric value to compare
     * @param metricName2 The second metric value to compare
     * @param tolerance The amount of seconds gap allowed between measurements,
     * for the record to be valid for the required analysis.
     * @return If this measurement record is valid for performing analysis on
     * based upon the difference between two metrics clock values.
     */
    public boolean isContemporary(String metricName, String metricName2, int tolerance) {
        return getClockDifference(metricName, metricName2) <= tolerance;
    }

    /**
     * This tests to see if for this measurement record to see if a metric has a
     * clock value that is within an acceptable tolerance bound.
     *
     * @param metricName The metric value to compare
     * @param time The time to compare the metric to
     * @param tolerance The amount of seconds gap allowed between the
     * measurement and the specified time for the record to be valid for the
     * required analysis.
     * @return If this measurement record is valid for performing analysis on
     * based upon the difference between a metric's clock value and a specified
     * time.
     */
    public boolean isContemporary(String metricName, long time, int tolerance) {
        return getClockDifference(metricName, time) <= tolerance;
    }

    /**
     * This returns the maximum delay that any metric encountered.
     *
     * @return The highest delay encountered by any metric, in this set of
     * measurement data.
     */
    public long getMaxDelay() {
        long delay = Integer.MIN_VALUE;
        for (MetricValue entry : metrics.values()) {
            long current = Long.parseLong(entry.getDelay());
            if (current > delay) {
                delay = current;
            }
        }
        return delay;
    }

    /**
     * This returns the minimum delay that any metric encountered.
     *
     * @return The lowest delay encountered by any metric, in this set of
     * measurement data.
     */
    public long getMinDelay() {
        long delay = Integer.MAX_VALUE;
        for (MetricValue entry : metrics.values()) {
            long current = Long.parseLong(entry.getDelay());
            if (current < delay) {
                delay = current;
            }
        }
        return delay;
    }

    /**
     * This lists a set of names for the metrics that are available in this
     * measurement
     *
     * @return The list of all metrics names that were measured.
     */
    public Set<String> getMetricNameList() {
        return metrics.keySet();
    }

    /**
     * This lists the metrics that are available in this measurement
     *
     * @return the list of all metrics and the key value that is used to quickly
     * identify a metric.
     */
    public HashMap<String, MetricValue> getMetrics() {
        return metrics;
    }

    /**
     * This gets the count of how many values for different metrics are stored.
     *
     * @return The count of how many values for different metrics are stored.
     */
    public int getMetricCount() {
        return metrics.size();
    }

    /**
     * This returns the set of items/metric values for the measurement.
     *
     * @return The metric values for this measurement.
     */
    public Collection<MetricValue> getItems() {
        return metrics.values();
    }

    /**
     * This sets the set of metrics and their keys for the measurement.
     *
     * @param metrics the metrics to set
     */
    public void setMetrics(HashMap<String, MetricValue> metrics) {
        this.metrics = metrics;
    }

    /**
     * This adds a metric and value to a measurement
     *
     * @param item a metric to add to this measurement dataset
     */
    public void addMetric(MetricValue item) {
        metrics.put(item.getKey(), item);
    }

    /**
     * This gets the item that represents a given metric
     *
     * @param key The key that is used to identify a given measurement
     * @return The metric and its value that is identified by the key.
     */
    public MetricValue getMetric(String key) {
        return metrics.get(key);
    }

    /**
     * This provides the time of the measurement in Unix time.
     *
     * @return The time the measurement was taken
     */
    public long getClock() {
        return clock;
    }

    /**
     * This sets the time of the measurement in Unix time.
     *
     * @param clock The time the measurement was taken
     */
    public void setClock(long clock) {
        this.clock = clock;
    }

    /**
     * This provides rapid access to cpu utilisation values from a measurement.
     *
     * @return The cpu utilisation when the measurement was taken. Values in
     * range 0...1
     */
    public double getCpuUtilisation() {
        if (metrics.containsKey(CPU_SPOT_USAGE_KPI_NAME)) {
            return this.getMetric(CPU_SPOT_USAGE_KPI_NAME).getValue() / 100;
        }
        double interrupt = 0.0;
        double iowait = 0.0;
        double nice = 0.0;
        double softirq = 0.0;
        double steal = 0.0;
        double system = 0.0;
        double user = 0.0;
        if (metrics.containsKey(CPU_SYSTEM_KPI_NAME)) {
            system = this.getMetric(CPU_SYSTEM_KPI_NAME).getValue();
        }
        if (metrics.containsKey(CPU_USER_KPI_NAME)) {
            user = this.getMetric(CPU_USER_KPI_NAME).getValue();
        }
        if (metrics.containsKey(CPU_INTERUPT_KPI_NAME)) {
            interrupt = this.getMetric(CPU_INTERUPT_KPI_NAME).getValue();
        }
        if (metrics.containsKey(CPU_IO_WAIT_KPI_NAME)) {
            iowait = this.getMetric(CPU_IO_WAIT_KPI_NAME).getValue();
        }
        if (metrics.containsKey(CPU_NICE_KPI_NAME)) {
            nice = this.getMetric(CPU_NICE_KPI_NAME).getValue();
        }
        if (metrics.containsKey(CPU_SOFT_IRQ_KPI_NAME)) {
            softirq = this.getMetric(CPU_SOFT_IRQ_KPI_NAME).getValue();
        }
        if (metrics.containsKey(CPU_STEAL_KPI_NAME)) {
            steal = this.getMetric(CPU_STEAL_KPI_NAME).getValue();
        }
        return (system + user + interrupt + iowait + nice + softirq + steal) / 100;
    }

    /**
     * This provides rapid access to cpu utilisation metrics timestamp.
     *
     * @return The cpu utilisation's time stamp data.
     */
    public long getCpuUtilisationTimeStamp() {
        if (metrics.containsKey(CPU_SPOT_USAGE_KPI_NAME)) {
            return this.getMetric(CPU_SPOT_USAGE_KPI_NAME).getClock();
        }
        if (metrics.containsKey(CPU_SYSTEM_KPI_NAME)) {
            return this.getMetric(CPU_SYSTEM_KPI_NAME).getClock();
        }
        if (metrics.containsKey(CPU_USER_KPI_NAME)) {
            return this.getMetric(CPU_USER_KPI_NAME).getClock();
        }
        if (metrics.containsKey(CPU_INTERUPT_KPI_NAME)) {
            return this.getMetric(CPU_INTERUPT_KPI_NAME).getClock();
        }
        if (metrics.containsKey(CPU_IO_WAIT_KPI_NAME)) {
            return this.getMetric(CPU_IO_WAIT_KPI_NAME).getClock();
        }
        if (metrics.containsKey(CPU_NICE_KPI_NAME)) {
            return this.getMetric(CPU_NICE_KPI_NAME).getClock();
        }
        if (metrics.containsKey(CPU_SOFT_IRQ_KPI_NAME)) {
            return this.getMetric(CPU_SOFT_IRQ_KPI_NAME).getClock();
        }
        if (metrics.containsKey(CPU_STEAL_KPI_NAME)) {
            return this.getMetric(CPU_STEAL_KPI_NAME).getClock();
        }
        return 0;
    }

    /**
     * This provides rapid access to cpu utilisation values from a measurement.
     *
     * @return The cpu utilisation when the measurement was taken. Values in
     * range 0...1
     */
    public double getCpuIdle() {
        if (metrics.containsKey(CPU_SPOT_USAGE_KPI_NAME)) {
            return 1 - this.getMetric(CPU_SPOT_USAGE_KPI_NAME).getClock();
        }        
        return this.getMetric(CPU_IDLE_KPI_NAME).getValue() / 100;
    }

    /**
     * This provides rapid access to memory values for a measurement.
     *
     * @return The total memory available when the measurement was taken. Values
     * given in Mb
     */
    public double getMemoryAvailable() {
        //Original value given in bytes. 1024 * 1024 = 1048576
        return this.getMetric(MEMORY_AVAILABLE_KPI_NAME).getValue() / 1048576;
    }

    /**
     * This provides rapid access to memory values for a measurement.
     *
     * @return The total memory used when the measurement was taken. Values
     * given in Mb
     */
    public double getMemoryUsed() {
        return getMemoryTotal() - getMemoryAvailable();
    }

    /**
     * This provides rapid access to memory values for a measurement.
     *
     * @return The total memory available when the measurement was taken. Values
     * given in Mb
     */
    public double getMemoryTotal() {
        //Original value given in bytes. 1024 * 1024 = 1048576
        return this.getMetric(MEMORY_TOTAL_KPI_NAME).getValue() / 1048576;
    }

    /**
     * This provides information on the network activity of the host at the time
     * of measurement.
     *
     * @return The amount of data transfered in, units are in bits/second.
     */
    public double getNetworkIn() {
        double answer = 0.0;
        for (Map.Entry<String, MetricValue> metric : metrics.entrySet()) {
            if (metric.getKey().startsWith(NETWORK_IN_STARTS_WITH_KPI_NAME)) {
                answer = answer + metric.getValue().getValue();
            }
        }
        return answer;
    }

    /**
     * This provides information on the network activity of the host at the time
     * of measurement.
     *
     * @return The amount of data transfered out, units are in bits/second.
     */
    public double getNetworkOut() {
        double answer = 0.0;
        for (Map.Entry<String, MetricValue> metric : metrics.entrySet()) {
            if (metric.getKey().startsWith(NETWORK_OUT_STARTS_WITH_KPI_NAME)) {
                answer = answer + metric.getValue().getValue();
            }
        }
        return answer;
    }

}
