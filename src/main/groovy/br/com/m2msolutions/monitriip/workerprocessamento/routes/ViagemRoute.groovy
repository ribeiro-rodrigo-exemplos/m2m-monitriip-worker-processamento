package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
import com.mongodb.DBObject
import com.mongodb.MongoTimeoutException
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mongodb.MongoDbConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
/**
 * Created by Rodrigo Ribeiro on 03/04/17.
 */
//@Component
class ViagemRoute extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Autowired
    @Qualifier('rabbitConfig')
    def rcfg

    @Override
    void configure() throws Exception {

        from('direct:abrir-viagem-route').
            routeId('abrir-viagem-route').
            log('${body}').
        end()

        from('direct:fechar-viagem-route').
            routeId('fechar-viagem-route').
            log('${body}').
        end()
    }
}
