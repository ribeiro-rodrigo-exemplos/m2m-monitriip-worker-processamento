package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.util.PeriodCalculator
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import java.text.SimpleDateFormat

/**
 * Created by Rodrigo Ribeiro on 21/04/17.
 */
@Component
class ProcessadorDePeriodos implements Processor {

    @Autowired
    PeriodCalculator periodCalculator

    @Override
    void process(Exchange e) throws Exception {

        def dateFormat = new SimpleDateFormat('yyyy-MM-dd hh:mm:ss')

        def dataHoraFinal = dateFormat.parse(e.getProperty('payload',Map)['dataHoraEvento'])

        def dataHoraInicial = dateFormat.parse e.in.body['dataHoraInicial']

        def duracaoEmMinutos = periodCalculator.calcularPeriodoEmMinutos dataHoraInicial, dataHoraFinal

        e.setProperty 'duracao',duracaoEmMinutos
    }
}
