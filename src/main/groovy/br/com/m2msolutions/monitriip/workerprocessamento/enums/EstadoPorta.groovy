package br.com.m2msolutions.monitriip.workerprocessamento.enums

/**
 * Created by Rodrigo Ribeiro on 08/04/17.
 */
enum EstadoPorta {

    FECHADA(0),ABERTA(1)

    private static estados;

    EstadoPorta(cod){
        addEstado(this,cod)
    }

    private void addEstado(EstadoPorta estadoPorta, cod){

        if(estados == null)
            estados  = [:]

        estados[cod] = estadoPorta
    }

    static EstadoPorta obterEstado(cod){
        return estados[cod]
    }

}