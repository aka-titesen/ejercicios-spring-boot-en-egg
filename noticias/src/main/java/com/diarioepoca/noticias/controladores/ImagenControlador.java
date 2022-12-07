package com.diarioepoca.noticias.controladores;

import com.diarioepoca.noticias.entidades.Noticia;
import com.diarioepoca.noticias.entidades.Usuario;
import com.diarioepoca.noticias.excepciones.NoticiaException;
import com.diarioepoca.noticias.servicios.NoticiaServicios;
import com.diarioepoca.noticias.servicios.UsuarioServicios;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/imagen")
public class ImagenControlador {

    @Autowired
    private UsuarioServicios usuarioServicios;
    @Autowired
    private NoticiaServicios noticiaServicios;

    @GetMapping("/perfil/{id}")
    public ResponseEntity<byte[]> imagenUsuario(@PathVariable Integer id) {

        Usuario usuario = usuarioServicios.getOne(id);

        byte[] imagenUsuario = usuario.getFoto().getContenido(); //Contenido de la foto

        HttpHeaders headers = new HttpHeaders();

        headers.setContentType(MediaType.IMAGE_JPEG); //Le decimos a la cabeceras que devolverá una imagen

        return new ResponseEntity<>(imagenUsuario, headers, HttpStatus.OK); //Devolvemos la imagen, las cabeceras y el estatus
    }

//    @GetMapping("/noticia/{id}")
//    public ResponseEntity<byte[]> imagenNoticia(@PathVariable Integer id) {
//
//        try {
//            Noticia noticia = noticiaServicios.getOne(id);
//
//            if (noticia.getFoto() == null) {
//                throw new NoticiaException("La noticia no tiene imagen asignada");
//            }
//
//            byte[] imagenNoticia = noticia.getFoto().getContenido;
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.IMAGE_JPEG);
//
//            return new ResponseEntity<>(imagenNoticia, headers, HttpStatus.OK);
//
//        } catch (NoticiaException ex) {
//            Logger.getLogger(ImagenControlador.class.getName()).log(Level.SEVERE, null, ex);
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//    }
}
