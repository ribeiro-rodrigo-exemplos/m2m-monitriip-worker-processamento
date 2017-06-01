select id_cliente,tempo from direcao_continua
  where id_cliente=:#${property.payload[idCliente]}