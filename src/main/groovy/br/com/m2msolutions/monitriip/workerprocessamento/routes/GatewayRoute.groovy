package br.com.m2msolutions.monitriip.workerprocessamento.routes

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.direct.DirectConsumerNotAvailableException
import org.apache.camel.model.rest.RestBindingMode
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

        restConfiguration().
                component('jetty').
                host('localhost').
                port(8080).
                bindingMode(RestBindingMode.auto)

        rest('/viagem').
            put('/{idViagem}/').to('direct:abrir-viagem-route').
            patch('/{idViagem}').to('direct:fechar-viagem-route')

        rest('/jornada').
            put('/{idJornada}').to('direct:abrir-jornada-route').
            patch('/{idJornada}').to('direct:fechar-jornada-route')


        from("direct:teste").
            log('recebeu requisicao').
            log('${header.idViagem}').
        end()



    }
}
