package com.al.fiap.cs.account.core.domain.account;

import com.al.fiap.cs.account.core.domain.exceptions.InvalidDocumentException;

import java.util.Objects;

public record Document(String value) {

	public Document {
		Objects.requireNonNull(value, "document");
		String normalized = value.trim().replaceAll("\\D", "");
		if (!isValidCpf(normalized)) {
			throw new InvalidDocumentException("Document must be a valid CPF");
		}
		value = normalized;
	}

	private static boolean isValidCpf(String cpf) {
		if (cpf.length() != 11) {
			return false;
		}
		if (cpf.chars().distinct().count() == 1) {
			return false;
		}
		return checkDigit(cpf, 9) == digitAt(cpf, 9) && checkDigit(cpf, 10) == digitAt(cpf, 10);
	}

	private static int checkDigit(String cpf, int length) {
		int sum = 0;
		int weight = length + 1;
		for (int index = 0; index < length; index++) {
			sum += digitAt(cpf, index) * weight;
			weight--;
		}
		int remainder = sum % 11;
		if (remainder < 2) {
			return 0;
		}
		return 11 - remainder;
	}

	private static int digitAt(String cpf, int index) {
		return cpf.charAt(index) - '0';
	}
}
