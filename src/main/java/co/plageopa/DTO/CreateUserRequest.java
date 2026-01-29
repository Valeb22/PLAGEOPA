package co.plageopa.DTO;

public class CreateUserRequest {
  public String username;
  public String email;
  public String password;
  public String rol;

  public CreateUserRequest() {
	// TODO Auto-generated constructor stub
}

public String getUsername() {
	return username;
}

public void setUsername(String username) {
	this.username = username;
}

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getPassword() {
	return password;
}

public void setPassword(String password) {
	this.password = password;
}

public String getRol() {
	return rol;
}

public void setRol(String rol) {
	this.rol = rol;
}
  
}

