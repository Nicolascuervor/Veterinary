package com.agend.agendamientoservice.service;

import com.agend.agendamientoservice.model.HistorialClinico;
import com.agend.agendamientoservice.repository.HistorialClinicoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistorialClinicoService {

    @Autowired
    private HistorialClinicoRepository historialRepo;

    public List<HistorialClinico> obtenerHistorialPorMascota(Long mascotaId) {
        return historialRepo.findByMascotaId(mascotaId);
    }

    public HistorialClinico guardar(HistorialClinico historial) {
        return historialRepo.save(historial);
    }
}