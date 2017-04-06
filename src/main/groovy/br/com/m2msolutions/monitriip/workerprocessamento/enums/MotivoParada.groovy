package br.com.m2msolutions.monitriip.workerprocessamento.enums

/**
 * Created by rodrigo on 28/03/17.
 */
enum MotivoParada {

    PROGRAMADA(0),SOLICITACAO_DE_PASSAGEIRO(1),
    SOLICITACAO_DE_MOTORISTA(2),SOLICITACAO_EXTERNA(3),
    SOLICITACAO_FISCALIZACAO(4),ACIDENTE_NA_VIA(5),
    ACIDENTE_COM_VEICULO(6),ACIDENTE_COM_PASSAGEIRO(7),
    DEFEITO_NO_VEICULO(8),TROCA_PROGRAMADA_DE_VEICULO(9),OUTRO(10);

    private static tipos
    private int codigo

    MotivoParada(cod){
        addTipo this,cod
    }

    private void addTipo(MotivoParada tipo, cod){

        this.codigo = cod

        if(tipos == null)
            tipos = [:]

        tipos[cod] = tipo
    }

    int getCodigo(){
        return codigo
    }

    static MotivoParada obterTipo(cod){
        return tipos[cod]
    }
}