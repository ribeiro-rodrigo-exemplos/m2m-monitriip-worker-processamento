package br.com.m2msolutions.monitriip.workerprocessamento.config

import com.mongodb.Mongo
import com.mongodb.MongoClient
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource
import org.apache.camel.builder.DeadLetterChannelBuilder
import org.apache.camel.component.rabbitmq.ArgsConfigurer
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

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
    @Primary
    @ConfigurationProperties(prefix = "databases.sso")
    MysqlConnectionPoolDataSource ssoDb(){
        new MysqlConnectionPoolDataSource()
    }

    @Bean
    @ConfigurationProperties(prefix = "databases.frota")
    MysqlConnectionPoolDataSource frotaDb(){
        new MysqlConnectionPoolDataSource()
    }

    @Bean
    @ConfigurationProperties('databases')
    Map dbConfig(){
        [:]
    }
}
