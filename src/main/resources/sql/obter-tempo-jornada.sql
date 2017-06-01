select id_cliente,tempo from jornada_de_trabalho as jornada
  where id_cliente=:#${property.payload[idCliente]}