package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.JornadaNaoEncontradaException
import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.ViagemNaoEncontradaException
import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
import com.mongodb.DBObject
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
class ViagemRoute extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Autowired
    @Qualifier('rabbitConfig')
    def rcfg

    @Override
    void configure() throws Exception {

        onException(JornadaNaoEncontradaException,ViagemNaoEncontradaException).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${exception.message} - id: ${id}').
            logExhaustedMessageHistory(false).
            maximumRedeliveries(0).
            useOriginalMessage().
            to("direct:fallback-route").
        end()

        from('direct:viagem-route').
            routeId('viagem-route').
            choice().
                when().expression(simple('${body[tipoRegistroViagem]} == 1')).
                    to('direct:abrir-viagem-route').
                when().expression(simple('${body[tipoRegistroViagem]} == 0')).
                    to('direct:fechar-viagem-route').
            endChoice().
        end()

        from('direct:abrir-viagem-route').
            routeId('abrir-viagem-route').
            setProperty('payload',simple('${body}')).
            to('velocity:translators/jornada/consultar-jornada.vm').
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{"cpfMotorista":1}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
            process{
                e ->
                    if(!e.in.body){
                        def message = "Jornada ${e.getProperty('payload')['idJornada']} não foi encontrada."
                        throw new JornadaNaoEncontradaException(message)
                    }
            }.
            process('viagemMessagingMapper').
            process{
                e ->
                    e.setProperty 'dataInicial', DateUtil.formatarData(e.getProperty('payload')['dataHoraEvento'] as String)
                    def dataInicialViagem = e.getProperty('payload')['dataInicialViagem']
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
            process{
                if(!it.in.body){
                    def message = "Viagem ${it.getProperty('payload')['idViagem']} não foi encontrada."
                    throw new ViagemNaoEncontradaException(message)
                }
            }.
            process('processadorDePeriodos').
            process{
                e ->
                    e.setProperty 'dataFinal', DateUtil.formatarData(e.getProperty('payload')['dataHoraEvento'] as String)
            }.
            to('velocity:translators/viagem/fechar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=viagem&operation=update").
        end()
    }
}
