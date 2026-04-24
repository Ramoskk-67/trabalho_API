package br.com.mensageria.repository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.mensageria.entity.Evento;

import java.util.List;
/**
* JpaRepository<Entity, TipoDaChavePrimaria> já traz save, findById, findAll, delete..
* Você declara apenas o que for específico do domínio.
*/
public interface EventoRepository extends JpaRepository<Evento, Long> {
/**
* JPQL: consulta orientada a objetos (nomes de classe/campo, não tabela SQL).
* Pageable permite "LIMIT" — aqui: só o primeiro resultado (fila).
*/
@Query("SELECT e FROM Evento e WHERE e.status = :status ORDER BY e.criadoEm ASC")
List<Evento> findPendentesOrdenadosPorCriacao(
@Param("status") Evento.StatusEvento status,
Pageable pageable
);
/**
* Método derivado pelo nome: Spring Data monta a query automaticamente.
*/
List<Evento> findByStatusOrderByCriadoEmAsc(Evento.StatusEvento status);
}