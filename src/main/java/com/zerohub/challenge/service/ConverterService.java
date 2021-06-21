package com.zerohub.challenge.service;

import java.math.BigDecimal;

public interface ConverterService {

    void addCurrencies(String baseCurrency, String quoteCurrency, BigDecimal price);

    BigDecimal convert(String fromCurrency, String toCurrency, BigDecimal fromAmount);

}
