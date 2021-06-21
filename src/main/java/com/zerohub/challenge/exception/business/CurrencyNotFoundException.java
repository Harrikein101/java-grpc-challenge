package com.zerohub.challenge.exception.business;

public class CurrencyNotFoundException extends RuntimeException {

    public CurrencyNotFoundException(String notExistedCurrency, Throwable cause) {
        super(String.format("Currency %s was not found", notExistedCurrency), cause);
    }

}
