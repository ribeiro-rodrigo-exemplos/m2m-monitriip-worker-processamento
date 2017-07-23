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
class PeriodoViagemRoutes extends RouteBuilder{

    @Autowired
    @Qualifier('dbConfig') dbConfig

    @Override
    void configure() throws Exception {

        from('direct:abrir-periodo-viagem-route').
            routeId('abrir-periodo-viagem-route').
            convertBodyTo(Map).
            setProperty('payload',body()).
            to('velocity:translators/viagem/consultar-viagem.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            setProperty('viagem',body()).
            setBody(exchangeProperty('payload')).
            to('direct:consultar-jornada-route').
            setProperty('jornada',body()).
            choice().
                when(exchangeProperty('jornada').isNull()).
                    setBody(exchangeProperty('payload')).
                    to('direct:abrir-jornada-route').
                    setBody(exchangeProperty('viagem')).
                    setProperty('jornada',exchangeProperty('payload')).
                    to('jornadaMessagingMapper').
                    to('direct:criar-novo-periodo-viagem-route').
                when(exchangeProperty('jornada').isNotNull()).
                    setBody(exchangeProperty('viagem')).
                    to('direct:criar-novo-periodo-viagem-route').
        end()

        from('direct:fechar-periodo-viagem-route').
            routeId('fechar-periodo-viagem-route').
            convertBodyTo(Map).
            setProperty('payload',body()).
            to('direct:consultar-periodo-viagem-route').
            to('direct:processar-fechamento-periodo-viagem-route').
        end()

        from('direct:alterar-periodo-viagem-route').
            routeId('alterar-periodo-viagem-route').
            choice().
                when(header('estado').isEqualToIgnoreCase('aberto')).
                    to('direct:reabrir-jornada-route').
                    to('direct:reabrir-periodo-viagem-route').
                when(header('estado').isEqualToIgnoreCase('fechado')).
                    setProperty('backup',body()).
                    to('direct:fechar-jornada-route').
                    setBody(simple('${property.backup}')).
                    removeProperty('backup').
                    to('direct:fechar-periodo-viagem-route').
        end()

        from('direct:reabrir-periodo-viagem-route').
            routeId('reabrir-periodo-viagem-route').
            to('velocity:translators/viagem/reabrir.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
            process{it.setProperty 'updated.periodo',it.in.body['matchedCount'] ? true : false}.
            process{it.setProperty 'updated', it.getProperty('updated.periodo') && it.getProperty('updated.jornada')}.
            setBody(constant(null)).
            choice().
                when(exchangeProperty('updated').isEqualTo(true))
                    .setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
                when(exchangeProperty('updated').isEqualTo(false)).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
            endChoice().
        end()

        from('direct:abrir-viagem-route').
            routeId('abrir-viagem-route').
            convertBodyTo(Map).
            setHeader('idJornada',simple('${body[idJornada]}')).
            setProperty('payload',body()).
            to('direct:consultar-jornada-route').
            choice().
                when(body().isNotNull()).
                    to('direct:processar-abertura-viagem-route').
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(204)).
                    setBody(constant(null)).
                when(body().isNull()).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(424)).
        end()

        from('direct:fechar-viagem-route').
            routeId('fechar-viagem-route').
            convertBodyTo(Map).
            setProperty('payload',body()).
            to('direct:consultar-ultimo-periodo-viagem-route').
            choice().
                when(body().isNotNull()).
                    setHeader('idJornada',simple('${body[idJornada]}')).
                    to('direct:processar-fechamento-periodo-viagem-route').
                when(body().isNull()).
                    setHeader(Exchange.HTTP_RESPONSE_CODE,constant(404)).
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
            routeId('consultar-periodo-viagem-route').
            to('velocity:translators/viagem/consultar-periodo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataHoraInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
        end()

        from('direct:consultar-ultimo-periodo-viagem-route').
            routeId('consultar-ultimo-periodo-viagem-route').
            setHeader(MongoDbConstants.SORT_BY,constant('{dataHoraInicial:-1}')).
            setHeader(MongoDbConstants.LIMIT,constant(1)).
            to('velocity:translators/viagem/consultar-viagem.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
        end()

        from('direct:processar-abertura-viagem-route').
            routeId('processar-abertura-viagem-route').
                setProperty('jornada',body()).
                setBody(exchangeProperty('payload')).
                removeProperty('payload').
                process('viagemMessagingMapper').
                to('velocity:translators/viagem/abrir.vm').
                to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=insert").
                choice().
                    when(simple('${property.jornada[aberto]} == false')).
                        to('direct:reabrir-jornada-route').
        end()

        from('direct:processar-fechamento-periodo-viagem-route').
            routeId('processar-fechamento-periodo-viagem-route').
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
    }
}
