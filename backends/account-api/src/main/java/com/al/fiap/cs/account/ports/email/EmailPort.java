package com.al.fiap.cs.account.ports.email;

public interface EmailPort {
	void sendPasswordRecoveryCode(String toEmail, String code, String recoveryUrl);
}
