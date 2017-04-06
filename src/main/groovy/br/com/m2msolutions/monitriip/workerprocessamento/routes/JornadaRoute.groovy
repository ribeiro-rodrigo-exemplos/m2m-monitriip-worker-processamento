package br.com.m2msolutions.monitriip.workerprocessamento.routes

import com.mongodb.DBObject
import org.apache.camel.builder.DeadLetterChannelBuilder
import org.apache.camel.builder.RouteBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Created by rodrigo on 03/04/17.
 */
@Component
class JornadaRoute extends RouteBuilder {

    @Autowired
    DeadLetterChannelBuilder globalDeadLetterChannel

    @Override
    void configure() throws Exception {

        //errorHandler(globalDeadLetterChannel)

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
            transform(simple('resource:classpath:jornada/abrir.bson')).
            to('mongodb:monitriipDb?database=test&collection=jornada&operation=insert').
                log('Jornada ID ${body[_id]}').
        end()

        from('direct:fechar-jornada-route').
            routeId('fechar-jornada').
            transform(simple('resource:classpath:jornada/fechar.bson')).
            convertBodyTo(DBObject).
            to('mongodb:monitriipDb?database=test&collection=jornada&operation=update').
            process({
                if(!it.in.body["matchedCount"])
                    throw new RuntimeException('Jornada não encontrada')
            }).
        end()
    }
}
