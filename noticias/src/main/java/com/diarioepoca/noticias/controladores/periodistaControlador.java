package com.diarioepoca.noticias.controladores;

import com.diarioepoca.noticias.entidades.Noticia;
import com.diarioepoca.noticias.entidades.Periodista;
import com.diarioepoca.noticias.excepciones.NoticiaException;
import com.diarioepoca.noticias.servicios.NoticiaServicios;
import com.diarioepoca.noticias.servicios.PeriodistaServicios;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/periodista")
public class periodistaControlador {

    @Autowired
    private NoticiaServicios noticiaServicios;
    @Autowired
    private PeriodistaServicios periodistaServicios;

    /**
     * Éste método devuelve la vista form para crear una nueva noticia
     *
     * @return registrarNoticia.html
     */
    @GetMapping("/registrarNoticia")
    public String registrarNoticia() {
        return "registrarNoticia.html";
    }

    /**
     * Éste método recibe los datos de la vista form para crear una nueva
     * noticia y los persiste en la base de datos
     *
     * @param titulo
     * @param cuerpo
     * @param foto
     * @param modelo
     * @return redirect:/ o registrarNoticia.html
     */
    @PostMapping("/persistirNoticia")
    public String persistirNoticia(@RequestParam String titulo, @RequestParam String cuerpo, @RequestParam String foto, HttpSession sesion, ModelMap modelo) {
        Periodista periodistaLogueado = (Periodista) sesion.getAttribute("usuarioSesion");
        try {
            noticiaServicios.crearNoticia(titulo, cuerpo, foto, periodistaLogueado);
            modelo.put("exito", "La noticia fue cargada correctamente");
            return "redirect:/periodista/listarNoticiasModificar";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("titulo", titulo);
            modelo.put("cuerpo", cuerpo);
            modelo.put("foto", foto);
            return "registrarNoticia.html";
        }
    }

    /**
     * Éste método devuelve la vista que es una lista de noticias con los
     * botones para modificar o eliminar una noticia
     *
     * @param modelo
     * @return listarNoticiasModificar.html
     */
    @GetMapping("/listarNoticiasModificar")
    public String listarNoticiasModificar(ModelMap modelo, HttpSession sesion) {
        Periodista periodistaLogueado = (Periodista) sesion.getAttribute("usuarioSesion");
        List<Noticia> noticias = noticiaServicios.listarNoticiasDelPeriodista(periodistaLogueado);
        modelo.addAttribute("noticias", noticias);
        return "listarNoticiasModificar.html";
    }

    /**
     * Éste método recibe un id de una noticia por la ruta, busca el objeto
     * noticia con ese id y devuelve una vista para ver esa noticia sola
     * detalladamente pasandole el objeto noticia a traves del ModelMap
     *
     * @param id
     * @param modelo
     * @return noticiaDetalladaModificar.html
     */
    @GetMapping("/noticiaDetalladaModificar/{id}")
    public String noticiaDetalladaModificar(@PathVariable Integer id, ModelMap modelo) {
        Noticia noticia = noticiaServicios.getOne(id);
        modelo.addAttribute("noticia", noticia);
        return "noticiaDetalladaModificar.html";
    }

    /**
     * Éste método recibe el id y los datos de la noticia a modificar y persiste
     * los cambios a la base de datos
     *
     * @param id
     * @param titulo
     * @param cuerpo
     * @param foto
     * @param modelo
     * @return redirect:/listarNoticiasModificar o
     * noticiaDetalladaModificar.html
     */
    @PostMapping("/persistirNoticiaModificada/{id}")
    public String persistirNoticiaModificada(@PathVariable Integer id, @RequestParam String titulo, @RequestParam String cuerpo, @RequestParam String foto, HttpSession sesion, ModelMap modelo) {
        Periodista periodistaLogueado = (Periodista) sesion.getAttribute("usuarioSesion");
        try {
            noticiaServicios.modificarNoticia(id, titulo, cuerpo, foto, periodistaLogueado);
            modelo.put("exito", "La noticia fue modificada correctamente");
            return "redirect:/periodista/listarNoticiasModificar";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            return "noticiaDetalladaModificar.html";
        }
    }

    /**
     * Éste método recibe un id por la ruta de la dirección y elimina un objeto
     * del tipo noticia
     *
     * @param id
     * @return redirect:/listarNoticiasModificar
     */
    @GetMapping("/persistirNoticiaEliminada/{id}")
    public String persistirNoticiaEliminada(@PathVariable Integer id, HttpSession sesion) {
        Periodista periodistaLogueado = (Periodista) sesion.getAttribute("usuarioSesion");
        try {
            noticiaServicios.deleteOne(id, periodistaLogueado);
        } catch (NoticiaException ex) {
            Logger.getLogger(periodistaControlador.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "redirect:/periodista/listarNoticiasModificar";
    }

}
