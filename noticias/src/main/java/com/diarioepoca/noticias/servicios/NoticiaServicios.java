package com.diarioepoca.noticias.servicios;

import com.diarioepoca.noticias.entidades.Noticia;
import com.diarioepoca.noticias.entidades.Periodista;
import com.diarioepoca.noticias.excepciones.NoticiaException;
import com.diarioepoca.noticias.repositorios.AdministradorRepositorio;
import com.diarioepoca.noticias.repositorios.NoticiaRepositorio;
import com.diarioepoca.noticias.repositorios.PeriodistaRepositorio;
import com.diarioepoca.noticias.repositorios.UsuarioRepositorio;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoticiaServicios {

    @Autowired
    private NoticiaRepositorio noticiaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private PeriodistaRepositorio periodistaRepositorio;
    @Autowired
    private AdministradorRepositorio administradorRepositorio;

    @Transactional
    public void crearNoticia(String titulo, String cuerpo, String foto, Periodista periodista) throws NoticiaException {
        validar(titulo, cuerpo, foto);
        //Si hay una exception, no se va a ejecutar el código de abajo
        Noticia noticia = new Noticia();
        noticia.setTitulo(titulo);
        noticia.setCuerpo(cuerpo);
        noticia.setFoto(foto);
        noticia.setActivo(true);
        noticia.setAlta(new Date());
        noticia.setBaja(null);

        noticia.setPeriodista(periodista); //Agrego el periodista de la sesión actual en la noticia creada y que va a ser persistida
        /*
        Busco la noticia o las noticias que pueda tener el periodista actual y las devuelvo en una lista 
         */
        List<Noticia> noticiasDevueltasDb = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodista.getId());
        noticiasDevueltasDb.add(noticia); //A la lista devuelta le agrego la noticia que está creada en éste contexto

        periodista.setNoticias(noticiasDevueltasDb); //Asigno la noticia o las noticias asociadas al periodista más la que fue creada recientemente

        //Arrays.asList(noticiasDevueltasDb)
        noticiaRepositorio.save(noticia); //Persisto la noticia relacionado con el periodista actual de la sesión en la base de datos
        periodistaRepositorio.save(periodista); //Persisto con los cambios; es decir con el periodista más con la noticia agregada
    }

    @Transactional(readOnly = true)
    public List<Noticia> listarNoticiasDelPeriodista(Periodista periodista) {
        List<Noticia> noticias = new ArrayList();
        noticias = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodista.getId());
        return noticias;
    }

    @Transactional(readOnly = true)
    public List<Noticia> listarNoticiasGeneral() {
        List<Noticia> auxiliar = new ArrayList();
        List<Noticia> noticias = new ArrayList();
        auxiliar = noticiaRepositorio.findAll();
        for (Noticia noticia : auxiliar) {
            if (noticia.isActivo()) {
                noticias.add(noticia);
            }
        }
        return noticias;
    }

    @Transactional
    public void modificarNoticia(Integer id, String titulo, String cuerpo, String foto, Periodista periodista) throws NoticiaException {
        validar(titulo, cuerpo, foto);
        //Si hay una exception, no se va a ejecutar el código de abajo
        Optional<Noticia> respuesta = noticiaRepositorio.findById(id);
        if (respuesta.isPresent()) {
            /*
        Busco la noticia o las noticias que pueda tener el periodista actual y las devuelvo en una lista 
             */
            List<Noticia> noticiasDevueltasDb = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodista.getId());
            Noticia noticia = respuesta.get();
            noticia.setTitulo(titulo);
            noticia.setCuerpo(cuerpo);
            noticia.setFoto(foto);
            //Itero en la lista devuelto de la Db y busca la anterior "versión" de la noticia, la elimino y agrega la misma noticia pero con las modificaciones 
            for (int i = 0; i < noticiasDevueltasDb.size(); i++) {
                if (noticiasDevueltasDb.get(i).getId().equals(noticia.getId())) {
                    noticiasDevueltasDb.remove(i);
                    noticiasDevueltasDb.add(noticia);
                }
            }
            periodista.setNoticias(noticiasDevueltasDb); //Agrego a la lista de noticias del periodista, las noticias que ya tenía más la modificación
            noticiaRepositorio.save(noticia); //Persisto la noticia modificada
            periodistaRepositorio.save(periodista); //Persisto al periodista de la sesión con la noticia modificada
        } else {
            throw new NoticiaException("No se encontró una noticia con ese id");
        }
    }

    private void validar(String titulo, String cuerpo, String foto) throws NoticiaException {
        if (titulo == null || titulo.trim().isEmpty()) {
            throw new NoticiaException("El título no puede ser nulo ni vacio");
        }
        if (cuerpo == null || cuerpo.trim().isEmpty()) {
            throw new NoticiaException("El cuerpo no puede ser nulo ni vacio");
        }
        if (foto == null || foto.trim().isEmpty()) {
            throw new NoticiaException("La url de la foto no puede ser nulo ni vacio");
        }
    }

    @Transactional(readOnly = true)
    public Noticia getOne(Integer id) {
        return noticiaRepositorio.getOne(id);
    }

    //Eliminación lógica
    @Transactional
    public void deleteOne(Integer id, Periodista periodista) throws NoticiaException {
        Optional<Noticia> respuesta = noticiaRepositorio.findById(id); //Busco la noticia
        if (respuesta.isPresent()) {
            List<Noticia> noticiasDevueltasDb = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodista.getId()); //Busco la noticia o todos las noticias relacionadas con el periodista de la sesión actual
            Noticia noticiaAEliminar = respuesta.get();
            noticiaAEliminar.setPeriodista(null); //Quito la relación de la noticia con periodista creador
            noticiaAEliminar.setBaja(new Date());
            noticiaAEliminar.setActivo(false);
            for (int i = 0; i < noticiasDevueltasDb.size(); i++) { //Busco la noticia que quiero eliminar en la lista del periodista y la quito
                if (noticiasDevueltasDb.get(i).getId().equals(noticiaAEliminar.getId())) {
                    noticiasDevueltasDb.remove(i);
                }
            }
            periodista.setNoticias(noticiasDevueltasDb); //Vuelvo a establecer la lista del periodista al mismo pero sin la notica que voy a eliminar
            noticiaRepositorio.save(noticiaAEliminar); //Persisto la noticia eliminada
            periodistaRepositorio.save(periodista); //Persisito el periodista sin la noticia eliminada en su lista
        } else {
            throw new NoticiaException("No se encontró una noticia con ese id");
        }
    }
}
