package com.diarioepoca.noticias.servicios;

import com.diarioepoca.noticias.entidades.Imagen;
import com.diarioepoca.noticias.excepciones.NoticiaException;
import com.diarioepoca.noticias.repositorios.ImagenRepositorio;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagenServicios {

    @Autowired
    private ImagenRepositorio imagenRepositorio;

    public Imagen guardarImagen(MultipartFile archivo) throws NoticiaException {
        if (archivo != null) {
            try {
                Imagen imagen = new Imagen();
                imagen.setMime(archivo.getContentType()); //Tipo de archivo
                imagen.setNombre(archivo.getName()); //Nombre del archivo
                imagen.setContenido(archivo.getBytes()); //Contenido
                return imagenRepositorio.save(imagen);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return null;
    }

    public Imagen modificarImagen(MultipartFile archivo, Integer id) throws NoticiaException {
        if (archivo != null) {
            try {
                Imagen imagen = new Imagen();
                if (id != null) { //Si el id es nulo significa que no tenía imagen, entonces tendrá su imagen por primera vez, y se creará y persistirá a partir de la línea 42 hasta la 45
                    Optional<Imagen> respuesta = imagenRepositorio.findById(id);
                    if (respuesta.isPresent()) {
                        imagen = respuesta.get();
                    }
                }
                imagen.setMime(archivo.getContentType()); //Tipo de archivo
                imagen.setNombre(archivo.getName()); //Nombre del archivo
                imagen.setContenido(archivo.getBytes()); //ContenidoFS
                return imagenRepositorio.save(imagen);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        return null;
    }
}
