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
import javax.servlet.http.HttpSession;
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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UsuarioServicios implements UserDetailsService { //La interfaz UserDetailsService trae un método que se usa para autenticar a los usuarios que ingresen a la plataforma 

    @Autowired
    private UsuarioRepositorio usuarioRepositorio;
    @Autowired
    private PeriodistaRepositorio periodistaRepositorio;
    @Autowired
    private AdministradorRepositorio administradorRepositorio;
    @Autowired
    private NoticiaRepositorio noticiaRepositorio;
    @Autowired
    private ImagenServicios imagenServicios;

    @Transactional
    public void crearUsuario(MultipartFile archivo, String nombre, String email, String password, String passwordDos) throws NoticiaException {

        validarSiExisteUsuario(email);

        validar(nombre, email, password, passwordDos);

        Usuario usuario = new Usuario();

        usuario.setNombre(nombre);
        usuario.setEmail(email);
        usuario.setPassword(new BCryptPasswordEncoder().encode(password)); //Encriptamos la contraseña
        usuario.setActivo(true);
        usuario.setAlta(new Date());
        usuario.setBaja(null);
        usuario.setRol(Rol.USUARIO);

        Imagen imagen = imagenServicios.guardarImagen(archivo); //Servicio para persistir en la Bd la foto de perfil del usuario

        usuario.setFoto(imagen); //Establecemos la imagen al atributo del usuario

        usuarioRepositorio.save(usuario); //Guardamos el usuario

    }

    public void modificarUsuario(Integer id, MultipartFile archivo) throws NoticiaException {
        Integer idImagen = null;
        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);

        if (respuesta.isPresent()) {

            Usuario usuario = respuesta.get();

            switch (usuario.getRol().toString()) {
                case "USUARIO":

                    if (usuario.getFoto() != null) { //Si tiene una foto
                        idImagen = usuario.getFoto().getId(); //Obtendremos el id de la foto
                    }

                    /*Actualizamos la imagen mediante el id, si éste es null, significa que el usuario no tenía foto y entonces éste método asignará ésta imagen por primera vez*/
                    Imagen imagenUsuario = imagenServicios.modificarImagen(archivo, idImagen);

                    usuario.setFoto(imagenUsuario); //Establecemos la imagen al usuario

                    usuarioRepositorio.save(usuario); //Establecemos el usuario con la imagen modificada

                    break;
                case "ADMINISTRADOR":

                    if (usuario.getFoto() != null) { //Si tiene una foto
                        idImagen = usuario.getFoto().getId(); //Obtendremos el id de la foto
                    }

                    /*Actualizamos la imagen mediante el id, si éste es null, significa que el usuario no tenía foto y entonces éste método asignará ésta imagen por primera vez*/
                    Imagen imagenAdministrador = imagenServicios.modificarImagen(archivo, idImagen);

                    usuario.setFoto(imagenAdministrador); //Establecemos la imagen al usuario

                    usuarioRepositorio.save(usuario); //Establecemos el usuario con la imagen modificadaD
                    break;
                case "PERIODISTA":
                    Periodista periodista = (Periodista) usuario;

                    if (periodista.getFoto() != null) { //Si tiene una foto
                        idImagen = periodista.getFoto().getId(); //Obtendremos el id de la foto
                    }
                    /*Actualizamos la imagen mediante el id, si éste es null, significa que el usuario no tenía foto y entonces éste método asignará ésta imagen por primera vez*/
                    Imagen imagenPeriodista = imagenServicios.modificarImagen(archivo, idImagen);

                    periodista.setFoto(imagenPeriodista); //Establecemos la imagen al periodista

                    List<Noticia> noticiasDevueltasBd = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodista.getId()); //Buscamos y devolvemos las noticas del periodista

                    for (Noticia noticia : noticiasDevueltasBd) { //Le aplicamos a cada noticia del periodista los cambios
                        noticia.setPeriodista(periodista);
                    }
                    periodista.setNoticias(noticiasDevueltasBd); //Le aplicamos al periodista las noticias con los cambios

                    periodistaRepositorio.save(periodista);

                    break;
                default:
                    throw new NoticiaException("Error de sistema");
            }
        } else {
            throw new NoticiaException("No se encontró un usuario con ese id");
        }
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

    private void validarSiExisteUsuario(String email) throws NoticiaException {
        if (usuarioRepositorio.buscarUsuarioPorEmail(email) != null) {
            throw new NoticiaException("Ya hay un usuario con el mismo correo electronico");
        }
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarUsuarios() {
        List<Usuario> auxililar = new ArrayList();
        List<Usuario> usuarios = new ArrayList();
        auxililar = usuarioRepositorio.findAll();
        for (Usuario usuario : auxililar) {
            if (usuario.isActivo()) {
                usuarios.add(usuario);
            }
        }
        return usuarios;
    }

    @Transactional(readOnly = true)
    public Usuario getOne(Integer id) {
        return usuarioRepositorio.getOne(id);
    }

//Eliminación lógica más sus relaciones
    @Transactional
    public void deleteOne(Integer id) throws NoticiaException {
        Optional<Usuario> respuesta = usuarioRepositorio.findById(id);
        if (respuesta.isPresent()) {
            Usuario usuarioEncontrado = respuesta.get();
            switch (usuarioEncontrado.getRol().toString()) {
                case "USUARIO":
                    usuarioEncontrado.setBaja(new Date());
                    usuarioEncontrado.setActivo(false);
                    usuarioRepositorio.save(usuarioEncontrado);
                    break;
                case "PERIODISTA":
                    Periodista periodistaEncontrado = (Periodista) usuarioEncontrado;
                    periodistaEncontrado.setNoticias(null);
                    periodistaEncontrado.setBaja(new Date());
                    periodistaEncontrado.setActivo(false);
                    List<Noticia> noticiasDevueltasDb = noticiaRepositorio.buscarNoticiasPorIdPeriodista(periodistaEncontrado.getId());

                    for (int i = 0; i < noticiasDevueltasDb.size(); i++) {
                        noticiasDevueltasDb.get(i).setPeriodista(null);
                        noticiasDevueltasDb.get(i).setBaja(new Date());
                        noticiasDevueltasDb.get(i).setActivo(false);
                        noticiaRepositorio.save(noticiasDevueltasDb.get(i));
                    }

                    periodistaRepositorio.save(periodistaEncontrado);
                    break;
                case "ADMINISTRADOR":
                    Administrador administradorEncontrado = (Administrador) usuarioEncontrado;
                    administradorEncontrado.setBaja(new Date());
                    administradorEncontrado.setActivo(false);
                    administradorRepositorio.save(administradorEncontrado);
                    break;
            }
        } else {
            throw new NoticiaException("No se encontró un usuario con ese id");
        }
    }

    /**
     * Cuando un usuario se logeé con sus credenciales, Spring security va a
     * dirijirse a éste método y va a otorgar los permisos a los que tiene
     * acceso éste usuario. Recibe el "username" de un usuario para poderlo
     * autenticar
     *
     * @param email
     * @return user
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        /*
        Obtenemos el usuario que intenta ingresar a través de su email.
        Buscamos un usuario de nuestro dominio y transformarlo al dominio de Spring Security
         */
        Usuario usuario = usuarioRepositorio.buscarUsuarioPorEmail(email);

        if (usuario != null) { //Acá empezamos a trabajar con Spring Security

            List<GrantedAuthority> permisos = new ArrayList(); //Ésta lista va a contener todos los permisos que tendrá el usuario

            GrantedAuthority p = new SimpleGrantedAuthority("ROLE_" + usuario.getRol().toString()); //ROLE_Usuario

            permisos.add(p); //Guardamos el permiso de la línea 80 a la lista de permisos

            /*
            Vamos a atrapar los atributos del usuario que inicio sesión en la plataforma y guardarlo en la sesión web más precisamente se va a guardar en la clase 
            ServletRequestAttributes y en el objeto attr.
            Llamamos a la clase RequestContextHolder que maneja los datos del usuario que se logea y con el método currentRequestAttributes(); obtenemos al objeto usuario de la sesión actual.
            Es decir de la solicitud HTTP
             */
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            /*
            Vamos a guardar los atributos de la sesión actual capturada en un objeto sesion de la clase HttpSession; es decir,
            vamos a guardar el llamado que nos trae el atributo y en base a eso la sesión, de la solicitud HTTP que pedimos antes
             */
            HttpSession sesion = attr.getRequest().getSession(true);

            sesion.setAttribute("usuarioSesion", usuario); //A la sesión actual añadimos el objeto actual; es decir el usuario que está logeado bajo una llave

            return new User(usuario.getEmail(), usuario.getPassword(), permisos); //Ésta clase recibe el username, password y los permisos que tendrá el usuario; es decir otorga los permisos a un usuario
        } else {
            return null;
        }
    }
}
