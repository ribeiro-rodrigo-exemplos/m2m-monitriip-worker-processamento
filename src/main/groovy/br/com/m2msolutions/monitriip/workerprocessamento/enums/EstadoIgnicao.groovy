package br.com.m2msolutions.monitriip.workerprocessamento.enums

/**
 * Created by Rodrigo Ribeiro on 08/04/17.
 */
enum EstadoIgnicao {

    DESLIGADA(0),LIGADA(1)

    private static estados;

    EstadoIgnicao(cod){
        addEstado(this,cod)
    }

    private void addEstado(EstadoIgnicao estadoIgnicao, cod){

        if(estados == null)
            estados  = [:]

        estados[cod] = estadoIgnicao
    }

    static EstadoIgnicao obterEstado(cod){
        return estados[cod]
    }

}