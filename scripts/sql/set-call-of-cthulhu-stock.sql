UPDATE almacen_peliculas.pelicula
SET stock_disponible = 10.00,
    activa = b'1'
WHERE LOWER(titulo) = 'call of cthulhu';

SELECT id, titulo, stock_disponible, activa
FROM almacen_peliculas.pelicula
WHERE LOWER(titulo) = 'call of cthulhu';
