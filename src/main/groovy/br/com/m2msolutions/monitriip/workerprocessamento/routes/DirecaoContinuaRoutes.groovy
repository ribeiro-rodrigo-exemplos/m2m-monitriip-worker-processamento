package br.com.m2msolutions.monitriip.workerprocessamento.routes

import com.mongodb.DBObject
import com.mongodb.MongoTimeoutException
import org.apache.camel.Exchange
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mongodb.MongoDbConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 08/04/17.
 */
@Component
class DirecaoContinuaRoutes extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        from('direct:direcao-continua-route').
            routeId('direcao-continua-route').
            convertBodyTo(Map).
            setProperty('originalPayload',body()).
            to('sql:classpath:sql/obter-tempo-direcao-continua.sql?dataSource=ssoDb&outputType=SelectOne').
            setProperty('tempoMaximoDirecao',simple('${body[tempo]}')).
            setBody(simple('${property.originalPayload}')).
            to('velocity:translators/viagem/consultar-periodo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant("{'_id':1}")).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            choice().
                when(body().isNotNull()).
                    to('velocity:translators/direcao/atualizar.vm').
                    convertBodyTo(DBObject).
                    to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
                    setBody(constant(null)).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
                otherwise().
                    setBody(constant(null)).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
        end()
    }
}
