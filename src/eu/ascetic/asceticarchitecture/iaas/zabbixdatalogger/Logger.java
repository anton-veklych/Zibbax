
package eu.ascetic.asceticarchitecture.iaas.zabbixdatalogger;

import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.queryinterface.datasourceclient.ZabbixDirectDbDataSourceAdaptor;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.Host;
import eu.ascetic.asceticarchitecture.iaas.energymodeller.types.energyuser.VmDeployed;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.logging.Level;

/**
 * This application logs out the raw data that is received by the energy
 * modeller from the Zabbix API.
 */
public class Logger {

    private static boolean running = true;

    public static void main(String[] args) throws IOException {

        if (args.length == 0) {
            System.out.println("Please provide as the first argument the name of "
                    + "the host or virtual machine to monitor, such as asok09.");
            System.exit(0);
        }
        String hostname = args[0];
        HashSet<String> strArgs = new HashSet<>();
        strArgs.addAll(Arrays.asList(args));
        MeasurementLogger logger = new MeasurementLogger(new File("Dataset_" + hostname + ".txt"), false);
        new Thread(logger).start();
        if (!(strArgs.contains("silent") || strArgs.contains("s"))) {
            System.out.println("This application will run continually until the word "
                    + "'quit' is written.)");
            System.out.println("It is currently logging data out for: " + hostname);
            System.out.println("This is being output to the file: Dataset_" + hostname + ".txt");
            QuitWatcher quitWatcher = new QuitWatcher();
            new Thread(quitWatcher).start();
        }
        /**
         * Change this line here in order to switch to the web interface version!
         */
        ZabbixDirectDbDataSourceAdaptor adaptor = new ZabbixDirectDbDataSourceAdaptor();
//        ZabbixDataSourceAdaptor adaptor = new ZabbixDataSourceAdaptor();
        Host host = adaptor.getHostByName(hostname);
        VmDeployed vm = null;
        if (host == null) {
            vm = adaptor.getVmByName(hostname);
        }
        while (running) {
            if (host != null) {
                logger.printToFile(adaptor.getHostData(host));
            } else if (vm != null) {
                logger.printToFile(adaptor.getVmData(vm));
            } else {
                running = false;
                java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.INFO, "The resource named was not found");
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                java.util.logging.Logger.getLogger(Logger.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        logger.stop();
    }

    /**
     * This looks for input from the console so that the application can be told
     * when to quit.
     */
    private static class QuitWatcher implements Runnable {

        @Override
        public void run() {
            while (running) {
                Scanner scanner = new Scanner(System.in);
                String cmd = scanner.hasNext() ? scanner.next() : null;
                if (cmd != null && cmd.equals("quit")) {
                    running = false;
                }
            }
        }

    }
}
