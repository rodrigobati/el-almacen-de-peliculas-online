package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.Director;

import java.util.List;

@Repository
@Transactional(readOnly = true)
public class DirectorRepository {

    @PersistenceContext
    private EntityManager em;

    // Busca directores por IDs → devuelve DIRECTOR (dominio)
    public List<Director> findAllById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        var entidades = em
                .createQuery("SELECT d FROM DirectorEntity d WHERE d.id IN :ids", DirectorEntity.class)
                .setParameter("ids", ids)
                .getResultList();

        // Convertimos Entity → Domain
        return entidades.stream()
                .map(entity -> entity.asDomain())
                .toList();
    }

    // Por si necesitás uno solo
    public Director findById(Long id) {
        var de = em.find(DirectorEntity.class, id);
        return (de == null) ? null : de.asDomain();
    }
}
