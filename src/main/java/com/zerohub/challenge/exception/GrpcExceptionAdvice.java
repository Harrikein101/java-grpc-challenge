package com.zerohub.challenge.exception;

import com.zerohub.challenge.exception.business.CurrencyNotFoundException;
import com.zerohub.challenge.exception.business.RateNotFoundException;
import io.grpc.Status;
import io.grpc.StatusException;
import net.devh.boot.grpc.server.advice.GrpcAdvice;
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GrpcExceptionAdvice {

    @GrpcExceptionHandler
    public Status handleRateNotFound(RateNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage()).withCause(ex);
    }

    @GrpcExceptionHandler
    public Status handleCurrencyNotFound(CurrencyNotFoundException ex) {
        return Status.NOT_FOUND.withDescription(ex.getMessage()).withCause(ex);
    }

}
