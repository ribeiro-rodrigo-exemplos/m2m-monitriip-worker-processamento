package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.enums.EstadoIgnicao
import br.com.m2msolutions.monitriip.workerprocessamento.enums.EstadoPorta
import br.com.m2msolutions.monitriip.workerprocessamento.util.DistanceCalculator
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
class VelocidadeLocalizacaoRoute extends RouteBuilder {

    @Autowired
    DeadLetterChannelBuilder globalDeadLetterChannel

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        from('direct:velocidade-localizacao-route').
            routeId('velocidade-localizacao-route').
            convertBodyTo(Map).
            setProperty('originalPayload',simple('${body}')).
            to('velocity:translators/viagem/consultar-transbordo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant("{'localizacaoInicial.coordinates':1}")).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            process({
                if(!it.in.body)
                    throw new RuntimeException('Viagem não encontrada')
            }).
            process('processadorDeDistancias').
            process('velocidadeLocalizacaoMessagingMapper').
            to('velocity:translators/velocidade/criar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
            log('${body}').
        end()

    }
}
