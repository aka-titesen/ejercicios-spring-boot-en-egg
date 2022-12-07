package com.diarioepoca.noticias.controladores;

import com.diarioepoca.noticias.entidades.Periodista;
import com.diarioepoca.noticias.entidades.Usuario;
import com.diarioepoca.noticias.excepciones.NoticiaException;
import com.diarioepoca.noticias.servicios.AdministradorServicios;
import com.diarioepoca.noticias.servicios.NoticiaServicios;
import com.diarioepoca.noticias.servicios.PeriodistaServicios;
import com.diarioepoca.noticias.servicios.UsuarioServicios;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
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
@RequestMapping("/administrador")
public class administradorControlador {

    @Autowired
    private NoticiaServicios noticiaServicios;
    @Autowired
    private UsuarioServicios usuarioServicios;
    @Autowired
    private PeriodistaServicios periodistaServicios;
    @Autowired
    private AdministradorServicios administradorServicios;

    @GetMapping("/registrarAdministrador")
    public String registrarAdministrador() {
        return "registrarAdministrador.html";
    }

    @PostMapping("/persistirAdministrador")
    public String persistirAdministrador(@RequestParam String nombre, @RequestParam String email, @RequestParam String password, @RequestParam String passwordDos, ModelMap modelo, MultipartFile archivo) {
        try {
            administradorServicios.crearAdministrador(archivo, nombre, email, password, passwordDos);
            modelo.put("exito", "El administrador fue registrado correctamente");
            return "redirect:/login";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("email", email);
            return "registrarAdministrador.html";
        }
    }

    @GetMapping("/registrarPeriodista")
    public String registrarPeriodista() {
        return "registrarPeriodista.html";
    }

    @PostMapping("/persistirPeriodista")
    public String persistirPeriodista(@RequestParam String nombre, @RequestParam String email, @RequestParam String password, @RequestParam String passwordDos, @RequestParam Integer salario, ModelMap modelo, MultipartFile archivo) {
        try {
            periodistaServicios.crearPeriodista(archivo, nombre, email, password, passwordDos, salario);
            modelo.put("exito", "El periodista fue registrado correctamente");
            return "redirect:/login";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            modelo.put("nombre", nombre);
            modelo.put("email", email);
            modelo.put("salario", salario);
            return "registrarPeriodista.html";
        }
    }

    @GetMapping("/listarPeriodistasModificar")
    public String listarPeriodistaModificar(ModelMap modelo) {
        List<Periodista> periodistas = periodistaServicios.listarPeriodistas();
        modelo.addAttribute("periodistas", periodistas);
        return "listarPeriodistasModificar.html";
    }

    @GetMapping("/periodistaDetalladoModificar/{id}")
    public String periodistaDetalladoModificar(@PathVariable Integer id, ModelMap modelo) {
        Periodista periodistaEncontrado = periodistaServicios.getOne(id);
        modelo.addAttribute("periodista", periodistaEncontrado);
        return "periodistaDetalladoModificar.html";
    }

    @PostMapping("/persistirPeriodistaModificado/{id}")
    public String persistirUsuarioModificado(@PathVariable Integer id, @RequestParam Integer salario, ModelMap modelo) {
        try {
            administradorServicios.cambiarSalario(id, salario);
            modelo.put("exito", "El salario se cambió exitosamente");
            return "redirect:/administrador/listarPeriodistasModificar";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            return "listarPeriodistasModificar.html";
        }
    }

    @GetMapping("/listarUsuariosModificar")
    public String listarUsuariosModificar(ModelMap modelo) {
        List<Usuario> usuarios = usuarioServicios.listarUsuarios();
        modelo.addAttribute("usuarios", usuarios);
        return "listarUsuariosModificar.html";
    }

    @GetMapping("/usuarioDetalladoModificar/{id}")
    public String usuarioDetalladoModificar(@PathVariable Integer id, ModelMap modelo) {
        Usuario usuarioEncontrado = usuarioServicios.getOne(id);
        List<String> roles = new ArrayList();
        roles.add("USUARIO");
        roles.add("PERIODISTA");
        roles.add("ADMINISTRADOR");
        for (int i = 0; i < roles.size(); i++) {
            if (usuarioEncontrado.getRol().toString().equalsIgnoreCase(roles.get(i))) {
                roles.remove(i);
            }
        }
        modelo.addAttribute("roles", roles);
        modelo.addAttribute("usuario", usuarioEncontrado);
        return "usuarioDetalladoModificar.html";
    }

    @PostMapping("/persistirUsuarioModificado/{id}")
    public String persistirUsuarioModificado(@PathVariable Integer id, @RequestParam String rol, ModelMap modelo) {
        try {
            administradorServicios.cambiarRol(id, rol);
            modelo.put("exito", "El rol se cambió exitosamente");
            return "redirect:/logout";
        } catch (NoticiaException ex) {
            modelo.put("error", ex.getMessage());
            return "listarUsuariosModificar.html";
        }
    }

    @GetMapping("/persistirUsuarioEliminado/{id}")
    public String persistirUsuarioEliminado(@PathVariable Integer id) {
        try {
            usuarioServicios.deleteOne(id);
            return "redirect:/administrador/listarUsuariosModificar";
        } catch (NoticiaException ex) {
            Logger.getLogger(administradorControlador.class.getName()).log(Level.SEVERE, null, ex);
            return "listarUsuariosModificar.html";
        }
    }
}
