package br.iwmvi.petshop.tutor.service;

import br.iwmvi.petshop.tutor.dto.request.TutorRequest;
import br.iwmvi.petshop.tutor.dto.response.TutorResponse;
import br.iwmvi.petshop.exception.EmailJaCadastradoException;
import br.iwmvi.petshop.exception.TutorNotFoundException;
import br.iwmvi.petshop.tutor.mapper.TutorMapper;
import br.iwmvi.petshop.tutor.model.Tutor;
import br.iwmvi.petshop.tutor.repository.TutorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<TutorResponse> listar() {
        return tutorRepository.findAllByDeletedAtIsNull().stream()
                .map(TutorMapper::toResponse)
                .toList();
    }

    public TutorResponse buscarPorId(Long id) {
        Tutor tutor = tutorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new TutorNotFoundException(id));

        return TutorMapper.toResponse(tutor);
    }

    public TutorResponse atualizar(Long id, TutorRequest request) {
        Tutor tutor = tutorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new TutorNotFoundException(id));

        String email = request.email().toLowerCase();

        if (!email.equals(tutor.getEmail()) && tutorRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException("Email ja cadastrado: " + email);
        }

        TutorMapper.atualizarEntidade(tutor, request);
        tutorRepository.save(tutor);

        return TutorMapper.toResponse(tutor);
    }

    public void excluir(Long id) {
        Tutor tutor = tutorRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new TutorNotFoundException(id));

        tutor.setDeletedAt(LocalDateTime.now());
        tutorRepository.save(tutor);
    }

    public TutorResponse restaurar(Long id) {
        Tutor tutor = tutorRepository.findById(id)
                .orElseThrow(() -> new TutorNotFoundException(id));

        tutor.setDeletedAt(null);
        tutorRepository.save(tutor);

        return TutorMapper.toResponse(tutor);
    }
}
