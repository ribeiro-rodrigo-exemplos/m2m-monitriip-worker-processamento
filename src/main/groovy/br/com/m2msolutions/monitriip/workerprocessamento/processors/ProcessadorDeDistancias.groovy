package br.com.m2msolutions.monitriip.workerprocessamento.processors

import br.com.m2msolutions.monitriip.workerprocessamento.util.DistanceCalculator
import org.apache.camel.Exchange
import org.apache.camel.Processor
import org.springframework.stereotype.Component
/**
 * Created by Rodrigo Ribeiro on 20/04/17.
 */
@Component
class ProcessadorDeDistancias implements Processor {
    @Override
    void process(Exchange e) throws Exception {

        def latitudeInicial = e.in.body['localizacaoInicial']['coordinates'][0] as Double
        def longitudeInicial = e.in.body['localizacaoInicial']['coordinates'][1] as Double

        def latitudeAtual = e.getProperty('originalPayload')['latitude'] as Double
        def longitudeAtual = e.getProperty('originalPayload')['longitude'] as Double

        def distancia = DistanceCalculator.distance latitudeAtual,longitudeAtual,latitudeInicial,longitudeInicial,'K'
        e.setProperty 'distanciaPercorrida',distancia
    }
}
