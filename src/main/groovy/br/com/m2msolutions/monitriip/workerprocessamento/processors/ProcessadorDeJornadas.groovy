package br.com.m2msolutions.monitriip.workerprocessamento.processors

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 11/06/17.
 */
@Component
class ProcessadorDeJornadas implements Processor {
    @Override
    void process(Exchange exchange) throws Exception {
        def payload = exchange.getProperty 'payload'
        payload['idLog'] = 7

        if(payload['tipoRegistroEvento'] == 1)
            payload['tipoRegistroViagem'] = 1
        else
            payload['tipoRegistroViagem'] = 0

        exchange.in.body = payload
    }
}
