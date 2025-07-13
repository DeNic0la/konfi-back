package ch.denicola.konfi.brunch;


import ch.denic0la.openapi.konfi.brunch.api.BrunchApi;
import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denicola.konfi.brunch.data.Brunch;
import ch.denicola.konfi.brunch.data.BrunchRepository;
import ch.denicola.konfi.brunch.data.BrunchService;
import jakarta.annotation.Resource;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Log
@RequestMapping("/api")
public class BrunchController implements BrunchApi {


    @Autowired BrunchService brunchService;

    @Autowired BrunchRepository brunchRepository;

    @GetMapping("/ok")
    public String test(){
        log.info("OK endpoint called");
        return "OK";
    }

    @Override
    public ResponseEntity<BrunchInfoDTO> createBrunch(BrunchCreateDTO brunchCreateDTO) {
        // Check if brunch with the same ID already exists
        if (brunchRepository.existsById(brunchCreateDTO.getId())) {
            return ResponseEntity.status(409).body(null); // Conflict
        }
        var brunch = brunchService.brunchCreateDTOToBrunch(brunchCreateDTO);
        log.info(brunch.toString());
        var saved = brunchRepository.save(brunch);
        var data = brunchService.brunchToBrunchInfoDTO(saved);
        return ResponseEntity.status(201).body(data);
    }
}
