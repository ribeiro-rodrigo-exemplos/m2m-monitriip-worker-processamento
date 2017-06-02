select id_cliente,nu_max_minutos as tempo from direcao_continua
  where id_cliente=:#${property.originalPayload[idCliente]}