package ch.denic0la.konfi.brunch;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.denic0la.openapi.konfi.brunch.api.VotingApi;
import ch.denic0la.openapi.konfi.brunch.model.BrunchVoteDTO;
import lombok.extern.java.Log;

@RestController
@Log
@RequestMapping("/api")
public class VotingController implements VotingApi {
  @Override
  public ResponseEntity<Void> createVoteOnBrunchById(String brunchId, BrunchVoteDTO brunchVoteDTO) {

    return VotingApi.super.createVoteOnBrunchById(brunchId, brunchVoteDTO);
  }
}
