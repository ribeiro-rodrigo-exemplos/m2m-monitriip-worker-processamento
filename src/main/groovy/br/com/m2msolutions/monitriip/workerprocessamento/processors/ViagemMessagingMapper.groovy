package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.enums.Sentido
import br.com.m2msolutions.monitriip.workerprocessamento.enums.TipoTransporte
import br.com.m2msolutions.monitriip.workerprocessamento.enums.TipoViagem
import com.mongodb.DBObject
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 20/04/17.
 */
@Component
class ViagemMessagingMapper implements Processor{

    @Override
    void process(Exchange e) throws Exception {

        def payload  = e.getProperty 'payload',DBObject

        e.setProperty 'tipoViagem',TipoViagem.obterTipo(payload['codigoTipoViagem'])
        e.setProperty 'sentidoLinha',Sentido.obterSentido(payload['codigoSentidoLinha'])
        e.setProperty 'tipoTransporte',TipoTransporte.obterTipo(payload['idLog'])
    }
}
