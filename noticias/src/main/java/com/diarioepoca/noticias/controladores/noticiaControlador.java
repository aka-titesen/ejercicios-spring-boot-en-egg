package com.diarioepoca.noticias.controladores;

import com.diarioepoca.noticias.entidades.Noticia;
import com.diarioepoca.noticias.entidades.Usuario;
import com.diarioepoca.noticias.excepciones.NoticiaException;
import com.diarioepoca.noticias.servicios.AdministradorServicios;
import com.diarioepoca.noticias.servicios.NoticiaServicios;
import com.diarioepoca.noticias.servicios.UsuarioServicios;
import java.util.List;
import javax.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/")
public class noticiaControlador {

    @Autowired
    private NoticiaServicios noticiaServicios;
    @Autowired
    private UsuarioServicios usuarioServicio;
    @Autowired
    private AdministradorServicios administradoresServicios;

    @GetMapping("/index")
    public String index(HttpSession sesion) {
        /*
        Recupero de la sesión actual; de la petición actual, en éste caso el usuario logueado, 
        que anteriormente establecimos en la clase servicio en el método loadUserByUsername
         */
        Usuario usuarioLogueado = (Usuario) sesion.getAttribute("usuarioSesion");
        if (usuarioLogueado.getRol().toString().equalsIgnoreCase("PERIODISTA")) { //Preguntamos que rol tiene para que en base de eso le redireccione a una vista predeterminado
            return "redirect:/periodista/listarNoticiasModificar";
        }
        if (usuarioLogueado.getRol().toString().equalsIgnoreCase("ADMINISTRADOR")) {
            return "redirect:/administrador/listarUsuariosModificar";
        }
        if (usuarioLogueado.getRol().toString().equalsIgnoreCase("USUARIO")) {
            return "redirect:/listarNoticias";
        }

        return null;
    }

    /**
     * Éste método dispara el "index.html" cuando el usuario se mete en
     * localhost:8080/. Éste método recibe un "ModelMap" que guarda un arrayList
     * de noticias que inyectará en el html mediante "Thymeleaf" en cards
     *
     * @return index.html
     */
    @GetMapping("/listarNoticias")
    public String listarNoticias(ModelMap modelo) {
        List<Noticia> noticias = noticiaServicios.listarNoticiasGeneral();
        modelo.addAttribute("noticias", noticias);
        return "listarNoticias.html";
    }

    /**
     * Éste método se dispara cuando el usuario hace clic en una noticia, recibe
     * un id de la misma, dentro de éste método- llama a servicios y su método
     * de buscar "getOne" y le pasa el id justamente para devolver ese objeto
     * noticia en concreto. Lo carga al ModelMap y devuelve el detalle.html para
     * descomponerlo y mostrarlo
     *
     * @param id
     * @param modelo
     * @return detalle.html
     */
//    @PreAuthorize("hasAnyRole('ROLE_USUARIO', 'ROLE_PERIODISTA', 'ROLE_ADMINISTRADOR')")
    @GetMapping("/detallarNoticia")
    public String detallarNoticia(@RequestParam Integer id, ModelMap modelo) {
        Noticia noticia = noticiaServicios.getOne(id);
        modelo.addAttribute("noticia", noticia);
        return "detallarNoticia.html";
    }

    /**
     * Éste método que a la dirección /registrar va a renderizar la vista form
     * para que el usuario se pueda registrar
     *
     * @return registrarUsuario.html
     */
    @GetMapping("/registrarUsuario")
    public String registrarUsuario() {
        return "registrarUsuario.html";
    }

    /**
     * Éste método PostMapping recibe los datos del formulario
     * registrarUsuario.html para persistir el usuario en la base de datos
     *
     * @param nombre
     * @param email
     * @param password
     * @param passwordDos
     * @param modelo
     * @return redirect:../registrarUsuario o registrarUsuario.html
     */
    @PostMapping("/persistirUsuario")
    public String persistirUsuario(@RequestParam String nombre, @RequestParam String email, @RequestParam String password, @RequestParam String passwordDos, ModelMap modelo, MultipartFile archivo) {
        try {
            usuarioServicio.crearUsuario(archivo, nombre, email, password, passwordDos);
            modelo.put("exito", "El usuario fue registrado correctamente");
            return "redirect:/login";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("email", email);
            return "registrarUsuario.html";
        }
    }

    @GetMapping("/editarPerfil")
    public String editarPerfil(HttpSession sesion, ModelMap modelo) {
        Usuario usuarioLogueado = (Usuario) sesion.getAttribute("usuarioSesion");
        modelo.addAttribute("usuario", usuarioLogueado);
        return "editarPerfil.html";
    }

    @PostMapping("/persistirPerfilModificado/{id}")
    public String persistirPerfilModificado(@PathVariable Integer id, MultipartFile archivo, ModelMap modelo) {
        try {
            usuarioServicio.modificarUsuario(id, archivo);
            modelo.put("exito", "El usuario fue modificado correctamente");
            return "redirect:/listarNoticias";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            return "editarPerfil.html";
        }
    }

    /**
     * Éste método devuelve la vista login para iniciar sesión
     *
     * @param error
     * @param modelo
     * @return
     */
    @GetMapping("/login")
    public String login(@RequestParam(required = false) String error, ModelMap modelo) {
        if (error != null) {
            modelo.put("error", "¡Usuario o contraseña invalidos!");
        }
        return "login.html";
    }
}
