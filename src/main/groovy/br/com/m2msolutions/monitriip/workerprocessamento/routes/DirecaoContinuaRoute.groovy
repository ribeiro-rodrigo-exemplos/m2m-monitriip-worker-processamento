package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.ViagemNaoEncontradaException
import com.mongodb.DBObject
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
class DirecaoContinuaRoute extends RouteBuilder {

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

        from('direct:direcao-continua-route').
            routeId('direcao-continua-route').
            setProperty('originalPayload',simple('${body}')).
            to('sql:classpath:sql/obter-tempo-direcao-continua.sql?dataSource=ssoDb&outputType=SelectOne').
            setProperty('tempoMaximoDirecao',simple('${body[tempo]}')).
            setBody(simple('${property.originalPayload}')).
            to('velocity:translators/viagem/consultar-periodo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant("{'_id':1}")).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            process({
                if(!it.in.body){
                    def message = "Viagem ${it.getProperty('originalPayload')['idViagem']} não foi encontrada."
                    throw new ViagemNaoEncontradaException(message)
                }
            }).
            to('velocity:translators/direcao/atualizar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
        end()
    }
}
