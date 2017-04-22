package br.com.m2msolutions.monitriip.workerprocessamento.routes

import com.mongodb.DBObject
import org.apache.camel.builder.DeadLetterChannelBuilder
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
    DeadLetterChannelBuilder globalDeadLetterChannel

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        from('direct:direcao-continua-route').
            routeId('direcao-continua-route').
            convertBodyTo(Map).
            setProperty('originalPayload',simple('${body}')).
            to('velocity:translators/viagem/consultar-transbordo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant("{'_id':1}")).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            process({
                if(!it.in.body)
                    throw new RuntimeException('Viagem não encontrada')
            }).
            to('velocity:translators/direcao/criar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
        end()
    }
}
