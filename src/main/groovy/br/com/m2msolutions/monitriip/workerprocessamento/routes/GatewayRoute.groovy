package br.com.m2msolutions.monitriip.workerprocessamento.routes

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 03/04/17.
 */
@Component
class GatewayRoute extends RouteBuilder {

    @Override
    void configure() throws Exception {

        from("file:exemplos?delay=5s&noop=true").
            unmarshal().string().
            choice().
                when().jsonpath('$[?(@.idLog == 4)]').
                    to('mock:velocidade-localizacao-route').
                when().jsonpath('$[?(@.idLog == 5)]').
                    to('mock:jornada-route').
                when().jsonpath('$[?(@.idLog == 6)]').
                    to('mock:parada-route').
                when().jsonpath('$[?(@.idLog == 7)]').
                    to('direct:viagem-route').
                when().jsonpath('$[?(@.idLog == 8)]').
                    to('mock:viagem-route').
                when().jsonpath('$[?(@.idLog == 9)]').
                    to('mock:bilhete-route').
                when().jsonpath('$[?(@.idLog == 250)]').
                    to('mock:direcao-continua-route').
            endChoice().
        end()

    }
}
