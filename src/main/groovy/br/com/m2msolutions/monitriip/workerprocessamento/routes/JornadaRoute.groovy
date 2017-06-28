package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
import com.mongodb.DBObject
import com.mongodb.MongoTimeoutException
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
class JornadaRoute extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        onException(MongoTimeoutException).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${exception.message} - id: ${id}').
            logExhaustedMessageHistory(false).
            maximumRedeliveries(13).
            redeliveryDelay(10000).
        end()

        from("direct:jornada-route").
            routeId('jornada-route').
            choice().
                when().expression(simple('${body[tipoRegistroEvento]} == 1')).
                    to('direct:abrir-jornada-route').
                when().expression(simple('${body[tipoRegistroEvento]} == 0')).
                    to('direct:fechar-jornada-route').
            endChoice().
        end()

        from('direct:abrir-jornada-route').
            routeId('abrir-jornada').
            setProperty('payload',simple('${body}')).
            to('sql:classpath:sql/obter-nome-motorista.sql?dataSource=frotaDb&outputType=SelectOne').
            setProperty('nomeMotorista',simple('${body}')).
            setBody(simple('${property.payload}')).
            process({e ->
                e.setProperty 'dataInicial', DateUtil.formatarData(e.in.body['dataHoraEvento'] as String)
            }).
            to('velocity:translators/jornada/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=save").
            setBody(simple('${property.payload}')).
            choice().
                when().expression(simple('${body[idViagem]} != null')).
                    to("direct:converter-jornada-viagem").
            endChoice().
        end()

        from('direct:fechar-jornada-route').
            routeId('fechar-jornada').
            setProperty('payload',simple('${body}')).
            to('sql:classpath:sql/obter-tempo-jornada.sql?dataSource=ssoDb&outputType=SelectOne').
            setProperty('tempoMaximoJornada',simple('${body[tempo]}')).
            to("velocity:translators/jornada/consultar-jornada.vm").
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataHoraInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
            choice().
                when(simple('${body} != null')).
                    to('direct:processar-fechamento-jornada-route').
                otherwise().
                    to('direct:fallback-route').
        end()

        from('direct:processar-fechamento-jornada-route').
            routeId('processar-fechamento-jornada').
            process('processadorDePeriodos').
            process({e ->
                e.setProperty 'dataFinal', DateUtil.formatarData(e.getProperty('payload')['dataHoraEvento'] as String)
            }).
            to('velocity:translators/jornada/fechar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=update").
            setBody(simple('${property.payload}')).
            filter().
                expression(simple('${body[idViagem]} != null')).
                    to("direct:converter-jornada-viagem").
        end()

        from('direct:converter-jornada-viagem').
            routeId('converter-jornada-viagem').
            to("velocity:translators/viagem/consultar-viagem.vm").
            removeHeader(MongoDbConstants.FIELDS_FILTER).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            process('processadorDeJornadas').
            to('direct:viagem-route').
        end()
    }
}
