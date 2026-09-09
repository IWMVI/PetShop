package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.tutor.exception.EmailJaCadastradoException;
import br.iwmvi.petshop.tutor.mapper.TutorMapper;
import br.iwmvi.petshop.tutor.model.Tutor;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;

    public TutorResponse cadastrar(TutorRequest request) {
        String email = request.email().toLowerCase();

        if (tutorRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException("Email ja cadastrado: " + email);
        }

        Tutor tutor = TutorMapper.toEntity(request);
        tutorRepository.save(tutor);

        return TutorMapper.toResponse(tutor);
    }
}
