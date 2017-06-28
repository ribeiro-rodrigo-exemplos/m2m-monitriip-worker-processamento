package br.com.m2msolutions.monitriip.workerprocessamento.routes

import com.mongodb.DBObject
import com.mongodb.MongoTimeoutException
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
*  Created by Rodrigo Ribeiro on 06/04/17.
*/

@Component
class BilheteRoute extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        onException(MongoTimeoutException).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${exception.message} - id: ${id}').
            logExhaustedMessageHistory(false).
            maximumRedeliveries(6).
            redeliveryDelay(10000).
        end()

        from("direct:bilhete-route").
            routeId('bilhete-route').
            setProperty('idViagem',simple('${body[idViagem]}')).
            to('velocity:translators/bilhete/criar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
        end()
    }
}
