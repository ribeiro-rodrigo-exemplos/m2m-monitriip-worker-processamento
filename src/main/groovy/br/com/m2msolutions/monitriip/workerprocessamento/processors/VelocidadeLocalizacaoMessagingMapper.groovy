package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.enums.EstadoIgnicao
import br.com.m2msolutions.monitriip.workerprocessamento.enums.EstadoPorta
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 20/04/17.
 */
@Component
class VelocidadeLocalizacaoMessagingMapper implements Processor {
    @Override
    void process(Exchange e) throws Exception {
        def originalPayload = e.getProperty 'originalPayload'
        e.setProperty 'situacaoIgnicaoMotor',EstadoIgnicao.obterEstado(originalPayload['situacaoIgnicaoMotor'])
        e.setProperty 'situacaoPortaVeiculo',EstadoPorta.obterEstado(originalPayload['situacaoPortaVeiculo'])
    }
}
