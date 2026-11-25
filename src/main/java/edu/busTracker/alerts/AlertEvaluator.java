package edu.busTracker.alerts;

import edu.busTracker.dao.AlertSettingsDao;
import edu.busTracker.dao.AllocationDao;
import edu.busTracker.model.AlertSettings;
import edu.busTracker.model.StudentAllocation;
import edu.busTracker.tracking.EtaCalculator;
import edu.busTracker.tracking.EventPublisher;
import edu.busTracker.tracking.LocationEvent;
import edu.busTracker.tracking.LocationEventListener;
import edu.busTracker.tracking.SseBroadcaster;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Event listener evaluating arrival alerts for students on every location update.
 * Section 10:
 *   If student alert is enabled, student's bus has not passed stop,
 *   etaSec <= minutesBefore * 60, and last_fired_date != today -> fire event and update last_fired_date.
 */
@Component
public class AlertEvaluator implements LocationEventListener {

    private static final Logger log = LoggerFactory.getLogger(AlertEvaluator.class);

    private final EventPublisher eventPublisher;
    private final AlertSettingsDao alertSettingsDao;
    private final AllocationDao allocationDao;
    private final EtaCalculator etaCalculator;
    private final SseBroadcaster sseBroadcaster;

    @Autowired
    public AlertEvaluator(EventPublisher eventPublisher, AlertSettingsDao alertSettingsDao,
                          AllocationDao allocationDao, EtaCalculator etaCalculator,
                          SseBroadcaster sseBroadcaster) {
        this.eventPublisher = eventPublisher;
        this.alertSettingsDao = alertSettingsDao;
        this.allocationDao = allocationDao;
        this.etaCalculator = etaCalculator;
        this.sseBroadcaster = sseBroadcaster;
    }

    @PostConstruct
    public void init() {
        eventPublisher.register(this);
    }

    @Override
    public void onLocationUpdate(LocationEvent event) {
        Long busId = event.getLocation().getBusId();
        if (busId == null) return;

        // Find students allocated to this bus
        List<StudentAllocation> allocations = allocationDao.findByBusId(busId);
        String today = LocalDate.now().toString();

        for (StudentAllocation alloc : allocations) {
            Optional<AlertSettings> settingsOpt = alertSettingsDao.findByRegNo(alloc.getRegNo());
            if (settingsOpt.isEmpty()) continue;

            AlertSettings s = settingsOpt.get();
            if (!s.isEnabled()) continue;

            // Check if already fired today
            if (today.equals(s.getLastFiredDate())) continue;

            // Calculate ETA to student's boarding stop
            var etaRes = etaCalculator.eta(busId, alloc.getBus().getRouteId(), alloc.getBoardingStop().getStopId());
            if (etaRes.isPassed()) continue;

            long thresholdSeconds = s.getMinutesBefore() * 60L;
            if (etaRes.getEtaSeconds() <= thresholdSeconds && etaRes.getEtaSeconds() > 0) {
                log.info("Firing arrival alert for student {} on Bus {} (ETA: {}s)",
                        alloc.getRegNo(), alloc.getBus().getBusNumber(), etaRes.getEtaSeconds());

                sseBroadcaster.sendArrivalAlert(alloc.getRegNo(), alloc.getBus().getBusNumber(),
                        alloc.getBoardingStop().getName(), s.getMinutesBefore(), etaRes.getEtaSeconds());

                // Record fired date so it doesn't repeat today
                s.setLastFiredDate(today);
                alertSettingsDao.upsert(s);
            }
        }
    }
}
