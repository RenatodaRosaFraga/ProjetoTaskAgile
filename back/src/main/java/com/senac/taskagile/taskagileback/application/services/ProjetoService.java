package com.senac.taskagile.taskagileback.application.services;

import com.senac.taskagile.taskagileback.application.DTO.ProjetoRequest;
import com.senac.taskagile.taskagileback.application.DTO.ProjetoResponse;
import com.senac.taskagile.taskagileback.domain.entities.Projeto;
import com.senac.taskagile.taskagileback.domain.enuns.EnumStatusProjeto;
import com.senac.taskagile.taskagileback.domain.repository.ProjetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjetoService {

    @Autowired
    private ProjetoRepository projetoRepository;

    @Autowired
    private ViaCepService viaCepService;

    public List<ProjetoResponse> ListarTodos() {
        try {
            return projetoRepository.findAll()
                    .stream()
                    .map(ProjetoResponse::new)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ProjetoResponse BuscarProjetoPorId(Long id) {
        try {
            var projeto = projetoRepository.findById(id).orElse(null);
            if (projeto == null) {
                return null;
            }
            return new ProjetoResponse(projeto);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Long SalvarProjeto(ProjetoRequest projetoRequest) {
        try {
            Projeto projeto = new Projeto();
            projeto.setNome(projetoRequest.nome());
            projeto.setPrazo(projetoRequest.prazo());
            projeto.setStatus(EnumStatusProjeto.ATIVO);

            // Busca endereço pelo CEP usando API ViaCEP
            if (projetoRequest.cep() != null && !projetoRequest.cep().isEmpty()) {
                var endereco = viaCepService.buscarEnderecoPorCep(projetoRequest.cep());
                projeto.setCep(endereco.cep());
                projeto.setLogradouro(endereco.logradouro());
                projeto.setComplemento(endereco.complemento());
                projeto.setBairro(endereco.bairro());
                projeto.setLocalidade(endereco.localidade());
                projeto.setUf(endereco.uf());
            }

            return projetoRepository.save(projeto).getId();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean AlterarProjeto(Long id, ProjetoRequest projetoRequest) {
        var projetoBanco = projetoRepository.findById(id).orElse(null);

        if (projetoBanco != null) {
            projetoBanco.setNome(projetoRequest.nome());
            projetoBanco.setPrazo(projetoRequest.prazo());

            // Busca endereço pelo CEP usando API ViaCEP se o CEP foi alterado
            if (projetoRequest.cep() != null && !projetoRequest.cep().isEmpty()) {
                var endereco = viaCepService.buscarEnderecoPorCep(projetoRequest.cep());
                projetoBanco.setCep(endereco.cep());
                projetoBanco.setLogradouro(endereco.logradouro());
                projetoBanco.setComplemento(endereco.complemento());
                projetoBanco.setBairro(endereco.bairro());
                projetoBanco.setLocalidade(endereco.localidade());
                projetoBanco.setUf(endereco.uf());
            }

            projetoRepository.save(projetoBanco);
            return true;
        }

        return false;
    }

    public boolean AlterarStatusProjeto(Long id, EnumStatusProjeto status) {
        var projetoBanco = projetoRepository.findById(id).orElse(null);

        if (projetoBanco != null) {
            projetoBanco.setStatus(status);
            projetoRepository.saveAndFlush(projetoBanco);
            return true;
        }

        return false;
    }
}
