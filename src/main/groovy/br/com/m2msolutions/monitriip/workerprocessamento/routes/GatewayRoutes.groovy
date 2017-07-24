package br.com.m2msolutions.monitriip.workerprocessamento.routes

import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.direct.DirectConsumerNotAvailableException
import org.apache.camel.model.dataformat.JsonLibrary
import org.apache.camel.model.rest.RestBindingMode
import org.apache.camel.model.rest.RestParamType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 03/04/17.
 */
@Component
class GatewayRoutes extends RouteBuilder {

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
            put('/{idJornada}').
                to('direct:abrir-jornada-route').
            patch('/{idJornada}').
                to('direct:fechar-jornada-route')

        rest('/viagens/{idViagem}').
            put().
                to('direct:abrir-viagem-route').
            patch().
                to('direct:fechar-viagem-route')

        rest('/viagens/{idViagem}/jornadas').
            put('/{idJornada}').
                to('direct:abrir-periodo-viagem-route').
            patch('/{idJornada}').
                param().
                    name('estado').
                    type(RestParamType.query).
                endParam().
                    to('direct:alterar-periodo-viagem-route')

        rest('/viagens/{idViagem}/jornadas/{idJornada}').
            post('/bilhetes').
                to('direct:bilhete-route').
            post('/paradas').
                to('direct:parada-route').
            post('/velocidades').
                to('direct:velocidade-localizacao-route').
            put('/direcoes').
                to('direct:direcao-continua-route')
    }
}
