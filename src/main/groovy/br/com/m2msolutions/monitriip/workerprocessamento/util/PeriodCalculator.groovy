package br.com.m2msolutions.monitriip.workerprocessamento.util

import org.springframework.stereotype.Component

import java.util.concurrent.TimeUnit

/**
 * Created by Rodrigo Ribeiro on 20/04/17.
 */
@Component
class PeriodCalculator {
    def calcularPeriodoEmMinutos(Date dataInicio,Date dataFim){
        def duracao = dataFim.time - dataInicio.time
        TimeUnit.MILLISECONDS.toMinutes duracao
    }
}
