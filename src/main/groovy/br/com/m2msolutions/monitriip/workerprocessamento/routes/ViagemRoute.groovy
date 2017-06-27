package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.JornadaNaoEncontradaException
import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.ViagemNaoEncontradaException
import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
import com.mongodb.DBObject
import com.mongodb.MongoSocketException
import com.mongodb.MongoSocketOpenException
import com.mongodb.MongoTimeoutException
import com.mongodb.MongoWriteException
import org.apache.camel.LoggingLevel
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.mongodb.CamelMongoDbException
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
    @Qualifier('dbConfig')
    def dbConfig

    @Autowired
    @Qualifier('rabbitConfig')
    def rcfg

    @Override
    void configure() throws Exception {

        onException(MongoTimeoutException).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${exception.message} - id: ${id}').
            logExhaustedMessageHistory(false).
            maximumRedeliveries(6).
        end()

        from('direct:viagem-route').
            routeId('viagem-route').
            choice().
                when().expression(simple('${body[tipoRegistroViagem]} == 1 || ${body[tipoRegistroViagem]} == 3')).
                    to('direct:abrir-viagem-route').
                when().expression(simple('${body[tipoRegistroViagem]} == 0 || ${body[tipoRegistroViagem]} == 2')).
                    to('direct:fechar-viagem-route').
            endChoice().
        end()

        from('direct:abrir-viagem-route').
            routeId('abrir-viagem-route').
            process('viagemMessagingMapper').
            process{
                e ->
                    e.setProperty 'dataInicial', DateUtil.formatarData(e.in.body['dataHoraEvento'] as String)
                    def dataInicialViagem = e.in.body['dataInicialViagem']
                    e.setProperty('dataInicialViagem', dataInicialViagem ? dataInicialViagem : e.getProperty('dataInicial'))
            }.
            to('velocity:translators/viagem/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=insert").
        end()

        from('direct:fechar-viagem-route').
            routeId('fechar-viagem-route').
            setProperty('payload',simple('${body}')).
            to('velocity:translators/viagem/consultar-periodo.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataHoraInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=findOneByQuery").
            choice().
                when(simple('${body} != null')).
                    process('processadorDePeriodos').
                    process{
                        e ->
                            e.setProperty 'dataFinal', DateUtil.formatarData(e.getProperty('payload')['dataHoraEvento'] as String)
                    }.
                    to('velocity:translators/viagem/fechar.vm').
                    convertBodyTo(DBObject).
                    to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
                when(simple('${body} == null')).
                      to('direct:fallback-route').
        end()
    }
}
