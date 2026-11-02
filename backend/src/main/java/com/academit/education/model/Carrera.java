package com.academit.education.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "carrera")
@Getter
@Setter
@NoArgsConstructor
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "titulo_otorgado", nullable = false, length = 150)
    private String tituloOtorgado;

    @Column(name = "duracion_anios", nullable = false)
    private Integer duracionAnios;

    @Column(name = "resolucion", length = 100)
    private String resolucion;

    @Column(name = "estado", nullable = false, length = 10)
    private String estado;
}
