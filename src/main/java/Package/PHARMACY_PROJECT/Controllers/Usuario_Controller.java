package Package.PHARMACY_PROJECT.Controllers;

import Package.PHARMACY_PROJECT.Models.Usuario_Model;
import Package.PHARMACY_PROJECT.Response;
import Package.PHARMACY_PROJECT.Services.Usuario_Services;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/usuario")
public class Usuario_Controller {
    private static final Logger logger = LoggerFactory.getLogger(Usuario_Controller.class);

    @Autowired
    private Usuario_Services usersServices;

    @Autowired
    private JavaMailSender mailSender;


    @GetMapping
    public ResponseEntity<Response<List<Usuario_Model>>> getAllUsuarios() {
        List<Usuario_Model> usuarios = usersServices.findAll();
        Response<List<Usuario_Model>> response = new Response<>("200", "Usuarios obtenidos satisfactoriamente", usuarios, "USUARIOS_GET_OK");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Response<Usuario_Model>> login(@RequestBody Usuario_Model loginRequest) {
        Optional<Usuario_Model> usuario = Optional.empty();

        // Comprobamos si el "username" es un correo electrónico
        if (isValidEmail(loginRequest.getUsername())) {
            // Si es un correo electrónico, buscamos por correo
            usuario = usersServices.findByCorreoElectronico(loginRequest.getUsername());
        } else {
            // Si no es un correo, buscamos por nombre de usuario
            usuario = usersServices.findByUsername(loginRequest.getUsername());
        }

        logger.info("Usuario encontrado: {}", usuario.orElse(null)); // Loguea el usuario (si está presente) o null
        logger.info("¿Usuario y contraseña coinciden?: {}", usuario.isPresent() && usuario.get().getPassword().equals(loginRequest.getPassword()));

        if (usuario.isPresent() && usuario.get().getPassword().equals(loginRequest.getPassword())) {
            Response<Usuario_Model> response = new Response<>("200", "Login exitoso", usuario.get(), "LOGIN_SUCCESS");
            return ResponseEntity.ok(response);
        } else {
            Response<Usuario_Model> response = new Response<>("401", "Usuario o contraseña incorrectos", null, "LOGIN_FAILURE");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }


    @PostMapping
    public ResponseEntity<Response<Usuario_Model>> saveUsuarios(@RequestBody Usuario_Model usuario) {
        try {
            if (usuario.getNombreCompleto() == null || usuario.getNombreCompleto().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "El nombre no puede ser nulo", null, "NOMBRE_NULO"));
            }

            if (usuario.getCorreoElectronico() == null || usuario.getCorreoElectronico().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "El correo electrónico no puede ser nulo", null, "CORREO_NULO"));
            }

            // Verificar que todos los atributos requeridos, excepto token, no sean nulos
            if (usuario.getUsername() == null || usuario.getUsername().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "El ussername no puede ser nulo", null, "USERNAME_NULO"));
            }

            if (usuario.getPassword() == null || usuario.getPassword().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "la contraseña no puede ser nulo", null, "NOMBRE_NULO"));
            }
            if (usuario.getRol() == null || usuario.getRol().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "El rol no puede ser nulo", null, "NOMBRE_NULO"));
            }

            // Verificar si el nombre de usuario ya existe en la base de datos
            Optional<Usuario_Model> usuarioExistentePorUsername = usersServices.findByUsername(usuario.getUsername());
            if (usuarioExistentePorUsername.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "El nombre de usuario ya está en uso", null, "USERNAME_DUPLICADO"));
            }

            // Validar el formato del correo
            if (!esCorreoValido(usuario.getCorreoElectronico())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "El formato del correo es inválido", null, "CORREO_INVALIDO"));
            }

            // Verificar si el correo ya está registrado
            Optional<Usuario_Model> usuarioExistentePorCorreo = usersServices.findByCorreoElectronico(usuario.getCorreoElectronico());
            if (usuarioExistentePorCorreo.isPresent()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "El correo electrónico ya está en uso", null, "CORREO_DUPLICADO"));
            }

            // Si todo es válido, guardar el usuario
            Usuario_Model usuarioNuevo = usersServices.save(usuario);
            Response<Usuario_Model> response = new Response<>("200", "Usuario creado satisfactoriamente", usuarioNuevo, "USUARIO_INSERT_OK");
            return ResponseEntity.created(new URI("/usuario/" + usuarioNuevo.getId())).body(response);

        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new Response<>("400", "Error al crear usuario: " + e.getMessage(), null, "USUARIO_INSERT_ERROR"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("500", "Error interno del servidor: " + e.getMessage(), null, "INTERNAL_SERVER_ERROR"));
        }
    }


    public boolean esCorreoValido(String correo) {
        // Expresión regular para validar el formato del correo
        String regexCorreo = "^[A-Za-z0-9+_.-]+@(.+)$";
        Pattern pattern = Pattern.compile(regexCorreo);
        return pattern.matcher(correo).matches();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteUsuario(@PathVariable Long id) {
        try {
            Optional<Usuario_Model> usuario = usersServices.findById(id);
            if (usuario.isPresent()) {
                usersServices.deleteById(id);
                // Si la eliminación fue exitosa, devolvemos un mensaje indicando que fue exitosa
                Response<Void> response = new Response<>("200", "Usuario eliminado satisfactoriamente", null, "USER_DELETE_OK");
                return ResponseEntity.ok().body(response);
            } else {
                // Si el usuario no fue encontrado, devolvemos un mensaje específico
                Response<Void> response = new Response<>("404", "El usuario no fue encontrado", null, "USER_NOT_FOUND");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
        } catch (Exception e) {
            // Si ocurre un error inesperado, devolvemos un mensaje de error interno del servidor
            Response<Void> response = new Response<>("500", "Error interno del servidor: " + e.getMessage(), null, "INTERNAL_SERVER_ERROR");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<Usuario_Model>> update(@PathVariable Long id, @RequestBody Usuario_Model newUser) {
        // Verificar si el ID no es nulo
        if (id == null) {
            // Si el ID es nulo, retornar un ResponseEntity con un mensaje de error
            Response<Usuario_Model> response = new Response<>("400", "El ID no puede ser nulo", null, "ID_NULL_ERROR");
            return ResponseEntity.badRequest().body(response);
        }

        // Recuperar el usuario existente por su ID
        Optional<Usuario_Model> optionalUser = usersServices.findById(id);

        if (optionalUser.isPresent()) {
            Usuario_Model existingUser = optionalUser.get();

            // Actualizar los atributos del usuario existente con los nuevos valores
            existingUser.setUsername(newUser.getUsername());
            existingUser.setPassword(newUser.getPassword());

            // Guardar el usuario actualizado en el repositorio
            Usuario_Model updatedUser = usersServices.save(existingUser);

            // Crear un objeto Response con el usuario actualizado y retornarlo
            Response<Usuario_Model> response = new Response<>("200", "Usuario actualizado satisfactoriamente", updatedUser, "USUARIO_UPDATE_OK");
            return ResponseEntity.ok(response);
        } else {
            // Si el usuario no existe, retornar un ResponseEntity con un mensaje de error
            Response<Usuario_Model> response = new Response<>("404", "Usuario no encontrado con ID: " + id, null, "USER_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Response<List<String>>> forgotPassword(@RequestBody Map<String, Object> payload) {
        // Obtener el correo electrónico desde el payload
        String correoElectronico = (String) payload.get("correoElectronico");
        List<String> errores = new ArrayList<>();

        try {
            // Validar que el correo electrónico no esté vacío
            if (correoElectronico == null || correoElectronico.isEmpty()) {
                errores.add("El correo electrónico no puede estar vacío.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "Correo electrónico vacío.", errores, "EMPTY_EMAIL"));
            }

            // Validar el formato del correo electrónico
            if (!isValidEmail(correoElectronico)) {
                errores.add(correoElectronico);
                System.out.println("Error: El correo electrónico " + correoElectronico + " tiene un formato inválido.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "Correo electrónico con formato inválido.", errores, "INVALID_EMAIL_FORMAT"));
            }

            // Buscar al usuario por correo electrónico
            Optional<Usuario_Model> usuario = usersServices.findByCorreoElectronico(correoElectronico);
            if (usuario.isPresent()) {
                Usuario_Model user = usuario.get();

                // Generar un código de recuperación
                String recoveryCode = generateRecoveryCode(); // Método que puedes implementar
                user.setToken(recoveryCode); // Usamos el campo 'token' para almacenar el código
                usersServices.save(user);

                // Enviar el correo electrónico con el código de recuperación
                // Enviar el correo electrónico con el código de recuperación
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, false);

                helper.setTo(correoElectronico);
                helper.setSubject("Recuperación de Contraseña");
                helper.setText("Su código de recuperación es: " + recoveryCode);

                mailSender.send(message);



                try {
                    mailSender.send(message);
                } catch (MailException e) {
                    errores.add(correoElectronico);
                    System.out.println("Error: Fallo al enviar el correo electrónico a " + correoElectronico + ". Detalle del error: " + e.getMessage() + ".");
                    e.printStackTrace(); // Imprime el stack trace para obtener más detalles
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body(new Response<>("500", "Error al enviar el correo electrónico de recuperación.", errores, "MAIL_SENDING_ERROR"));
                }

                // Respuesta exitosa
                return ResponseEntity.ok(new Response<>("200", "Código de recuperación enviado al correo electrónico.", null, "RECOVERY_CODE_SENT"));
            } else {
                errores.add(correoElectronico);
                System.out.println("Error: No se encontró un usuario con el correo electrónico " + correoElectronico + ".");
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "No se encontró un usuario con el correo electrónico proporcionado.", errores, "USER_NOT_FOUND"));
            }
        } catch (Exception e) {
            errores.add("Ocurrió un error inesperado: " + e.getMessage());
            System.out.println("Error: Excepción inesperada al procesar la solicitud de recuperación de contraseña. Detalle del error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("500", "Error al procesar la solicitud de recuperación de contraseña.", errores, "INTERNAL_ERROR"));
        }
    }


    public String generateRecoveryCode() {
        // Generar un código de 6 dígitos
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Genera un número aleatorio entre 100000 y 999999
        return String.valueOf(code);
    }



    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }


    @PostMapping("/reset-password")
    public ResponseEntity<Response<String>> resetPassword(@RequestParam String recoveryCode, @RequestParam String newPassword) {
        Optional<Usuario_Model> userOptional = usersServices.findByToken(recoveryCode);

        if (userOptional.isPresent()) {
            Usuario_Model user = userOptional.get();
            // Verificar el código de recuperación
            if (user.getToken().equals(recoveryCode)) {
                user.setPassword(newPassword); // Actualiza la contraseña
                user.setToken(null); // Elimina el código de recuperación después de usarlo
                usersServices.save(user);
                return ResponseEntity.ok(new Response<>("200", "Contraseña restablecida correctamente", null, "PASSWORD_RESET_SUCCESS"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>("400", "Código de recuperación incorrecto", null, "INVALID_RECOVERY_CODE"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>("404", "Usuario no encontrado", null, "USER_NOT_FOUND"));
        }
    }

    @PostMapping("/validate-recovery-code")
    public ResponseEntity<Response<String>> validateRecoveryCode(@RequestBody Map<String, String> payload) {
        String code = payload.get("code");
        boolean isValid = verifyCode(code);

        if (isValid) {
            return ResponseEntity.ok(new Response<>("200", "Código válido. Puede restablecer su contraseña.", null, "CODE_VALID"));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>("400", "Código inválido.", null, "INVALID_CODE"));
        }
    }

    private boolean verifyCode(String code) {
        // Busca el código en la base de datos
        Optional<Usuario_Model> user = usersServices.findByToken(code);

        // Si el usuario con el código existe y el token no ha expirado, es válido
        if (user.isPresent()) {
            return true;
        }

        return false; // El código no es válido
    }

    private String generateRecoveryToken() {
        return UUID.randomUUID().toString();
    }


    @PutMapping("/update-password")
    public ResponseEntity<Response<String>> updatePassword(@RequestBody Map<String, String> payload) {
        String recoveryCode = payload.get("recoveryCode");
        String newPassword = payload.get("newPassword");

        Optional<Usuario_Model> userOptional = usersServices.findByToken(recoveryCode);

        if (userOptional.isPresent()) {
            Usuario_Model user = userOptional.get();
            // Verificar el código de recuperación
            if (user.getToken().equals(recoveryCode)) {
                user.setPassword(newPassword); // Actualiza la contraseña
                user.setToken(null); // Elimina el código de recuperación después de usarlo
                usersServices.save(user);
                return ResponseEntity.ok(new Response<>("200", "Contraseña actualizada correctamente", null, "PASSWORD_UPDATED_SUCCESS"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new Response<>("400", "Código de recuperación incorrecto", null, "INVALID_RECOVERY_CODE"));
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>("404", "Usuario no encontrado", null, "USER_NOT_FOUND"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<Usuario_Model>> getUsuarioById(@PathVariable Long id) {
        Optional<Usuario_Model> usuario = usersServices.findById(id);

        if (usuario.isPresent()) {
            Response<Usuario_Model> response = new Response<>("200", "Usuario encontrado", usuario.get(), "USER_FOUND");
            return ResponseEntity.ok(response);
        } else {
            Response<Usuario_Model> response = new Response<>("404", "Usuario no encontrado", null, "USER_NOT_FOUND");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/update-password/{id}")
    public ResponseEntity<Response<String>> updatePasswordById(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        try {
            // Validar que la nueva contraseña esté presente en el payload
            String newPassword = payload.get("newPassword");
            if (newPassword == null || newPassword.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new Response<>("400", "La nueva contraseña no puede estar vacía", null, "EMPTY_PASSWORD"));
            }

            // Buscar al usuario por ID
            Optional<Usuario_Model> userOptional = usersServices.findById(id);

            if (userOptional.isPresent()) {
                Usuario_Model user = userOptional.get();
                // Actualizar la contraseña del usuario
                user.setPassword(newPassword);
                usersServices.save(user);

                return ResponseEntity.ok(new Response<>("200", "Contraseña actualizada correctamente", null, "PASSWORD_UPDATED"));
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new Response<>("404", "Usuario no encontrado", null, "USER_NOT_FOUND"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new Response<>("500", "Error al actualizar la contraseña: " + e.getMessage(), null, "UPDATE_ERROR"));
        }
    }

}