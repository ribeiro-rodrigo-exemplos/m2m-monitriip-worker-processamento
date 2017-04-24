package br.com.m2msolutions.monitriip.workerprocessamento.util

import java.text.SimpleDateFormat

/**
 * Created by Rodrigo Ribeiro on 24/04/17.
 */
class DateUtil {

    static private formato = 'yyyy-MM-dd'

    static String formatarData(String data){
        new SimpleDateFormat(formato)
                .parse(data)
                .format(formato)
    }
}
