package com.senac.taskagile.taskagileback.domain.repository;

import com.senac.taskagile.taskagileback.domain.entities.Projeto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetoRepository extends JpaRepository<Projeto,Long> {

}
