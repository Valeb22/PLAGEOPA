package co.plageopa.DTO;

public class ResetPasswordRequest {

	public String usernameOrEmail;
	public String newPassword;
	public Integer userId;
	
	public ResetPasswordRequest() {
		// TODO Auto-generated constructor stub
	}

	public String getUsernameOrEmail() {
		return usernameOrEmail;
	}

	public void setUsernameOrEmail(String usernameOrEmail) {
		this.usernameOrEmail = usernameOrEmail;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}
	
}
