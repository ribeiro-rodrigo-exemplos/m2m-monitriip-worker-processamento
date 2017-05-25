package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.util.PeriodCalculator
import com.google.gson.Gson
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import groovy.time.*
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

        def dateFormat = new SimpleDateFormat('yyyy-MM-dd HH:mm:ss')

        def dataHoraFinal = dateFormat.parse(e.getProperty('payload',Map)['dataHoraEvento'])

        def dataHoraInicial = dateFormat.parse e.in.body['dataHoraInicial']

        def duracaoEmMinutos = periodCalculator.calcularPeriodoEmMinutos dataHoraInicial, dataHoraFinal

        def horasPorData = calcularHorasPorData dataHoraInicial,dataHoraFinal

        e.setProperty 'duracao',duracaoEmMinutos
        e.setProperty 'horasPorData',horasPorData
    }

    private calcularHorasPorData(Date dataHoraInicial,Date dataHoraFinal){

        def datas = [:]
        def dataInicial =  new SimpleDateFormat('yyyy-MM-dd').parse(dataHoraInicial.format('yyyy-MM-dd'))
        def dataFinal =  new SimpleDateFormat('yyyy-MM-dd').parse(dataHoraFinal.format('yyyy-MM-dd'))
        def marco = dataHoraInicial.toCalendar().clone() as Calendar

        use(TimeCategory){

            (dataInicial..dataFinal).each{

                def di = marco.clone() as Calendar
                def diff = 24 - marco.get(Calendar.HOUR_OF_DAY)
                marco.add Calendar.HOUR,diff

                if(marco.getTime().after(dataHoraFinal)){
                    marco = dataHoraFinal.toCalendar().clone()
                }

                def periodo = marco.time - di.time

                datas[it.format('yyyy-MM-dd')] = periodo.days ? 24 : periodo.hours
            }
        }

        def gson = new Gson()
        gson.toJson datas
    }
}
