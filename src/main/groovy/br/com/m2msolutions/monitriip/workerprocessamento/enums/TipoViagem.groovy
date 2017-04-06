package br.com.m2msolutions.monitriip.workerprocessamento.enums
/**
 * Created by rodrigo on 28/03/17.
 */
enum TipoViagem {

    REGULAR(0),EXTRA(1)

    private static tipos

    TipoViagem(cod){
        addTipo(this,cod)
    }

    private void addTipo(TipoViagem tipoViagem, cod){
        if(tipos == null)
            tipos = [:]

        tipos[cod] = tipoViagem
    }

    static TipoViagem obterTipo(cod){
        return tipos[cod]
    }

}