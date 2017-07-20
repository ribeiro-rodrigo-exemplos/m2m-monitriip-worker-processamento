select nm_nomeFuncionario as nome from funcionario
where nm_cpf=:#cpfMotorista order by id_funcionario desc limit 1