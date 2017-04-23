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
class VelocidadeLocalizacaoRoute extends RouteBuilder {

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

        from('direct:velocidade-localizacao-route').
            routeId('velocidade-localizacao-route').
            convertBodyTo(Map).
            setProperty('originalPayload',simple('${body}')).
            to('velocity:translators/viagem/consultar-transbordo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant("{'localizacaoInicial.coordinates':1}")).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            process({
                if(!it.in.body){
                    def message = "Viagem ${it.getProperty('originalPayload')['idViagem']} não foi encontrada."
                    throw new ViagemNaoEncontradaException(message)
                }
            }).
            process('processadorDeDistancias').
            process('velocidadeLocalizacaoMessagingMapper').
            to('velocity:translators/velocidade/criar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
        end()

    }
}
