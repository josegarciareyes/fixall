package com.registro.usuarios.servicio;



import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.registro.usuarios.controlador.dto.UsuarioRegistroDTO;
import com.registro.usuarios.modelo.DatosPersonales;
import com.registro.usuarios.modelo.Rol;
import com.registro.usuarios.modelo.TipoUsuario;
import com.registro.usuarios.modelo.Usuario;
import com.registro.usuarios.repositorio.RolRepositorio;
import com.registro.usuarios.repositorio.TipoUsuarioRepositorio;
import com.registro.usuarios.repositorio.UsuarioRepositorio;

@Service
public class UsuarioServicioImpl implements UsuarioServicio {

	
	private final UsuarioRepositorio usuarioRepositorio;
	private final RolRepositorio rolRepositorio;
    private final TipoUsuarioRepositorio tipoUsuarioRepositorio;
	private final BCryptPasswordEncoder passwordEncoder; // Codificador de contraseñas

    // Constructor para inyectar dependencias
	public UsuarioServicioImpl(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio,
	TipoUsuarioRepositorio tipoUsuarioRepositorio, BCryptPasswordEncoder passwordEncoder) {
	this.usuarioRepositorio = usuarioRepositorio;
	this.rolRepositorio = rolRepositorio;
	this.tipoUsuarioRepositorio = tipoUsuarioRepositorio;
	this.passwordEncoder = passwordEncoder;
	}

	
	// Método para registrar un usuario utilizando un DTO
	@Override
	public Usuario registrarUsuario(UsuarioRegistroDTO registroDTO) {
        // Buscar el tipo de usuario en la base de datos por ID
        TipoUsuario tipoUsuario = tipoUsuarioRepositorio.findById(registroDTO.getTipoUsuario().getId())
                .orElseThrow(() -> new RuntimeException("TipoUsuario no encontrado"));

        // Crear el objeto Usuario
        Usuario usuario = new Usuario();
        usuario.setEmail(registroDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(registroDTO.getPassword()));
        usuario.setTipoUsuario(tipoUsuario);

        // Crear los datos personales y asociarlos al usuario
        DatosPersonales datosPersonales = new DatosPersonales(
                registroDTO.getNombre(),
                registroDTO.getCedula(),
                registroDTO.getDireccion(),
                registroDTO.getTelefono(),
                usuario
        );
        usuario.setDatosPersonales(datosPersonales);

        // Asignar el rol por defecto
        Rol rolUsuario = rolRepositorio.findByNombre("USUARIO")
                .orElseThrow(() -> new RuntimeException("Rol USUARIO no encontrado"));
		usuario.setRoles(Collections.singleton(rolUsuario));

        // Guardar el usuario
		return usuarioRepositorio.save(usuario);
    }

	    @Override
		public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
			// Busca el usuario en la base de datos utilizando el repositorio
			Usuario usuario = usuarioRepositorio.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));
		
			// Crea y devuelve un objeto UserDetails con la información del usuario
			return org.springframework.security.core.userdetails.User.builder()
					.username(usuario.getEmail())
					.password(usuario.getPassword())
					.authorities(mapearAutoridadesRoles(usuario.getRoles()))// Asigna los roles del usuario como autoridades
					.build();
		}

        private Set<? extends org.springframework.security.core.GrantedAuthority> mapearAutoridadesRoles(Set<com.registro.usuarios.modelo.Rol> roles) {
			// Convierte los roles en objetos GrantedAuthority compatibles con Spring Security
        return roles.stream()
            .map(rol -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + rol.getNombre()))
            .collect(Collectors.toSet());

}

	// Método para obtener todos los usuarios de la base de datos	
	@Override
	public List<Usuario> listarUsuarios() {
		// Recupera todos los usuarios almacenados en la base de datos
		return usuarioRepositorio.findAll();
	}

// Método para actualizar un usuario existente
@Override
public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
	// Busca el usuario existente por ID
	Usuario usuarioExistente = usuarioRepositorio.findById(id)
			.orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
	
	// Actualiza los datos del usuario existente con los datos del usuario actualizado
	usuarioExistente.setEmail(usuarioActualizado.getEmail());
	usuarioExistente.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
	usuarioExistente.getDatosPersonales().setNombre(usuarioActualizado.getDatosPersonales().getNombre());
	usuarioExistente.getDatosPersonales().setCedula(usuarioActualizado.getDatosPersonales().getCedula());
	usuarioExistente.getDatosPersonales().setDireccion(usuarioActualizado.getDatosPersonales().getDireccion());
	usuarioExistente.getDatosPersonales().setTelefono(usuarioActualizado.getDatosPersonales().getTelefono());
	
	// Guarda y devuelve el usuario actualizado
	return usuarioRepositorio.save(usuarioExistente);
}

@Override
public List<Usuario> obtenerUsuariosPorTipo(String tipoUsuario) {
        // Recupera y devuelve usuarios filtrados por su tipo
        return usuarioRepositorio.findByTipoUsuario_Nombre(tipoUsuario);
    }

	@Override
    public Usuario buscarPorEmail(String email) {
        return usuarioRepositorio.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
    }
}

