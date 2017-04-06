package br.com.m2msolutions.monitriip.workerprocessamento.enums

/**
 * Created by rodrigo on 28/03/17.
 */
enum TipoTransporte {

    REGULAR(7),FRETADO(8)

    private static tipos

    TipoTransporte(cod){
        addTipo(this,cod)
    }

    private void addTipo(TipoTransporte tipoTransporte, cod) {
        if(tipos == null)
            tipos = [:]

        tipos[cod] = tipoTransporte
    }

    static TipoTransporte obterTipo(cod){
        return tipos[cod]
    }
}