package com.foodfast.drone_service.service;

import com.foodfast.drone_service.model.Drone;
import com.foodfast.drone_service.repository.DroneRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DroneServiceUnitTest {

    private DroneRepository droneRepository;
    private DroneService droneService;

    @BeforeEach
    void setUp() {
        droneRepository = mock(DroneRepository.class);
        droneService = new DroneServiceImpl(droneRepository);
    }

    @Test
    void testCreateDrone() {
        Drone drone = new Drone();
        drone.setName("Drone A");

        Drone saved = new Drone();
        saved.setId(1L);
        saved.setName("Drone A");
        saved.setStatus("AVAILABLE");

        when(droneRepository.save(any(Drone.class))).thenReturn(saved);

        Drone result = droneService.createDrone(drone);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("AVAILABLE", result.getStatus());
        verify(droneRepository, times(1)).save(any(Drone.class));
    }

    @Test
    void testGetAllDrones() {
        Drone d1 = new Drone(); d1.setId(1L);
        Drone d2 = new Drone(); d2.setId(2L);

        when(droneRepository.findAll()).thenReturn(List.of(d1, d2));

        List<Drone> result = droneService.getAllDrones();

        assertEquals(2, result.size());
        verify(droneRepository, times(1)).findAll();
    }

    @Test
    void testGetDroneById() {
        Drone drone = new Drone();
        drone.setId(10L);
        drone.setName("Test Drone");

        when(droneRepository.findById(10L)).thenReturn(Optional.of(drone));

        Drone result = droneService.getDroneById(10L).orElse(null);

        assertNotNull(result);
        assertEquals("Test Drone", result.getName());
        verify(droneRepository, times(1)).findById(10L);
    }

    @Test
    void testUpdateDrone() {
        Drone existing = new Drone();
        existing.setId(5L);
        existing.setName("Old");
        existing.setModel("M1");

        Drone update = new Drone();
        update.setName("New");
        update.setModel("M2");
        update.setStatus("BUSY");
        update.setBatteryLevel(90);
        update.setLatitude(10.0);
        update.setLongitude(106.0);

        when(droneRepository.findById(5L)).thenReturn(Optional.of(existing));
        when(droneRepository.save(any(Drone.class))).thenReturn(existing);

        Drone result = droneService.updateDrone(5L, update);

        assertEquals("New", result.getName());
        assertEquals("M2", result.getModel());
        assertEquals("BUSY", result.getStatus());
        assertEquals(90, result.getBatteryLevel());
        verify(droneRepository, times(1)).save(existing);
    }

    @Test
    void testUpdateDrone_NotFound() {
        when(droneRepository.findById(100L)).thenReturn(Optional.empty());

        Drone update = new Drone();

        assertThrows(RuntimeException.class, () -> {
            droneService.updateDrone(100L, update);
        });

        verify(droneRepository, never()).save(any());
    }

    @Test
    void testDeleteDrone() {
        droneService.deleteDrone(3L);

        verify(droneRepository, times(1)).deleteById(3L);
    }

    @Test
    void testGetDronesByStatus() {
        Drone d1 = new Drone(); d1.setStatus("AVAILABLE");

        when(droneRepository.findByStatus("AVAILABLE")).thenReturn(List.of(d1));

        List<Drone> result = droneService.getDronesByStatus("AVAILABLE");

        assertEquals(1, result.size());
        assertEquals("AVAILABLE", result.get(0).getStatus());
        verify(droneRepository, times(1)).findByStatus("AVAILABLE");
    }
}
