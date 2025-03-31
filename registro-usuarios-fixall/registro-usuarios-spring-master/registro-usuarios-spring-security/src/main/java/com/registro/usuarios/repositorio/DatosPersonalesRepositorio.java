package com.registro.usuarios.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;

import com.registro.usuarios.modelo.DatosPersonales;


public interface DatosPersonalesRepositorio extends JpaRepository<DatosPersonales, Long> {

}
