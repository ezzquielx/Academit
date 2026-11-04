package com.academit.education.repository;

import com.academit.education.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    boolean existsByCuotaId(Integer idCuota);

    List<Pago> findByCuotaIdIn(java.util.Collection<Integer> idsCuotas);

    List<Pago> findByCuotaMatriculaAlumnoId(Integer idAlumno);

    List<Pago> findByFechaBetween(LocalDate desde, LocalDate hasta);
}
