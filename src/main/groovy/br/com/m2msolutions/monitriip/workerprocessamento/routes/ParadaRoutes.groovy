package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.enums.MotivoParada
import com.mongodb.DBObject
import com.mongodb.MongoTimeoutException
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 06/04/17.
 */
@Component
class ParadaRoutes extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        from('direct:parada-route').
            routeId('parada-route').
            convertBodyTo(Map).
            process({it.setProperty('motivo',MotivoParada.obterTipo(it.in.body['codigoMotivoParada']))}).
            to('velocity:translators/parada/criar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
            process{it.setProperty 'updated',it.in.body['modifiedCount'] ? true : false}.
            setBody(constant(null)).
            choice().
                when(exchangeProperty('updated')).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
                otherwise().
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
        end()
    }
}
