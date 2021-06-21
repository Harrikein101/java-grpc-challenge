package com.zerohub.challenge;

import com.zerohub.challenge.proto.ConvertRequest;
import com.zerohub.challenge.proto.ConvertResponse;
import com.zerohub.challenge.proto.PublishRequest;
import com.zerohub.challenge.proto.RatesServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test",
        "grpc.server.port=-1",
        "grpc.client.inProcess.address=in-process:test"
})
@ExtendWith(SpringExtension.class)
@DirtiesContext
@Slf4j
public class ChallengeApplicationTest {
    private static final String BTC = "BTC";
    private static final String EUR = "EUR";
    private static final String USD = "USD";
    private static final String UAH = "UAH";
    private static final String RUB = "RUB";
    private static final String LTC = "LTC";
    private static final String AUD = "AUD";
    private static final String GBP = "GBP";
    private static final String JPY = "JPY";

    @GrpcClient("inProcess")
    private RatesServiceGrpc.RatesServiceBlockingStub service;

    @BeforeEach
    public void setup() {
        var rates = List.of(
                toPublishRequest(new String[]{BTC, EUR, "50000.0000"}),
                toPublishRequest(new String[]{EUR, USD, "1.2000"}),
                toPublishRequest(new String[]{EUR, AUD, "1.5000"}),
                toPublishRequest(new String[]{USD, RUB, "80.0000"}), // раньше была опечатка
                toPublishRequest(new String[]{UAH, RUB, "4.0000"}),
                toPublishRequest(new String[]{LTC, BTC, "0.0400"}),
                toPublishRequest(new String[]{LTC, USD, "2320.0000"}),
                toPublishRequest(new String[]{LTC, USD, "2320.0000"}),
                toPublishRequest(new String[]{GBP, JPY, "152.1400"})
        );
        for (var rate : rates) {
            service.publish(rate);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testData")
    void ConvertTest(String ignore, ConvertRequest request, BigDecimal expectedPrice) {
        ConvertResponse response = service.convert(request);
        BigDecimal returnedPrice = new BigDecimal(response.getPrice());
        assertEquals(expectedPrice, returnedPrice);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testFailedData")
    void ConvertFailedTest(String ignore, ConvertRequest request, Status status) {
        try {
            ConvertResponse response = service.convert(request);
        } catch (StatusRuntimeException ex) {
            assertEquals(ex.getStatus().getCode(), status.getCode());
        }
    }

    private static Stream<Arguments> testData() {

        return Stream.of(
                Arguments.of("Same currency", toConvertRequest(new String[]{BTC, BTC, "0.9997"}), "0.9997"),
                Arguments.of("Simple conversion", toConvertRequest(new String[]{EUR, BTC, "50000.0000"}), "1.0000"),
                Arguments.of("Reversed conversion", toConvertRequest(new String[]{BTC, EUR, "1.0000"}), "50000.0000"),
                Arguments.of("Convert with one hop", toConvertRequest(new String[]{BTC, AUD, "1.0000"}), "75000.0000"),
                // значение было изменено с 4640000 на 4800000, потому что BTC/LTC/USD/RUB менее выгодный, чем BTC/EUR/USD/RUB
                Arguments.of("Convert with two hops", toConvertRequest(new String[]{BTC, RUB, "1.0000"}), "4800000.0000"),
                Arguments.of("Reversed conversion with two hops", toConvertRequest(new String[]{RUB, EUR, "96.0000"}), "1.0000"),
                Arguments.of("Small number conversion", toConvertRequest(new String[]{BTC, USD, "0.000000003"}), "0.0002"),
                Arguments.of("Too small number conversation", toConvertRequest(new String[]{BTC, USD, "0.0000000003"}), "0.0000")
        );
    }

    private static Stream<Arguments> testFailedData() {
        return Stream.of(
                Arguments.of("Not existed currency", toConvertRequest(new String[]{"test", BTC, "0.9997"}),
                        Status.NOT_FOUND),
                Arguments.of("Not existed currency", toConvertRequest(new String[]{UAH, "test", "0.9997"}),
                        Status.NOT_FOUND),
                Arguments.of("Not existed currencies", toConvertRequest(new String[]{"test", "test", "0.9997"}),
                        Status.NOT_FOUND),
                Arguments.of("No rate for currencies", toConvertRequest(new String[]{JPY, BTC, "50000.0000"}),
                        Status.NOT_FOUND)
        );
    }

    private static PublishRequest toPublishRequest(String[] args) {
        return PublishRequest
                .newBuilder()
                .setBaseCurrency(args[0])
                .setQuoteCurrency(args[1])
                .setPrice(args[2])
                .build();
    }

    private static ConvertRequest toConvertRequest(String[] args) {
        return ConvertRequest
                .newBuilder()
                .setFromCurrency(args[0])
                .setToCurrency(args[1])
                .setFromAmount(args[2])
                .build();
    }
}
