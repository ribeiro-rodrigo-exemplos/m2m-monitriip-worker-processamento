package br.com.m2msolutions.monitriip.workerprocessamento.enums

/**
 * Created by rodrigo on 28/03/17.
 */
enum Sentido {

    VOLTA(0),IDA(1)

    private static sentidos;

    Sentido(cod){
        addSentido(this,cod)
    }

    private void addSentido(Sentido sentidoLinha, def cod){

        if(sentidos == null)
            sentidos  = [:]

        sentidos[cod] = sentidoLinha
    }

    static Sentido obterSentido(def cod){
        return sentidos[cod]
    }
}