package br.com.m2msolutions.monitriip.workerprocessamento.routes

import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.JornadaNaoEncontradaException
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
class JornadaRoute extends RouteBuilder {

    @Autowired
    @Qualifier('dbConfig')
    def dbConfig

    @Override
    void configure() throws Exception {

        onException(JornadaNaoEncontradaException).
            log(LoggingLevel.WARN,"${this.class.simpleName}",'${exception.message} - id: ${id}').
            maximumRedeliveries(0).
            logExhaustedMessageHistory(false).
            useOriginalMessage().
            to("direct:fallback-route").
        end()

        from("direct:jornada-route").
            routeId('jornada-route').
            choice().
                when().jsonpath('$[?(@.tipoRegistroEvento == 1)]').
                    to('direct:abrir-jornada-route').
                when().jsonpath('$[?(@.tipoRegistroEvento == 0)]').
                    to('direct:fechar-jornada-route').
            endChoice().
        end()

        from('direct:abrir-jornada-route').
            routeId('abrir-jornada').
            convertBodyTo(Map).
            to('velocity:translators/jornada/abrir.vm').
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=insert").
        end()

        from('direct:fechar-jornada-route').
            routeId('fechar-jornada').
            convertBodyTo(Map).
            setProperty('payload',simple('${body}')).
            to("velocity:translators/jornada/consultar-jornada.vm").
            setHeader(MongoDbConstants.FIELDS_FILTER,constant('{dataInicial:1,_id:0}')).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=findOneByQuery").
            process({
                if(!it.in.body){
                    def message = "Jornada ${it.getProperty('payload')['idJornada']} não foi encontrada."
                    throw new JornadaNaoEncontradaException(message)
                }
            }).
            process('processadorDePeriodos').
            to('velocity:translators/jornada/fechar.vm').
            convertBodyTo(DBObject).
            to("mongodb:monitriipDb?database=${dbConfig.monitriip.database}&collection=jornada&operation=update").
        end()
    }
}
