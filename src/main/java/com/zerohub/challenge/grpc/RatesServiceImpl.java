package com.zerohub.challenge.grpc;

import com.google.protobuf.Empty;
import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import com.zerohub.challenge.service.ConverterService;
import com.zerohub.challenge.utils.DecimalUtils;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import java.math.BigDecimal;

@GrpcService
public class RatesServiceImpl extends RatesServiceGrpc.RatesServiceImplBase {

    private final ConverterService converterService;

    @Autowired
    public RatesServiceImpl(ConverterService converterService) {
        this.converterService = converterService;
    }

    @Override
    public void publish(PublishRequest request,
                        StreamObserver<Empty> responseObserver) {
        converterService.addCurrencies(
                request.getBaseCurrency(),
                request.getQuoteCurrency(),
                new BigDecimal(request.getPrice())
        );
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    @Override
    public void convert(ConvertRequest request,
                        StreamObserver<ConvertResponse> responseObserver) {
        BigDecimal result = converterService.convert(
                request.getFromCurrency(),
                request.getToCurrency(),
                new BigDecimal(request.getFromAmount())
        );
        ConvertResponse response = ConvertResponse
                .newBuilder()
                .setPrice(DecimalUtils.roundCarefully(result).toString())
                .build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }
}
