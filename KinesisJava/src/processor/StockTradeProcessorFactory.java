/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package processor;

import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessor;
import com.amazonaws.services.kinesis.clientlibrary.interfaces.IRecordProcessorFactory;

/**
 *
 * @author João Pedro Toledo
 */
public class StockTradeProcessorFactory implements IRecordProcessorFactory {

    /**
     * Constructor.
     */
    public StockTradeProcessorFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IRecordProcessor createProcessor() {
        return new StockTradeRecordProcessor();
    }

}

