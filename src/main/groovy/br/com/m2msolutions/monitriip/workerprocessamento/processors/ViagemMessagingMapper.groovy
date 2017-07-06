package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.enums.Sentido
import br.com.m2msolutions.monitriip.workerprocessamento.enums.TipoTransporte
import br.com.m2msolutions.monitriip.workerprocessamento.enums.TipoViagem
import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
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

        def payload  = e.in.body

        e.setProperty 'tipoViagem',payload['codigoTipoViagem'] != null ? TipoViagem.obterTipo(payload['codigoTipoViagem'] as Integer)
                : TipoViagem.obterTipo(2)
        e.setProperty 'sentidoLinha',Sentido.obterSentido(payload['codigoSentidoLinha'] ? payload['codigoSentidoLinha'] : payload['sentidoLinha'])
        e.setProperty 'tipoTransporte',TipoTransporte.obterTipo(payload['idLog'])
    }
}

