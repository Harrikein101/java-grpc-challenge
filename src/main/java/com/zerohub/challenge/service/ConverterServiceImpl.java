package com.zerohub.challenge.service;

import com.zerohub.challenge.exception.business.CurrencyNotFoundException;
import com.zerohub.challenge.exception.business.RateNotFoundException;
import com.zerohub.challenge.exception.graph.GraphPathNotFoundException;
import com.zerohub.challenge.exception.graph.VertexNotFoundException;
import com.zerohub.challenge.graph.DirectedWeightedGraph;
import com.zerohub.challenge.graph.DirectedWeightedGraphWithPreprocessing;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class ConverterServiceImpl implements ConverterService {

    private final DirectedWeightedGraph currencyGraph = new DirectedWeightedGraphWithPreprocessing();

    @Override
    public void addCurrencies(String baseCurrency, String quoteCurrency, BigDecimal price) {
        currencyGraph.addEdge(baseCurrency, quoteCurrency, price);
    }

    @Override
    public BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal fromAmount) {
        try {
            BigDecimal factor = currencyGraph.findPath(fromCurrency, toCurrency);
            return fromAmount.multiply(factor);
        } catch (VertexNotFoundException ex) {
            throw new CurrencyNotFoundException(ex.getNotExistedVertex(), ex);
        } catch (GraphPathNotFoundException ex) {
            throw new RateNotFoundException(fromCurrency, toCurrency, ex);
        }
    }

}
