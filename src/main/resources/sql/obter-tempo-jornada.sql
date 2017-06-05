select id_cliente,nu_max_minutos as tempo from jornada_trabalho as jornada
  where id_cliente=:#${property.payload[idCliente]} order by id_jornada_trabalho desc limit 1;
