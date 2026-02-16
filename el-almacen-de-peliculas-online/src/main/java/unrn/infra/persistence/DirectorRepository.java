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

    public DirectorEntity findEntityById(Long id) {
        return em.find(DirectorEntity.class, id);
    }

    public boolean existsByNombreIgnoreCase(String nombre) {
        Long count = em
                .createQuery(
                        "SELECT COUNT(d) FROM DirectorEntity d WHERE LOWER(d.nombre) = LOWER(:nombre)",
                        Long.class)
                .setParameter("nombre", nombre)
                .getSingleResult();
        return count != null && count > 0;
    }

    public List<DirectorEntity> buscarPorNombre(String q, Integer page, Integer size) {
        String normalized = q == null ? "" : q.trim().toLowerCase();
        String filter = "%" + normalized + "%";

        var query = em.createQuery(
                "SELECT d FROM DirectorEntity d WHERE LOWER(d.nombre) LIKE :q ORDER BY d.nombre ASC",
                DirectorEntity.class)
                .setParameter("q", filter);

        if (page != null && size != null && size > 0 && page >= 0) {
            query.setFirstResult(page * size);
            query.setMaxResults(size);
        }

        return query.getResultList();
    }

    @Transactional
    public DirectorEntity guardar(DirectorEntity directorEntity) {
        em.persist(directorEntity);
        em.flush();
        return directorEntity;
    }
}
