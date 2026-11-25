package edu.busTracker.alternatives;

import edu.busTracker.db.DatabaseInitializer;
import edu.busTracker.model.BusLocation;
import edu.busTracker.tracking.LocationStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "app.db.path=./target/test-db/bustrack_alt_test.db",
        "app.session.store=memory",
        "app.alternatives.allow-cross-route-boarding=true"
})
public class AlternativeBusServiceTest {

    @Autowired
    private AlternativeBusService alternativeBusService;

    @Autowired
    private LocationStore locationStore;

    @Autowired
    private DatabaseInitializer databaseInitializer;

    @BeforeEach
    public void setup() {
        locationStore.clear();
        databaseInitializer.initialize();
    }

    @Test
    @DisplayName("AP26DEMO0001 at Duggirala: Sees TN-3 and TN-4 as alternative buses")
    public void testStudentA_Alternatives() {
        List<AlternativeBusOption> alts = alternativeBusService.findAlternatives("AP26DEMO0001");
        assertNotNull(alts);
        assertEquals(2, alts.size(), "Duggirala is served by TN-2 (allocated), TN-3 and TN-4. Should suggest 2 alternatives.");

        List<String> busNumbers = alts.stream().map(AlternativeBusOption::getBusNumber).toList();
        assertTrue(busNumbers.contains("TN-3"));
        assertTrue(busNumbers.contains("TN-4"));
        assertFalse(busNumbers.contains("TN-2"), "Allocated bus must never be in alternatives");
    }

    @Test
    @DisplayName("AP26DEMO0002 at Ravindrapadu: Sees TN-3 as alternative bus")
    public void testStudentB_Alternatives() {
        List<AlternativeBusOption> alts = alternativeBusService.findAlternatives("AP26DEMO0002");
        assertNotNull(alts);
        assertEquals(1, alts.size(), "Ravindrapadu is served by TN-2 (allocated) and TN-3. Should suggest TN-3 only.");
        assertEquals("TN-3", alts.get(0).getBusNumber());
    }

    @Test
    @DisplayName("AP26DEMO0003 at Kotha Vanthana: 0 alternatives (Correct empty state)")
    public void testStudentC_NoAlternatives() {
        List<AlternativeBusOption> alts = alternativeBusService.findAlternatives("AP26DEMO0003");
        assertNotNull(alts);
        assertTrue(alts.isEmpty(), "Kotha Vanthana is exclusive to TN-3; there must be no alternatives.");
    }

    @Test
    @DisplayName("AP26DEMO0005 at Mangalagiri Old Bus Stand: 0 alternatives (Exclusive to TN-4)")
    public void testStudentE_NoAlternatives() {
        List<AlternativeBusOption> alts = alternativeBusService.findAlternatives("AP26DEMO0005");
        assertNotNull(alts);
        assertTrue(alts.isEmpty(), "Mangalagiri Old Bus Stand is exclusive to TN-4; no alternatives.");
    }

    @Autowired
    private edu.busTracker.dao.BusDao busDao;

    @Autowired
    private edu.busTracker.dao.StopDao stopDao;

    @Test
    @DisplayName("Rule: Exclude alternative bus if it has already passed student's boarding stop")
    public void testPassedBusExcluded() {
        Long tn3BusId = busDao.findByBusNumber("TN-3").orElseThrow().getBusId();
        Long duggiralaStopId = stopDao.findByName("Duggirala").orElseThrow().getStopId();

        locationStore.updateLocation(tn3BusId,
                new BusLocation(tn3BusId, 16.30, 80.55, 35.0, 45.0, Instant.now().toString(), "SIMULATED"),
                "RUNNING",
                Set.of(duggiralaStopId));

        List<AlternativeBusOption> alts = alternativeBusService.findAlternatives("AP26DEMO0001");
        List<String> busNumbers = alts.stream().map(AlternativeBusOption::getBusNumber).toList();

        assertFalse(busNumbers.contains("TN-3"), "TN-3 must be excluded once it has passed Duggirala");
    }
}
