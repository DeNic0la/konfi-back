package ch.denic0la.konfi.brunch.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchUpdateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;

@Service("brunchService")
public class BrunchService {

  @Autowired private BrunchManagementService managementService;
  @Autowired private VotingService votingService;
  @Autowired private BrunchMappingService mappingService;

  // Delegated methods for backward compatibility
  public BrunchInfoDTO createBrunch(BrunchCreateDTO createDTO) {
    return managementService.createBrunch(createDTO);
  }

  public BrunchInfoDTO getBrunch(String brunchId) {
    return managementService.getBrunch(brunchId);
  }

  public BrunchInfoDTO updateBrunch(String brunchId, BrunchUpdateDTO updateDTO) {
    return managementService.updateBrunch(brunchId, updateDTO);
  }

  public void deleteBrunch(String brunchId) {
    managementService.deleteBrunch(brunchId);
  }

  public BrunchVoteDTO submitVote(String brunchId, BrunchVoteDTO voteDTO) {
    return votingService.submitVote(brunchId, voteDTO);
  }

  public java.util.List<BrunchVoteDTO> getBrunchResults(String brunchId) {
    return votingService.getBrunchResults(brunchId);
  }

  // Direct mapping methods for backward compatibility
  public BrunchInfoDTO brunchToBrunchInfoDTO(Brunch brunch) {
    return mappingService.brunchToBrunchInfoDTO(brunch);
  }

  public Brunch brunchCreateDTOToBrunch(BrunchCreateDTO brunchCreate) {
    return mappingService.brunchCreateDTOToBrunch(brunchCreate);
  }

  public Vote brunchVoteDTOToVote(BrunchVoteDTO voteDTO, Brunch brunch) {
    return mappingService.brunchVoteDTOToVote(voteDTO, brunch);
  }

  public BrunchVoteDTO voteToBrunchVoteDTO(Vote vote) {
    return mappingService.voteToBrunchVoteDTO(vote);
  }

  public BrunchQuestionInfoDTO questionToBrunchQuestionInfoDTO(Question question) {
    return mappingService.questionToBrunchQuestionInfoDTO(question);
  }
}
