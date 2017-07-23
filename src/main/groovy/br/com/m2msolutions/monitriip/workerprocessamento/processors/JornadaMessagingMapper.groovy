package br.com.m2msolutions.monitriip.workerprocessamento.processors

import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 23/07/17.
 */
@Component
class JornadaMessagingMapper implements Processor{
    @Override
    void process(Exchange e) throws Exception {
        def jornada = e.getProperty 'jornada'
        jornada['cnpjCliente'] = jornada['cnpjEmpresaTransporte']
    }
}
