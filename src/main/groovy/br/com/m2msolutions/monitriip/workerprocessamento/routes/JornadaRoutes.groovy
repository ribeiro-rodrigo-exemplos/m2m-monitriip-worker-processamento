package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
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
 * Created by Rodrigo Ribeiro on 03/04/17.
 */
@Component
class JornadaRoutes extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        from('direct:abrir-jornada-route').
            routeId('abrir-jornada').
            convertBodyTo(Map).
            to('direct:obter-nome-motorista-route').
            process{it.setProperty 'dataInicial', DateUtil.formatarData(it.in.body['dataHoraEvento'] as String)}.
            to('velocity:translators/jornada/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=save").
            setBody(simple(null)).
            setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
        end()

        from('direct:fechar-jornada-route').
            routeId('fechar-jornada').
            convertBodyTo(Map).
            to('direct:obter-tempo-maximo-jornada-route').
            to("velocity:translators/jornada/consultar-jornada.vm").
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataHoraInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
            choice().
                when(body().isNotNull()).
                    to('direct:processar-fechamento-jornada-route').
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
                    setBody(constant(null)).
                otherwise().
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
                    setBody(constant(null)).
            endChoice().
        end()

        from('direct:reabrir-jornada-route').
            routeId('reabrir-jornada-route').
            to('velocity:translators/jornada/reabrir.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=update").
            process{it.setProperty 'updated.jornada',it.in.body['matchedCount'] ? true : false}.
        end()

        from('direct:obter-nome-motorista-route').
            routeId('obter-nome-motorista').
            setProperty('payload',simple('${body}')).
            to('sql:classpath:sql/obter-nome-motorista.sql?dataSource=frotaDb&outputType=SelectOne').
            setProperty('nomeMotorista',simple('${body}')).
            setBody(simple('${property.payload}')).
        end()

        from('direct:obter-tempo-maximo-jornada-route').
            routeId('obter-tempo-maximo-jornada-route').
            setProperty('payload',simple('${body}')).
            to('sql:classpath:sql/obter-tempo-jornada.sql?dataSource=ssoDb&outputType=SelectOne').
            setProperty('tempoMaximoJornada',simple('${body[tempo]}')).
        end()

        from('direct:processar-fechamento-jornada-route').
            routeId('processar-fechamento-jornada-route').
            process('processadorDePeriodos').
            process({e ->
                e.setProperty 'dataFinal', DateUtil.formatarData(e.getProperty('payload')['dataHoraEvento'] as String)
            }).
            to('velocity:translators/jornada/fechar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=update").
            process{it.setProperty 'updated',it.in.body['modifiedCount'] ? true : null}.
            choice().
                when(body().isNotNull()).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
                    setBody(constant(null)).
                when(body().isNull()).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
                    setBody(constant(null)).
            endChoice().
        end()

        from('direct:consultar-jornada-route').
            routeId('consultar-jornada-route').
            to('velocity:translators/jornada/consultar-jornada.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
        end()
    }
}
