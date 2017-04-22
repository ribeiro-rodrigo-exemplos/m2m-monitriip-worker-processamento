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
class ViagemRoute extends RouteBuilder {

    @Autowired
    DeadLetterChannelBuilder globalDeadLetterChannel
    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        //errorHandler globalDeadLetterChannel

        from('direct:viagem-route').
            routeId('viagem-route').
            choice().
                when().jsonpath('$[?(@.tipoRegistroViagem == 1)]').
                    to('direct:abrir-viagem-route').
                when().jsonpath('$[?(@.tipoRegistroViagem == 0)]').
                    to('direct:fechar-viagem-route').
            endChoice().
        end()

        from('direct:abrir-viagem-route').
            routeId('abrir-viagem-route').
            convertBodyTo(Map).
            setProperty('payload',simple('${body}')).
            to('velocity:translators/viagem/consultar-linha.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{"descr":1}')).
            to("mongodb:frotaDb?database=${dbConfig.frota.database}&collection=Linha&operation=findOneByQuery").
                setProperty('linha',simple('${body[descr]}')).
            to('velocity:translators/jornada/consultar-jornada.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{"cpfMotorista":1}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
                process({
                    e ->
                        if(!e.in.body)
                            throw new RuntimeException('Jornada não encontrada')
                }).
                setProperty('cpfMotorista',simple('${body[cpfMotorista]}')).
                process('viagemMessagingMapper').
                to('velocity:translators/viagem/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=insert").
        end()

        from('direct:fechar-viagem-route').
            routeId('fechar-viagem-route').
            convertBodyTo(Map).
            setProperty('payload',simple('${body}')).
            to('velocity:translators/viagem/consultar-transbordo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            process({
                if(!it.in.body)
                    throw new RuntimeException('Viagem não encontrada')
            }).
            process('processadorDePeriodos').
            to('velocity:translators/viagem/fechar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
        end()
    }
}
