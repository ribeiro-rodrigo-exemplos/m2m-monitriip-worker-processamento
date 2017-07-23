select id_cliente,tempo as tempo from direcao_continua
  where id_cliente=:#${property.originalPayload[idCliente]} order by id desc limit 1;