package ch.denic0la.konfi.brunch.data;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;

@Service
public class VotingService {

  @Autowired private BrunchRepository brunchRepository;
  @Autowired private VoteRepository voteRepository;
  @Autowired private BrunchMappingService mappingService;
  @Autowired private BrunchValidationService validationService;

  public BrunchVoteDTO submitVote(String brunchId, BrunchVoteDTO voteDTO) {
    // Find the brunch with questions for validation
    Brunch brunch =
        brunchRepository
            .findByIdWithQuestions(brunchId)
            .orElseThrow(() -> new IllegalArgumentException("Brunch not found: " + brunchId));

    // Validate the vote
    validationService.validateVote(voteDTO, brunch);

    // Check for duplicate voter names (business rule)
    if (isVoterNameAlreadyUsed(brunchId, voteDTO.getName())) {
      throw new IllegalArgumentException(
          "Voter name '" + voteDTO.getName() + "' has already been used for this brunch");
    }

    // Convert DTO to entity
    Vote vote = mappingService.brunchVoteDTOToVote(voteDTO, brunch);

    // Save the vote
    Vote savedVote = voteRepository.save(vote);

    // Convert back to DTO for response
    return mappingService.voteToBrunchVoteDTO(savedVote);
  }

  public List<BrunchVoteDTO> getBrunchResults(String brunchId) {
    // Verify brunch exists
    if (!brunchRepository.existsById(brunchId)) {
      throw new IllegalArgumentException("Brunch not found: " + brunchId);
    }

    // Get all votes for the brunch
    List<Vote> votes = voteRepository.findByBrunchId(brunchId);

    // Convert to DTOs
    return votes.stream().map(mappingService::voteToBrunchVoteDTO).collect(Collectors.toList());
  }

  public List<String> getVoterNames(String brunchId) {
    // Verify brunch exists
    if (!brunchRepository.existsById(brunchId)) {
      throw new IllegalArgumentException("Brunch not found: " + brunchId);
    }

    return voteRepository.findByBrunchId(brunchId).stream()
        .map(Vote::getName)
        .collect(Collectors.toList());
  }

  public boolean hasVoted(String brunchId, String voterName) {
    return voteRepository.existsByBrunchIdAndName(brunchId, voterName);
  }

  public long getVoteCount(String brunchId) {
    return voteRepository.countByBrunchId(brunchId);
  }

  public void deleteVote(String brunchId, String voterName) {
    Vote vote =
        voteRepository
            .findByBrunchIdAndName(brunchId, voterName)
            .orElseThrow(
                () ->
                    new IllegalArgumentException(
                        "Vote not found for brunch '"
                            + brunchId
                            + "' and voter '"
                            + voterName
                            + "'"));

    voteRepository.delete(vote);
  }

  public void deleteAllVotes(String brunchId) {
    // Verify brunch exists
    if (!brunchRepository.existsById(brunchId)) {
      throw new IllegalArgumentException("Brunch not found: " + brunchId);
    }

    voteRepository.deleteByBrunchId(brunchId);
  }

  private boolean isVoterNameAlreadyUsed(String brunchId, String voterName) {
    return voteRepository.existsByBrunchIdAndName(brunchId, voterName);
  }
}
