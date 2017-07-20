package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
import com.mongodb.DBObject
import org.apache.camel.Exchange
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mongodb.MongoDbConstants
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 19/07/17.
 */
@Component
class PeriodoViagemRoute extends RouteBuilder{

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        from('direct:abrir-periodo-viagem-route').
            routeId('abrir-periodo-viagem-route').
            convertBodyTo(Map).
            setProperty('payload',simple('${body}')).
            to('velocity:translators/viagem/consultar-viagem.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            choice().
                when(body().isNotNull()).
                    to('direct:criar-novo-periodo-viagem-route').
                when(body().isNull()).
                    setBody(simple('${property.payload}')).
                    to('direct:abrir-viagem-route').
        end()

        from('direct:fechar-periodo-viagem-route').
            routeId('fechar-periodo-viagem-route').
            convertBodyTo(Map).
            setProperty('payload',simple('${body}')).
            to('direct:consultar-periodo-viagem-route').
            choice().
                when(body().isNotNull()).
                    process('processadorDePeriodos').
                    process{it.setProperty 'dataFinal', DateUtil.formatarData(it.getProperty('payload')['dataHoraEvento'] as String)}.
                    to('velocity:translators/viagem/fechar.vm').
                    convertBodyTo(DBObject).
                    to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
                    setBody(constant(null)).
                when(body().isNull()).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
                    setBody(constant(null)).
        end()

        from('direct:abrir-viagem-route').
            routeId('abrir-viagem-route').
            process('viagemMessagingMapper').
            to('velocity:translators/viagem/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=insert").
            setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
            setBody(constant(null)).
        end()

        from('direct:criar-novo-periodo-viagem-route').
            routeId('criar-novo-periodo-viagem-route').
            process('periodoViagemMessagingMapper').
            to('velocity:translators/viagem/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=insert").
            setBody(constant(null)).
            setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
        end()

        from('direct:consultar-periodo-viagem-route').
            to('velocity:translators/viagem/consultar-periodo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataHoraInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
        end()
    }
}
