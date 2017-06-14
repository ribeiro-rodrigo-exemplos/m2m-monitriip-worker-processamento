package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.exceptions.ViagemNaoEncontradaException
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

        def viagem = exchange.in.body

        if(!viagem)
            throw new ViagemNaoEncontradaException('Viagem não encontrada')

        def payload = exchange.getProperty 'payload'
        payload['idLog'] = 7

        if(payload['tipoRegistroEvento'] == 1)
            payload['tipoRegistroViagem'] = 1
        else
            payload['tipoRegistroViagem'] = 0

        payload['codigoTipoViagem'] = viagem['codigoTipoViagem']
        payload['codigoSentidoLinha'] = viagem['codigoSentidoLinha']
        payload['identificacaoLinha'] = viagem['identificacaoLinha']
        payload['tipoTransporte'] = viagem['idLog']

        exchange.in.body = payload
    }


}
