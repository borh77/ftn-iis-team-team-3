package com.example.iisdrugcrm.service.sales;

import com.example.iisdrugcrm.domain.sales.SalesActivity;
import com.example.iisdrugcrm.domain.sales.SalesProcess;
import com.example.iisdrugcrm.dto.sales.activity.ActivityRequestDTO;
import com.example.iisdrugcrm.dto.sales.activity.ActivityResponseDTO;
import com.example.iisdrugcrm.repository.sales.SalesActivityRepository;
import com.example.iisdrugcrm.repository.sales.SalesProcessRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SalesActivityService {

    private final SalesActivityRepository activityRepository;
    private final SalesProcessRepository salesProcessRepository;

    public SalesActivityService(
            SalesActivityRepository activityRepository,
            SalesProcessRepository salesProcessRepository
    ) {
        this.activityRepository = activityRepository;
        this.salesProcessRepository = salesProcessRepository;
    }

    public ActivityResponseDTO create(Long processId, ActivityRequestDTO dto) {

        SalesProcess process = salesProcessRepository.findById(processId)
                .orElseThrow(() -> new IllegalArgumentException("Sales process not found."));

        SalesActivity activity = new SalesActivity(
                dto.getType(),
                dto.getTitle(),
                dto.getDescription(),
                dto.getScheduledAt(),
                process
        );

        return mapToDto(activityRepository.save(activity));
    }

    public List<ActivityResponseDTO> getByProcess(Long processId) {
        return activityRepository.findBySalesProcessId(processId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public ActivityResponseDTO complete(Long id) {

        SalesActivity activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found."));

        activity.complete();

        return mapToDto(activityRepository.save(activity));
    }

    private ActivityResponseDTO mapToDto(SalesActivity activity) {
        return new ActivityResponseDTO(
                activity.getId(),
                activity.getType(),
                activity.getStatus(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getScheduledAt(),
                activity.getCompletedAt()
        );
    }
}