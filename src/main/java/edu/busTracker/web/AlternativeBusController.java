package edu.busTracker.web;

import edu.busTracker.alternatives.AlternativeBusOption;
import edu.busTracker.alternatives.AlternativeBusService;
import edu.busTracker.session.SessionData;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class AlternativeBusController {

    private final AlternativeBusService alternativeBusService;

    @Autowired
    public AlternativeBusController(AlternativeBusService alternativeBusService) {
        this.alternativeBusService = alternativeBusService;
    }

    @GetMapping("/alternatives")
    public ResponseEntity<List<AlternativeBusOption>> getAlternatives(HttpServletRequest request) {
        SessionData session = (SessionData) request.getAttribute("session");
        String regNo = session.getRegNoOrEmail();
        List<AlternativeBusOption> alternatives = alternativeBusService.findAlternatives(regNo);
        return ResponseEntity.ok(alternatives);
    }
}
