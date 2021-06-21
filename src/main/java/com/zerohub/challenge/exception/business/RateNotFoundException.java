package com.zerohub.challenge.exception.business;

public class RateNotFoundException extends RuntimeException {

    public RateNotFoundException(String fromCurrency, String toCurrency, Throwable cause) {
        super(String.format("Rate %s-%s was not found", fromCurrency, toCurrency), cause);
    }

}
