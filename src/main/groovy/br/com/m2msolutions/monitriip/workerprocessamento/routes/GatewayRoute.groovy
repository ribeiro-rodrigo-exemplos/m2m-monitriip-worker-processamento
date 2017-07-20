package br.com.m2msolutions.monitriip.workerprocessamento.routes

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.direct.DirectConsumerNotAvailableException
import org.apache.camel.model.dataformat.JsonLibrary
import org.apache.camel.model.rest.RestBindingMode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 03/04/17.
 */
@Component
class GatewayRoute extends RouteBuilder {

    @Value('${server.port}')
    Integer serverPort

    @Override
    void configure() throws Exception {

        restConfiguration().
            component('jetty').
                host('0.0.0.0').
                port(serverPort).
                bindingMode(RestBindingMode.auto)

        rest('/jornadas').
            put('/{idJornada}').to('direct:abrir-jornada-route').
            patch('/{idJornada}').to('direct:fechar-jornada-route')

        rest('/jornadas/{idJornada}/viagens').
            put('/{idViagem}').
                to('direct:abrir-periodo-viagem-route').
            patch('/{idViagem}').
                to('direct:fechar-periodo-viagem-route')

        rest('/jornadas/{idJornada}/viagens/{idViagem}').
            post('/bilhetes').
                to('direct:bilhete-route').
            post('/paradas').
                to('direct:paradas-route').
            post('/velocidade').
                to('direct:velocidade-route').
            put('/direcao').
                to('direct:direcao-continua-route')

        from("direct:convert-data-route").
            convertBodyTo(Map).
        end()



    }
}
