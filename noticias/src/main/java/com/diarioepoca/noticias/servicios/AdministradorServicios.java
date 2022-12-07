package com.diarioepoca.noticias.servicios;

import com.diarioepoca.noticias.entidades.Administrador;
import com.diarioepoca.noticias.entidades.Imagen;
import com.diarioepoca.noticias.entidades.Noticia;
import com.diarioepoca.noticias.entidades.Periodista;
import com.diarioepoca.noticias.entidades.Usuario;
import com.diarioepoca.noticias.enumeraciones.Rol;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdministradorServicios implements UserDetailsService {

    @Autowired
    private NoticiaRepositorio noticiaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private PeriodistaRepositorio periodistaRepositorio;
    @Autowired
    private AdministradorRepositorio administradorRepositorio;
    @Autowired
    private ImagenServicios imagenServicos;

    @Transactional
    public void crearAdministrador(MultipartFile archivo, String nombre, String email, String password, String passwordDos) throws NoticiaException {

        validarSiExisteAdministrador(email);

        validar(nombre, email, password, passwordDos);

        Administrador administrador = new Administrador();

        administrador.setNombre(nombre);
        administrador.setEmail(email);
        administrador.setPassword(new BCryptPasswordEncoder().encode(password)); //Encriptamos la contraseña
        administrador.setActivo(true);
        administrador.setAlta(new Date());
        administrador.setBaja(null);
        administrador.setRol(Rol.ADMINISTRADOR);

        Imagen imagen = imagenServicos.guardarImagen(archivo); //Servicio para persistir en la Bd la foto de perfil del usuario

        administrador.setFoto(imagen); //Establecemos la imagen al atributo del usuario

        administradorRepositorio.save(administrador);

    }

    public void validar(String nombre, String email, String password, String passwordDos) throws NoticiaException {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new NoticiaException("El nombre del usuario no puede ser nulo o estar vacio");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new NoticiaException("La contraseña del usuario no puede ser nulo o estar vacio");
        }
        if (password == null || password.trim().isEmpty() || password.length() <= 6) {
            throw new NoticiaException("La contraseña del usuario no puede ser nulo o estar vacio");
        }
        if (passwordDos == null || passwordDos.trim().isEmpty() || passwordDos.length() <= 6) {
            throw new NoticiaException("La segunda contraseña del usuario no puede ser nulo o estar vacio");
        }
        if (!password.equals(passwordDos)) {
            throw new NoticiaException("Las contraseñas ingresadas deben ser iguales");
        }
    }

    private void validarSiExisteAdministrador(String email) throws NoticiaException {
        if (administradorRepositorio.buscarAdministradorPorEmail(email) != null) {
            throw new NoticiaException("Ya hay un usuario con el mismo correo electronico");
        }
    }

    @Transactional(readOnly = true)
    public List<Administrador> listarAdministradores() {
        List<Administrador> auxiliar = new ArrayList();
        List<Administrador> administradores = new ArrayList();
        auxiliar = administradorRepositorio.findAll();
        for (Administrador administrador : auxiliar) {
            if (administrador.isActivo()) {
                administradores.add(administrador);
            }
        }
        return administradores;
    }

    @Transactional(readOnly = true)
    public List<Periodista> listarPeriodistas() {
        List<Periodista> auxiliar = new ArrayList();
        List<Periodista> periodistas = new ArrayList();
        auxiliar = periodistaRepositorio.findAll();
        for (Periodista periodista : auxiliar) {
            if (periodista.isActivo()) {
                periodistas.add(periodista);
            }
        }
        return periodistas;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        List<Usuario> auxiliar = new ArrayList();
        List<Usuario> usuarios = new ArrayList();
        auxiliar = usuarioRepositorio.findAll();
        for (Usuario usuario : auxiliar) {
            if (usuario.isActivo()) {
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    @Transactional
    public void cambiarSalario(Integer id, Integer salario) throws NoticiaException {

        Optional<Periodista> respuesta = periodistaRepositorio.findById(id);

        if (respuesta.isPresent()) {
            Periodista periodistaEncontrado = respuesta.get();
            periodistaEncontrado.setSalario(salario);
            periodistaRepositorio.save(periodistaEncontrado);
        } else {
            throw new NoticiaException("No se encontró un periodista con ese id");
        }

    }

    @Transactional
    public void cambiarRol(Integer id, String rol) throws NoticiaException {
        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);
        if (respuesta.isPresent()) {
            Usuario usuarioEncontrado = respuesta.get();
            switch (rol) { // Inicio - switch externo

                case "USUARIO": //Si el usuario quiere cambiar a Usuario

                    switch (usuarioEncontrado.getRol().toString()) { // Inicio - Sub switch
                        case "PERIODISTA":
                            Periodista periodistaDesvincular = (Periodista) usuarioEncontrado;
                            periodistaDesvincular.setNoticias(null);
                            periodistaDesvincular.setSalario(null);
                            List<Noticia> noticiasDevueltasDb = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodistaDesvincular.getId());
                            for (int i = 0; i < noticiasDevueltasDb.size(); i++) {
                                noticiasDevueltasDb.get(i).setPeriodista(null);
                                noticiasDevueltasDb.get(i).setBaja(new Date());
                                noticiasDevueltasDb.get(i).setActivo(false);
                                noticiaRepositorio.save(noticiasDevueltasDb.get(i));
                            }
                            periodistaDesvincular.setRol(Rol.USUARIO);
                            Usuario aUsuarioConversion = periodistaDesvincular;
                            usuarioRepositorio.save(aUsuarioConversion);
                            usuarioRepositorio.cambiarRol("Usuario", aUsuarioConversion.getId());
                            break;
                        case "ADMINISTRADOR":
                            usuarioEncontrado.setRol(Rol.USUARIO);
                            usuarioRepositorio.save(usuarioEncontrado);
                            usuarioRepositorio.cambiarRol("Usuario", usuarioEncontrado.getId());
                            break;
                        default:
                            throw new NoticiaException("Error de sistema");
                    }// Final - Sub switch
                    break;

                case "PERIODISTA": //SI el usuario quiere cambiar a periodista

                    switch (usuarioEncontrado.getRol().toString()) {  // Inicio - Sub switch
                        case "USUARIO":
                            usuarioEncontrado.setRol(Rol.PERIODISTA);
                            usuarioRepositorio.save(usuarioEncontrado);
                            usuarioRepositorio.cambiarRol("Periodista", usuarioEncontrado.getId());
                            break;
                        case "ADMINISTRADOR":
                            usuarioEncontrado.setRol(Rol.PERIODISTA);
                            usuarioRepositorio.save(usuarioEncontrado);
                            usuarioRepositorio.cambiarRol("Periodista", usuarioEncontrado.getId());
                            break;
                        default:
                            throw new NoticiaException("Error de sistema");
                    }// Final - Sub switch
                    break;

                case "ADMINISTRADOR": //Si el usuario quiere cambiar a administrador

                    switch (usuarioEncontrado.getRol().toString()) { // Inicio - Sub switch
                        case "USUARIO":
                            usuarioEncontrado.setRol(Rol.ADMINISTRADOR);
                            usuarioRepositorio.save(usuarioEncontrado);
                            usuarioRepositorio.cambiarRol("Administrador", usuarioEncontrado.getId());
                            break;
                        case "PERIODISTA":
                            Periodista periodistaDesvincular = (Periodista) usuarioEncontrado;
                            periodistaDesvincular.setNoticias(null);
                            periodistaDesvincular.setSalario(null);
                            List<Noticia> noticiasDevueltasDb = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodistaDesvincular.getId());
                            for (int i = 0; i < noticiasDevueltasDb.size(); i++) {
                                noticiasDevueltasDb.get(i).setPeriodista(null);
                                noticiasDevueltasDb.get(i).setBaja(new Date());
                                noticiasDevueltasDb.get(i).setActivo(false);
                                noticiaRepositorio.save(noticiasDevueltasDb.get(i));
                            }
                            periodistaDesvincular.setRol(Rol.ADMINISTRADOR);
                            Usuario aUsuarioConversion = periodistaDesvincular;
                            usuarioRepositorio.save(aUsuarioConversion);
                            usuarioRepositorio.cambiarRol("Administrador", aUsuarioConversion.getId());
                            break;
                        default:
                            throw new NoticiaException("Error de sistema");
                    } // Final - Sub switch
                    break;
                default:
                    throw new NoticiaException("Error de sistema");

            } // Final - switch externo
        } else {
            throw new NoticiaException("No se encontró un usuario con ese id");
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        /*
        Obtenemos el usuario que intenta ingresar a través de su email.
        Buscamos un usuario de nuestro dominio y transformarlo al dominio de Spring Security
         */
        Administrador administrador = administradorRepositorio.buscarAdministradorPorEmail(email);

        if (administrador != null) { //Acá empezamos a trabajar con Spring Security

            List<GrantedAuthority> permisos = new ArrayList(); //Ésta lista va a contener todos los permisos que tendrá el usuario

            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + administrador.getRol().toString()); //ROLE_Usuario

            permisos.add(p); //Guardamos el permiso de la línea 80 a la lista de permisos

            return new User(administrador.getEmail(), administrador.getPassword(), permisos); //Ésta clase recibe el username, password y los permisos que tendrá el usuario; es decir otorga los permisos a un usuario
        } else {
            return null;
        }
    }

}
