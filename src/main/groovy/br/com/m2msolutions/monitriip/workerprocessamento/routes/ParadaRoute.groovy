package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.enums.MotivoParada
import com.mongodb.DBObject
import com.mongodb.MongoTimeoutException
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

        onException(MongoTimeoutException).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${exception.message} - id: ${id}').
            logExhaustedMessageHistory(false).
            maximumRedeliveries(10).
            redeliveryDelay(10000).
        end()

        from('direct:parada-route').
            routeId('parada-route').
            setProperty('idViagem',simple('${body[idViagem]}')).
            process({it.setProperty('motivo',MotivoParada.obterTipo(it.in.body['codigoMotivoParada']))}).
            to('velocity:translators/parada/criar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
        end()
    }
}
