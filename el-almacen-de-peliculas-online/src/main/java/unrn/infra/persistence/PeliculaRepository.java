package unrn.infra.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import unrn.model.Actor;
import unrn.model.Director;
import unrn.model.Pelicula;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
@Transactional(readOnly = true)
public class PeliculaRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Pelicula guardar(Pelicula p) {
        var directores = new ArrayList<DirectorEntity>();
        for (Director d : p.directores()) {
            DirectorEntity de = findDirectorPorNombre(d.nombre());
            if (de == null)
                de = new DirectorEntity(d.nombre());
            directores.add(de);
        }

        var actores = new ArrayList<ActorEntity>();
        for (Actor a : p.actores()) {
            ActorEntity ae = findActorPorNombre(a.nombre());
            if (ae == null)
                ae = new ActorEntity(a.nombre());
            actores.add(ae);
        }

        // find or create catalog entries
        CondicionEntity ce = findCondicionPorNombre(p.condicion().toString());
        if (ce == null) {
            ce = new CondicionEntity(p.condicion().toString());
            em.persist(ce);
        }
        FormatoEntity fe = findFormatoPorNombre(p.formato().tipo());
        if (fe == null) {
            fe = new FormatoEntity(p.formato().tipo());
            em.persist(fe);
        }
        GeneroEntity ge = findGeneroPorNombre(p.genero().nombre());
        if (ge == null) {
            ge = new GeneroEntity(p.genero().nombre());
            em.persist(ge);
        }

        PeliculaEntity pe = new PeliculaEntity(
                p.titulo(),
                ce,
                BigDecimal.valueOf(p.precio()),
                fe,
                ge,
                p.sinopsis(),
                p.imagenUrl(),
                p.fechaSalida(),
                directores,
                actores,
                p.rating());

        em.persist(pe);
        return pe.asDomain();
    }

    public Pelicula porId(Long id) {
        PeliculaEntity pe = em.find(PeliculaEntity.class, id);
        return (pe == null) ? null : pe.asDomain();
    }

    public java.util.List<Pelicula> buscarPorTitulo(String q) {
        var list = em.createNamedQuery("PeliculaEntity.buscarPorTitulo", PeliculaEntity.class)
                .setParameter("q", q).getResultList();
        return list.stream().map(PeliculaEntity::asDomain).toList();
    }

    public java.util.List<Pelicula> buscarPorGenero(String genero) {
        var list = em.createNamedQuery("PeliculaEntity.buscarPorGenero", PeliculaEntity.class)
                .setParameter("genero", genero).getResultList();
        return list.stream().map(PeliculaEntity::asDomain).toList();
    }

    public java.util.List<Pelicula> buscarPorActor(String actor) {
        var list = em.createNamedQuery("PeliculaEntity.buscarPorActor", PeliculaEntity.class)
                .setParameter("actor", actor).getResultList();
        return list.stream().map(PeliculaEntity::asDomain).toList();
    }

    public java.util.List<Pelicula> buscarPorDirector(String director) {
        var list = em.createNamedQuery("PeliculaEntity.buscarPorDirector", PeliculaEntity.class)
                .setParameter("director", director).getResultList();
        return list.stream().map(PeliculaEntity::asDomain).toList();
    }

    public long contarPorGenero(String genero) {
        return em.createNamedQuery("PeliculaEntity.contarPorGenero", Long.class)
                .setParameter("genero", genero).getSingleResult();
    }

    public boolean existePorTitulo(String titulo) {
        long c = em.createNamedQuery("PeliculaEntity.existePorTitulo", Long.class)
                .setParameter("titulo", titulo).getSingleResult();
        return c > 0;
    }

    public PageResult<Pelicula> buscarPaginado(
            String q,
            String genero,
            String formato,
            String condicion,
            String actor,
            String director,
            LocalDate desde,
            LocalDate hasta,
            BigDecimal minPrecio,
            BigDecimal maxPrecio,
            int page,
            int size,
            String sortField,
            boolean asc) {
        int offset = page * size;
        var cb = em.getCriteriaBuilder();

        // DATA QUERY
        CriteriaQuery<PeliculaEntity> dataQuery = cb.createQuery(PeliculaEntity.class);
        Root<PeliculaEntity> dataRoot = dataQuery.from(PeliculaEntity.class);
        var dataPredicates = buildPredicates(
                cb,
                dataRoot,
                q,
                genero,
                formato,
                condicion,
                actor,
                director,
                desde,
                hasta,
                minPrecio,
                maxPrecio);

        dataQuery.select(dataRoot).distinct(true);
        if (!dataPredicates.isEmpty()) {
            dataQuery.where(dataPredicates.toArray(new Predicate[0]));
        }

        Path<?> orderPath = resolveSortPath(dataRoot, sortField);
        dataQuery.orderBy(asc ? cb.asc(orderPath) : cb.desc(orderPath));

        var paged = em.createQuery(dataQuery)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

        // COUNT QUERY
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PeliculaEntity> countRoot = countQuery.from(PeliculaEntity.class);
        var countPredicates = buildPredicates(
                cb,
                countRoot,
                q,
                genero,
                formato,
                condicion,
                actor,
                director,
                desde,
                hasta,
                minPrecio,
                maxPrecio);
        countQuery.select(cb.countDistinct(countRoot));
        if (!countPredicates.isEmpty()) {
            countQuery.where(countPredicates.toArray(new Predicate[0]));
        }

        long total = em.createQuery(countQuery).getSingleResult();

        var items = paged.stream().map(PeliculaEntity::asDomain).toList();
        return new PageResult<>(items, total, page, size);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<PeliculaEntity> root,
            String titulo,
            String genero,
            String formato,
            String condicion,
            String actor,
            String director,
            LocalDate desde,
            LocalDate hasta,
            BigDecimal minPrecio,
            BigDecimal maxPrecio) {
        var preds = new ArrayList<Predicate>();

        preds.add(cb.isTrue(root.get("activa")));

        if (titulo != null && !titulo.isBlank()) {
            preds.add(cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%"));
        }
        if (genero != null && !genero.isBlank()) {
            preds.add(cb.equal(cb.lower(root.get("genero").get("nombre")), genero.toLowerCase()));
        }
        if (formato != null && !formato.isBlank()) {
            preds.add(cb.equal(cb.lower(root.get("formato").get("nombre")), formato.toLowerCase()));
        }
        if (condicion != null && !condicion.isBlank()) {
            preds.add(cb.equal(cb.lower(root.get("condicion").get("nombre")), condicion.toLowerCase()));
        }
        if (actor != null && !actor.isBlank()) {
            var actorJoin = root.join("actores", JoinType.LEFT);
            preds.add(cb.like(cb.lower(actorJoin.get("nombre")), "%" + actor.toLowerCase() + "%"));
        }
        if (director != null && !director.isBlank()) {
            var directorJoin = root.join("directores", JoinType.LEFT);
            preds.add(cb.like(cb.lower(directorJoin.get("nombre")), "%" + director.toLowerCase() + "%"));
        }
        if (desde != null) {
            preds.add(cb.greaterThanOrEqualTo(root.get("fechaSalida"), desde));
        }
        if (hasta != null) {
            preds.add(cb.lessThanOrEqualTo(root.get("fechaSalida"), hasta));
        }
        if (minPrecio != null) {
            preds.add(cb.ge(root.get("precio"), minPrecio));
        }
        if (maxPrecio != null) {
            preds.add(cb.le(root.get("precio"), maxPrecio));
        }

        return preds;
    }

    private Path<?> resolveSortPath(Root<PeliculaEntity> root, String sortField) {
        String normalized = (sortField == null || sortField.isBlank()) ? "titulo" : sortField;
        return switch (normalized) {
            case "precio" -> root.get("precio");
            case "fechaSalida" -> root.get("fechaSalida");
            case "genero" -> root.get("genero").get("nombre");
            case "formato" -> root.get("formato").get("nombre");
            case "condicion" -> root.get("condicion").get("nombre");
            default -> root.get("titulo");
        };
    }

    public java.util.List<Pelicula> buscarPorPrecioEntre(java.math.BigDecimal min, java.math.BigDecimal max) {
        var list = em
                .createQuery(
                        "SELECT p FROM PeliculaEntity p WHERE p.precio BETWEEN :min AND :max ORDER BY p.precio ASC",
                        PeliculaEntity.class)
                .setParameter("min", min)
                .setParameter("max", max)
                .getResultList();
        return list.stream().map(PeliculaEntity::asDomain).toList();
    }

    public java.util.List<Pelicula> buscarPorFechaSalidaEntre(java.time.LocalDate desde, java.time.LocalDate hasta) {
        var list = em.createQuery(
                "SELECT p FROM PeliculaEntity p WHERE p.fechaSalida BETWEEN :d AND :h ORDER BY p.fechaSalida DESC",
                PeliculaEntity.class)
                .setParameter("d", desde)
                .setParameter("h", hasta)
                .getResultList();
        return list.stream().map(PeliculaEntity::asDomain).toList();
    }

    public java.util.List<Pelicula> buscarDinamico(String titulo, String genero, String formato, String condicion,
            java.time.LocalDate desde, java.time.LocalDate hasta,
            java.math.BigDecimal minPrecio, java.math.BigDecimal maxPrecio) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(PeliculaEntity.class);
        var root = cq.from(PeliculaEntity.class);

        var preds = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

        if (titulo != null && !titulo.isBlank())
            preds.add(cb.like(cb.lower(root.get("titulo")), "%" + titulo.toLowerCase() + "%"));
        if (genero != null && !genero.isBlank())
            preds.add(cb.equal(cb.lower(root.get("genero")), genero.toLowerCase()));
        if (formato != null && !formato.isBlank())
            preds.add(cb.equal(cb.lower(root.get("formato")), formato.toLowerCase()));
        if (condicion != null && !condicion.isBlank())
            preds.add(cb.equal(cb.lower(root.get("condicion")), condicion.toLowerCase()));
        if (desde != null)
            preds.add(cb.greaterThanOrEqualTo(root.get("fechaSalida"), desde));
        if (hasta != null)
            preds.add(cb.lessThanOrEqualTo(root.get("fechaSalida"), hasta));
        if (minPrecio != null)
            preds.add(cb.ge(root.get("precio"), minPrecio));
        if (maxPrecio != null)
            preds.add(cb.le(root.get("precio"), maxPrecio));

        cq.select(root).where(preds.toArray(new jakarta.persistence.criteria.Predicate[0]));
        cq.orderBy(cb.asc(root.get("titulo")));

        var results = em.createQuery(cq).getResultList();
        return results.stream().map(PeliculaEntity::asDomain).toList();
    }

    public java.util.List<String> listarGeneros() {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(GeneroEntity.class);
        var root = cq.from(GeneroEntity.class);
        cq.select(root).orderBy(cb.asc(root.get("nombre")));
        var list = em.createQuery(cq).getResultList();
        return list.stream().map(g -> g.nombre).toList();
    }

    private DirectorEntity findDirectorPorNombre(String nombre) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(DirectorEntity.class);
        var root = cq.from(DirectorEntity.class);
        cq.select(root).where(cb.equal(root.get("nombre"), nombre));
        var list = em.createQuery(cq).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private ActorEntity findActorPorNombre(String nombre) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(ActorEntity.class);
        var root = cq.from(ActorEntity.class);
        cq.select(root).where(cb.equal(root.get("nombre"), nombre));
        var list = em.createQuery(cq).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private CondicionEntity findCondicionPorNombre(String nombre) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(CondicionEntity.class);
        var root = cq.from(CondicionEntity.class);
        cq.select(root).where(cb.equal(root.get("nombre"), nombre));
        var list = em.createQuery(cq).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private FormatoEntity findFormatoPorNombre(String nombre) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(FormatoEntity.class);
        var root = cq.from(FormatoEntity.class);
        cq.select(root).where(cb.equal(root.get("nombre"), nombre));
        var list = em.createQuery(cq).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    private GeneroEntity findGeneroPorNombre(String nombre) {
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(GeneroEntity.class);
        var root = cq.from(GeneroEntity.class);
        cq.select(root).where(cb.equal(root.get("nombre"), nombre));
        var list = em.createQuery(cq).getResultList();
        return list.isEmpty() ? null : list.get(0);
    }

    @Transactional
    public Pelicula eliminar(Long id) {
        PeliculaEntity pe = em.find(PeliculaEntity.class, id);
        if (pe == null) {
            return null;
        }

        pe.activa = false;
        pe.version = pe.version + 1;
        return pe.asDomain();
    }

    public List<Pelicula> listarTodos() {
        var list = em
                .createQuery("SELECT p FROM PeliculaEntity p WHERE p.activa = true ORDER BY p.titulo",
                        PeliculaEntity.class)
                .getResultList();
        return list.stream().map(PeliculaEntity::asDomain).toList();
    }

    @Transactional
    public Pelicula actualizar(Long id, Pelicula p) {
        PeliculaEntity pe = em.find(PeliculaEntity.class, id);
        if (pe == null) {
            return null;
        }

        var directores = new ArrayList<DirectorEntity>();
        for (Director d : p.directores()) {
            DirectorEntity de = findDirectorPorNombre(d.nombre());
            if (de == null) {
                de = new DirectorEntity(d.nombre());
                em.persist(de);
            }
            directores.add(de);
        }

        var actores = new ArrayList<ActorEntity>();
        for (Actor a : p.actores()) {
            ActorEntity ae = findActorPorNombre(a.nombre());
            if (ae == null) {
                ae = new ActorEntity(a.nombre());
                em.persist(ae);
            }
            actores.add(ae);
        }

        CondicionEntity ce = findCondicionPorNombre(p.condicion().toString());
        if (ce == null) {
            ce = new CondicionEntity(p.condicion().toString());
            em.persist(ce);
        }

        FormatoEntity fe = findFormatoPorNombre(p.formato().tipo());
        if (fe == null) {
            fe = new FormatoEntity(p.formato().tipo());
            em.persist(fe);
        }

        GeneroEntity ge = findGeneroPorNombre(p.genero().nombre());
        if (ge == null) {
            ge = new GeneroEntity(p.genero().nombre());
            em.persist(ge);
        }

        pe.titulo = p.titulo();
        pe.condicion = ce;
        pe.precio = BigDecimal.valueOf(p.precio());
        pe.formato = fe;
        pe.genero = ge;
        pe.sinopsis = p.sinopsis();
        pe.imagenUrl = p.imagenUrl();
        pe.fechaSalida = p.fechaSalida();
        pe.directores = directores;
        pe.actores = actores;
        pe.rating = p.rating();
        pe.ratingPromedio = p.ratingPromedio();
        pe.totalRatings = p.totalRatings();
        pe.version = pe.version + 1;

        return pe.asDomain();
    }

}
