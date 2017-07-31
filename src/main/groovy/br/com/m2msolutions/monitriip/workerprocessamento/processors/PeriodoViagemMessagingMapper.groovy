package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.util.DateUtil
import com.mongodb.DBObject
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.stereotype.Component

/**
 * Created by Rodrigo Ribeiro on 19/07/17.
 */
@Component
class PeriodoViagemMessagingMapper implements Processor {

    @Override
    void process(Exchange e) throws Exception {

        DBObject viagem = e.in.body
        def payload = e.getProperty 'payload'

        e.setProperty 'dataInicial',DateUtil.formatarData(payload['dataHoraEvento'] as String)
        e.setProperty 'dataInicialViagem', viagem['dataInicialViagem']
        e.setProperty 'tipoViagem',viagem['tipoViagem']
        e.setProperty 'tipoTransporte',viagem['tipoTransporte']
        e.setProperty 'sentidoLinha',viagem['sentidoLinha']

        viagem['longitude'] = payload['longitude']
        viagem['latitude'] = payload['latitude']
        viagem['dataHoraEvento'] = payload['dataHoraEvento']
        viagem['cnpjEmpresaTransporte'] = viagem['cnpjCliente']
        viagem['cpfMotorista'] = payload['cpfMotorista']
    }
}
