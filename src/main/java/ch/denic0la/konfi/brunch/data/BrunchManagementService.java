package ch.denic0la.konfi.brunch.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchUpdateDTO;

@Service
public class BrunchManagementService {

  @Autowired private BrunchRepository brunchRepository;
  @Autowired private BrunchMappingService mappingService;
  @Autowired private BrunchValidationService validationService;

  public BrunchInfoDTO createBrunch(BrunchCreateDTO createDTO) {
    validationService.validateBrunchCreate(createDTO);

    Brunch brunch = mappingService.brunchCreateDTOToBrunch(createDTO);
    Brunch savedBrunch = brunchRepository.save(brunch);

    return mappingService.brunchToBrunchInfoDTO(savedBrunch);
  }

  public BrunchInfoDTO getBrunch(String brunchId) {
    Brunch brunch =
        brunchRepository
            .findByIdWithQuestions(brunchId)
            .orElseThrow(() -> new IllegalArgumentException("Brunch not found: " + brunchId));

    return mappingService.brunchToBrunchInfoDTO(brunch);
  }

  public BrunchInfoDTO updateBrunch(String brunchId, BrunchUpdateDTO updateDTO) {
    validationService.validateBrunchUpdate(updateDTO);

    Brunch existingBrunch =
        brunchRepository
            .findByIdWithQuestions(brunchId)
            .orElseThrow(() -> new IllegalArgumentException("Brunch not found: " + brunchId));

    Brunch updatedBrunch = mappingService.updateBrunch(existingBrunch, updateDTO);
    Brunch savedBrunch = brunchRepository.save(updatedBrunch);

    return mappingService.brunchToBrunchInfoDTO(savedBrunch);
  }

  public void deleteBrunch(String brunchId) {
    if (!brunchRepository.existsById(brunchId)) {
      throw new IllegalArgumentException("Brunch not found: " + brunchId);
    }

    brunchRepository.deleteById(brunchId);
  }

  public boolean brunchExists(String brunchId) {
    return brunchRepository.existsById(brunchId);
  }
}
