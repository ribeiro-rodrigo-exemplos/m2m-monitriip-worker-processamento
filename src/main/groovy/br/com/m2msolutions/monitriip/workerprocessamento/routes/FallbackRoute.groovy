package br.com.m2msolutions.monitriip.workerprocessamento.routes

import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 23/04/17.
 */
@Component
class FallbackRoute extends RouteBuilder {

    @Autowired
    @Qualifier('rabbitConfig')
    def rcfg

    @Override
    void configure() throws Exception {

        from('direct:fallback-route').
            routeId('fallback-route').
            setHeader('rabbitmq.REQUEUE').constant(true).
            setBody(simple('${property.payloadBackup}')).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${id}').
            delay(rcfg['fallback-delay'] as Integer).
            to("rabbitmq://${rcfg.url}/${rcfg.exchange}?queue=${rcfg.queue}&durable=true&autoDelete=false&" +
                    "username=${rcfg.username}&password=${rcfg.password}&deadLetterExchange=${rcfg['exchange-dlq']}&" +
                    "deadLetterQueue=${rcfg.deadLetterQueue}&deadLetterRoutingKey=dead.letters&queueArgsConfigurer=#queueArgs").
        end()



    }
}
