package ch.denic0la.konfi.brunch;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.denic0la.openapi.konfi.brunch.api.BrunchApi;
import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.konfi.brunch.data.Brunch;
import ch.denic0la.konfi.brunch.data.BrunchRepository;
import ch.denic0la.konfi.brunch.data.BrunchService;
import lombok.extern.java.Log;

@RestController
@Log
@RequestMapping("/api")
public class BrunchController implements BrunchApi {

  @Autowired BrunchService brunchService;

  @Autowired BrunchRepository brunchRepository;

  @GetMapping("/ok")
  public String test() {
    log.info("OK endpoint called");
    return "OK";
  }

  @Override
  public ResponseEntity<BrunchInfoDTO> createBrunch(BrunchCreateDTO brunchCreateDTO) {
    // Check if brunch with the same ID already exists
    if (brunchRepository.existsById(brunchCreateDTO.getId())) {
      throw new ResponseStatusException(
          HttpStatusCode.valueOf(409), "Brunch with this ID already exists");
    }
    var brunch = brunchService.brunchCreateDTOToBrunch(brunchCreateDTO);
    log.info(brunch.toString());
    var saved = brunchRepository.save(brunch);
    var data = brunchService.brunchToBrunchInfoDTO(saved);
    return ResponseEntity.status(201).body(data);
  }

  @Override
  public ResponseEntity<List<String>> getAllBrunches() {
    return ResponseEntity.ok(brunchRepository.getAllBrunchIds());
  }

  @Override
  public ResponseEntity<BrunchInfoDTO> getBrunchById(String brunchId) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    Brunch brunch =
        brunchRepository
            .findById(brunchId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Brunch not found"));
    var data = brunchService.brunchToBrunchInfoDTO(brunch);
    return ResponseEntity.ok(data);
  }
}
