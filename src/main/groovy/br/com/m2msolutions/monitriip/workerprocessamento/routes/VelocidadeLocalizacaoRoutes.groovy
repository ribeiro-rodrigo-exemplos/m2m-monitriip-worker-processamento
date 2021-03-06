package br.com.m2msolutions.monitriip.workerprocessamento.routes

import com.mongodb.DBObject
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mongodb.MongoDbConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
/**
 * Created by Rodrigo Ribeiro on 08/04/17.
 */
@Component
class VelocidadeLocalizacaoRoutes extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        from('direct:velocidade-localizacao-route').
            routeId('velocidade-localizacao-route').
            convertBodyTo(Map).
            setProperty('originalPayload',body()).
            to('velocity:translators/viagem/consultar-periodo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant("{'localizacaoInicial.coordinates':1}")).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            choice().
                when(body().isNull()).
                    setBody(constant(null)).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
                when(body().isNotNull()).
                    process('processadorDeDistancias').
                    process('velocidadeLocalizacaoMessagingMapper').
                    to('velocity:translators/velocidade/criar.vm').
                    convertBodyTo(DBObject).
                    to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
                    setBody(constant(null)).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
        end()
    }
}
