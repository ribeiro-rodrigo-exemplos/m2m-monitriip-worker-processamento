package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.enums.MotivoParada
import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.ViagemNaoEncontradaException
import com.mongodb.DBObject
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 06/04/17.
 */
@Component
class ParadaRoute extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        onException(ViagemNaoEncontradaException).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${exception.message} - id: ${id}').
            maximumRedeliveries(0).
            logExhaustedMessageHistory(false).
            useOriginalMessage().
            to("direct:fallback-route").
        end()

        from('direct:parada-route').
            routeId('parada-route').
            setProperty('idViagem',simple('${body[idViagem]}')).
            process({it.setProperty('motivo',MotivoParada.obterTipo(it.in.body['codigoMotivoParada']))}).
            to('velocity:translators/parada/criar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
            process({
                if(!it.in.body["matchedCount"]){
                    def message = "Viagem ${it.getProperty('idViagem')} não foi encontrada."
                    throw new ViagemNaoEncontradaException(message)
                }
            }).
        end()
    }
}
