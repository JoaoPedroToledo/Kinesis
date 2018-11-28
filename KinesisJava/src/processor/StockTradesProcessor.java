/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.RegionUtils;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.KinesisClientLibConfiguration;
import com.amazonaws.services.kinesis.clientlibrary.lib.worker.Worker;
import java.util.UUID;
import utils.ConfigurationUtils;
import utils.CredentialUtils;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author João Pedro Toledo
 */
public class StockTradesProcessor {

    private static final Log LOG = LogFactory.getLog(StockTradesProcessor.class);

    private static final Logger ROOT_LOGGER = Logger.getLogger("");
    private static final Logger PROCESSOR_LOGGER =
            Logger.getLogger("Pacotes de Códigos-fonte.processor");

    private static void checkUsage(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: " + StockTradesProcessor.class.getSimpleName()
                    + " <application name> <stream name> <region>");
            System.exit(1);
        }
    }

    /**
     * Sets the global log level to WARNING and the log level for this package to INFO,
     * so that we only see INFO messages for this processor. This is just for the purpose
     * of this tutorial, and should not be considered as best practice.
     *
     */
    private static void setLogLevels() {
        ROOT_LOGGER.setLevel(Level.WARNING);
        PROCESSOR_LOGGER.setLevel(Level.INFO);
    }

    public static void main(String[] args) throws Exception {
        checkUsage(args);

        String applicationName = args[0];
        String streamName = args[1];
        Region region = RegionUtils.getRegion(args[2]);
        if (region == null) {
            System.err.println(args[2] + " is not a valid AWS region.");
            System.exit(1);
        }

        setLogLevels();

        AWSCredentialsProvider credentialsProvider = CredentialUtils.getCredentialsProvider();

        String workerId = String.valueOf(UUID.randomUUID());
        KinesisClientLibConfiguration kclConfig =
                new KinesisClientLibConfiguration(applicationName, streamName, credentialsProvider, workerId)
            .withRegionName(region.getName())
            .withCommonClientConfig(ConfigurationUtils.getClientConfigWithUserAgent());

        IRecordProcessorFactory recordProcessorFactory = new StockTradeProcessorFactory();

        // Create the KCL worker with the stock trade record processor factory
        Worker worker = new Worker(recordProcessorFactory, kclConfig);

        int exitCode = 0;
        try {
            worker.run();
        } catch (Throwable t) {
            LOG.error("Caught throwable while processing data.", t);
            exitCode = 1;
        }
        System.exit(exitCode);

    }

}
