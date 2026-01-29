package co.plageopa.service;

import java.util.Optional;

import co.plageopa.domain.Usuario;

public interface UserService {
	  Usuario create(String username, String email, String rawPassword, String rol);
	  Optional<Usuario> login(String username, String rawPassword);
	  void setPasswordAndForceChange(Integer userId, String rawPassword);
	  void deleteUser(Integer userId);
	}