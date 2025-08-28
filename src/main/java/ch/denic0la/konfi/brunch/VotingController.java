package ch.denic0la.konfi.brunch;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import ch.denic0la.konfi.brunch.data.Brunch;
import ch.denic0la.konfi.brunch.data.BrunchRepository;
import ch.denic0la.konfi.brunch.data.BrunchService;
import ch.denic0la.konfi.brunch.data.Vote;
import ch.denic0la.konfi.brunch.data.VoteRepository;
import ch.denic0la.konfi.brunch.security.BrunchPasswordAuthenticationToken;
import ch.denic0la.openapi.konfi.brunch.api.VotingApi;
import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;
import lombok.extern.java.Log;

@RestController
@Log
@RequestMapping("/api")
public class VotingController implements VotingApi {

  @Autowired private BrunchService brunchService;
  @Autowired private BrunchRepository brunchRepository;
  @Autowired private VoteRepository voteRepository;

  @Override
  public ResponseEntity<Void> createVoteOnBrunchById(String brunchId, BrunchVoteDTO brunchVoteDTO) {
    // Check authentication - voter or admin access required
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof BrunchPasswordAuthenticationToken token) || !token.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(401), "Authentication required");
    }

    // Find brunch with questions for validation
    Brunch brunch =
        brunchRepository
            .findByIdWithQuestions(brunchId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Brunch not found"));

    // Validate vote data
    if (brunchVoteDTO.getName() == null || brunchVoteDTO.getName().trim().isEmpty()) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Vote name is required");
    }

    // Check email requirement
    if (brunch.getRequireEmail() != null && brunch.getRequireEmail()) {
      if (brunchVoteDTO.getEmail() == null
          || !brunchVoteDTO.getEmail().isPresent()
          || brunchVoteDTO.getEmail().get() == null
          || brunchVoteDTO.getEmail().get().trim().isEmpty()) {
        throw new ResponseStatusException(
            HttpStatusCode.valueOf(400), "Email is required for this brunch");
      }

      // Validate email format against regex if provided
      if (brunch.getEmailRegexp() != null && !brunch.getEmailRegexp().trim().isEmpty()) {
        String emailRegex = brunch.getEmailRegexp();
        String email = brunchVoteDTO.getEmail().get();
        if (!email.matches(emailRegex)) {
          throw new ResponseStatusException(
              HttpStatusCode.valueOf(400), "Email does not match required format");
        }
      }
    }

    try {
      // Convert DTO to entity and save
      Vote vote = brunchService.brunchVoteDTOToVote(brunchVoteDTO, brunch);
      voteRepository.save(vote);

      log.info(
          "Vote submitted successfully for brunch: "
              + brunchId
              + " by: "
              + brunchVoteDTO.getName());
      return ResponseEntity.ok().build();
    } catch (IllegalArgumentException e) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
    } catch (Exception e) {
      log.severe("Error saving vote: " + e.getMessage());
      throw new ResponseStatusException(HttpStatusCode.valueOf(500), "Internal server error");
    }
  }

  @Override
  public ResponseEntity<List<BrunchVoteDTO>> getResultsForBrunchById(String brunchId) {
    // Check authentication - admin access required
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (!(auth instanceof BrunchPasswordAuthenticationToken token)
        || !token.isAuthenticated()
        || !token.isAdmin()) {
      throw new ResponseStatusException(HttpStatusCode.valueOf(403), "Admin access required");
    }

    // Find brunch with votes for results
    Brunch brunch =
        brunchRepository
            .findByIdWithQuestionsAndVotes(brunchId)
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatusCode.valueOf(404), "Brunch not found"));

    // Get all votes for this brunch and convert to DTOs
    List<BrunchVoteDTO> results =
        brunch.getVotes().stream()
            .map(brunchService::voteToBrunchVoteDTO)
            .collect(Collectors.toList());

    log.info("Retrieved " + results.size() + " voting results for brunch: " + brunchId);
    return ResponseEntity.ok(results);
  }
}
