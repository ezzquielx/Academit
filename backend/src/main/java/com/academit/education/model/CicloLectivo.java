package com.academit.education.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "ciclo_lectivo")
@Getter
@Setter
@NoArgsConstructor
public class CicloLectivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ciclo")
    private Integer id;

    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "nombre", nullable = false, length = 60)
    private String nombre;

    @Column(name = "estado", nullable = false, length = 15)
    private String estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;
}
