package ch.denicola.konfi.brunch.data;

import ch.denic0la.openapi.konfi.brunch.model.BrunchCreateDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchInfoDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionDTO;
import ch.denic0la.openapi.konfi.brunch.model.BrunchQuestionInfoDTO;
import lombok.extern.java.Log;
import org.modelmapper.ModelMapper;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("brunchService")
@Log
public class BrunchService {

    @Autowired
    private ModelMapper modelMapper;

    public BrunchInfoDTO brunchToBrunchInfoDTO(Brunch brunch) {
        var questions = brunch.getQuestions().stream().map(q->{
            var qDTO = new BrunchQuestionInfoDTO();
            qDTO.setId(q.getId());
            qDTO.setMin(q.getMin());
            qDTO.setMax(q.getMax());
            qDTO.setTitle(JsonNullable.of(q.getTitle()));
            try {
                var nullable = JsonNullable.of(q.getLink().toURI());
                qDTO.setLink(nullable);
            } catch (URISyntaxException e) {
                log.warning(e.getMessage());
            }
            qDTO.setOptional(q.getOptional());
            qDTO.setOrder(q.getOrder());
            qDTO.setRecommended(q.getRecommended());
            return qDTO;
        });
        var brunchInfoDTO = new BrunchInfoDTO();
        brunchInfoDTO.setTitle(brunch.getTitle());
        brunchInfoDTO.setId(brunch.getId());
        brunchInfoDTO.setRequireEmail(brunch.getRequireEmail());
        brunchInfoDTO.setEmailRegexp(JsonNullable.of(brunch.getEmailRegexp()));
        brunchInfoDTO.setQuestions(questions.collect(Collectors.toSet()));
        return brunchInfoDTO;
    }

    public Brunch brunchCreateDTOToBrunch(BrunchCreateDTO brunchCreate) {
        if (brunchCreate == null) {
            return null;
        }


        var questions = brunchCreate.getQuestions().stream().map(q -> modelMapper.map(q, Question.class)).toList();

        return Brunch.builder().id(brunchCreate.getId())
                .title(brunchCreate.getTitle())
                .adminPassword(brunchCreate.getAdminPassword().orElse(null))
                .votingPassword(brunchCreate.getVotingPassword().orElse(null))
                .emailRegexp(brunchCreate.getEmailRegexp().orElse(null))
                .requireEmail(brunchCreate.getRequireEmail())
                .questions(questions).build();


    }
}
