package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.enums.Sentido
import br.com.m2msolutions.monitriip.workerprocessamento.enums.TipoTransporte
import br.com.m2msolutions.monitriip.workerprocessamento.enums.TipoViagem
import com.mongodb.DBObject
import org.apache.camel.builder.DeadLetterChannelBuilder
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mongodb.MongoDbConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by rodrigo on 03/04/17.
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
            setProperty('payload',simple('${body}')).
            setBody(simple('resource:classpath:viagem/consultar-linha.bson')).
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{"descr":1}')).
            to("mongodb:frotaDb?database=${dbConfig.frota.database}&collection=Linha&operation=findOneByQuery").
                setProperty('linha',simple('${body[descr]}')).
            setBody(simple('resource:classpath:jornada/consultar-jornada.bson')).
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{"cpfMotorista":1}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
                process({
                    e ->
                        if(!e.in.body)
                            throw new RuntimeException('Jornada não encontrada')
                }).
                setProperty('cpfMotorista',simple('${body[cpfMotorista]}')).
                process({
                    e ->
                        def payload  = e.getProperty 'payload',DBObject

                        e.setProperty 'tipoViagem',TipoViagem.obterTipo(payload['codigoTipoViagem'])
                        e.setProperty 'sentidoLinha',Sentido.obterSentido(payload['codigoSentidoLinha'])
                        e.setProperty 'tipoTransporte',TipoTransporte.obterTipo(payload['idLog'])
                }).
                setBody(simple('resource:classpath:viagem/abrir.bson')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=insert").
        end()

        from('direct:fechar-viagem-route').
            routeId('fechar-viagem-route').
            setBody(simple('resource:classpath:viagem/fechar.bson')).
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
            process({
                if(!it.in.body["matchedCount"])
                    throw new RuntimeException('Viagem não encontrada')
            }).
        end()
    }
}
