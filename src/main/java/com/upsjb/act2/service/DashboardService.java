package com.upsjb.act2.service;

import com.upsjb.act2.model.DashboardResumen;
import com.upsjb.act2.repository.DashboardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResumen obtenerResumen() {
        return dashboardRepository.getResumen();
    }
}