/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processor;

import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.InvalidStateException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ShutdownException;
import com.amazonaws.services.kinesis.clientlibrary.exceptions.ThrottlingException;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorCheckpointer;
import com.amazonaws.services.kinesis.clientlibrary.types.ShutdownReason;
import kinesisjava.Record;
import kinesisjava.StockTrade;


/**
 *
 * @author João Pedro Toledo
 */
public class StockTradeRecordProcessor implements IRecordProcessor {

    private static final Log LOG = LogFactory.getLog(StockTradeRecordProcessor.class);
    private String kinesisShardId;

    // Reporting interval
    private static final long REPORTING_INTERVAL_MILLIS = 60000L; // 1 minute
    private long nextReportingTimeInMillis;

    // Checkpointing interval
    private static final long CHECKPOINT_INTERVAL_MILLIS = 60000L; // 1 minute
    private long nextCheckpointTimeInMillis;

    // Aggregates stats for stock trades
    private StockStats stockStats = new StockStats();

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(String shardId) {
        LOG.info("Inicializando o processo de gravação: " + shardId);
        this.kinesisShardId = shardId;
        nextReportingTimeInMillis = System.currentTimeMillis() + REPORTING_INTERVAL_MILLIS;
        nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processRecords(List<Record> records, IRecordProcessorCheckpointer checkpointer) {
        for (Record record : records) {
            // process record
            processRecord(record);
        }

        // If it is time to report stats as per the reporting interval, report stats
        if (System.currentTimeMillis() > nextReportingTimeInMillis) {
            reportStats();
            resetStats();
            nextReportingTimeInMillis = System.currentTimeMillis() + REPORTING_INTERVAL_MILLIS;
        }

        // Checkpoint once every checkpoint interval
        if (System.currentTimeMillis() > nextCheckpointTimeInMillis) {
            checkpoint(checkpointer);
            nextCheckpointTimeInMillis = System.currentTimeMillis() + CHECKPOINT_INTERVAL_MILLIS;
        }
    }

    private void reportStats() {
        System.out.println("****** Estilhaco " + kinesisShardId + " do ultimo minuto ******\n" +
                stockStats + "\n" +
                "****************************************************************\n");
    }

    private void resetStats() {
        stockStats = new StockStats();
    }

    private void processRecord(Record record) {
        StockTrade trade = StockTrade.fromJsonAsBytes(record.getData().array());
        if (trade == null) {
            LOG.warn("Pulando gravação. Partition Key: " + record.getPartitionKey());
            return;
        }
        stockStats.addStockTrade(trade);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown(IRecordProcessorCheckpointer checkpointer, ShutdownReason reason) {
        LOG.info("Parando a gravação: " + kinesisShardId);
        // Important to checkpoint after reaching end of shard, so we can start processing data from child shards.
        if (reason == ShutdownReason.TERMINATE) {
            checkpoint(checkpointer);
        }
    }

    private void checkpoint(IRecordProcessorCheckpointer checkpointer) {
        LOG.info("Salvando " + kinesisShardId);
        try {
            checkpointer.checkpoint();
        } catch (ShutdownException se) {
            // Ignore checkpoint if the processor instance has been shutdown (fail over).
            LOG.info("Caught shutdown exception, skipping checkpoint.", se);
        } catch (ThrottlingException e) {
            // Skip checkpoint when throttled. In practice, consider a backoff and retry policy.
            LOG.error("Caught throttling exception, skipping checkpoint.", e);
        } catch (InvalidStateException e) {
            // This indicates an issue with the DynamoDB table (check for table, provisioned IOPS).
            LOG.error("Não foi possivel salvar", e);
        }
    }

}
