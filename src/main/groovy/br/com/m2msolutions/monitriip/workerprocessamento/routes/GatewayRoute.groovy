package br.com.m2msolutions.monitriip.workerprocessamento.routes

import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 03/04/17.
 */
@Component
class GatewayRoute extends RouteBuilder {

    @Autowired
    @Qualifier('rabbitConfig')
    def rcfg

    @Override
    void configure() throws Exception {

        from("rabbitmq://${rcfg.url}/${rcfg.exchange}?queue=${rcfg.queue}&deadLetterExchange=${rcfg['exchange-dlq']}" +
                "&deadLetterQueue=${rcfg.deadLetterQueue}&deadLetterRoutingKey=dead.letters&autoAck=false&" +
                "autoDelete=false&concurrentConsumers=${rcfg.concurrentConsumers}&username=${rcfg.username}" +
                "&password=${rcfg.password}").
            setProperty('payloadBackup',simple('${body}')).
            unmarshal().string().
            convertBodyTo(Map).
            choice().
                when().expression(simple('${body[idLog]} == 4')).
                    to('direct:velocidade-localizacao-route').
                when().expression(simple('${body[idLog]} == 5')).
                    to('direct:jornada-route').
                when().expression(simple('${body[idLog]} == 6')).
                    to('direct:parada-route').
                when().expression(simple('${body[idLog]} == 7')).
                    to('direct:viagem-route').
                when().expression(simple('${body[idLog]} == 8')).
                    to('direct:viagem-route').
                when().expression(simple('${body[idLog]} == 9')).
                    to('direct:bilhete-route').
                when().expression(simple('${body[idLog]} == 250')).
                    to('direct:direcao-continua-route').
            endChoice().
        end()

    }
}
