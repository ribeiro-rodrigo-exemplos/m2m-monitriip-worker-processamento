package br.com.m2msolutions.monitriip.workerprocessamento.config

import com.mongodb.Mongo
import com.mongodb.MongoClient
import org.apache.camel.builder.DeadLetterChannelBuilder
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Created by rodrigo on 04/04/17.
 */
@Configuration
class BeansConfig {

    @Bean
    Mongo monitriipDb(){
        def dbConfig = dbConfig()
        new MongoClient(dbConfig.monitriip.host,dbConfig.monitriip.port as Integer)
    }

    @Bean
    Mongo frotaDb(){
        def dbConfig = dbConfig()
        new MongoClient(dbConfig.frota.host,dbConfig.frota.port as Integer)
    }

    @Bean
    DeadLetterChannelBuilder globalDeadLetterChannel(){
        def builder = new DeadLetterChannelBuilder()
        builder.deadLetterUri = 'file:error'
        builder.useOriginalMessage()
        builder
    }

    @Bean
    @ConfigurationProperties('databases')
    Map dbConfig(){
        [:]
    }

    @Bean
    @ConfigurationProperties('rabbitmq')
    Map rabbitConfig(){
        [:]
    }
}
