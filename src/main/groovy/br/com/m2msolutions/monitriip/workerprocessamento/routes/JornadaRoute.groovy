package br.com.m2msolutions.monitriip.workerprocessamento.routes

import com.mongodb.DBObject
import org.apache.camel.builder.DeadLetterChannelBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mongodb.MongoDbConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
/**
 * Created by Rodrigo Ribeiro on 03/04/17.
 */
@Component
class JornadaRoute extends RouteBuilder {

    @Autowired
    DeadLetterChannelBuilder globalDeadLetterChannel

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        //errorHandler(globalDeadLetterChannel)

        from("direct:jornada-route").
            routeId('jornada-route').
            choice().
                when().jsonpath('$[?(@.tipoRegistroEvento == 1)]').
                    to('direct:abrir-jornada-route').
                when().jsonpath('$[?(@.tipoRegistroEvento == 0)]').
                    to('direct:fechar-jornada-route').
            endChoice().
        end()

        from('direct:abrir-jornada-route').
            routeId('abrir-jornada').
            convertBodyTo(Map).
            to('velocity:jornada/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=insert").
        end()

        from('direct:fechar-jornada-route').
            routeId('fechar-jornada').
            convertBodyTo(Map).
            setProperty('payload',simple('${body}')).
            to("velocity:jornada/consultar-jornada.vm").
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
            process({
                if(!it.in.body)
                    throw new RuntimeException('Jornada não encontrada')
            }).
            process('processadorDePeriodos').
            to('velocity:jornada/fechar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=update").
        end()
    }
}
