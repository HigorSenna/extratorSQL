package br.com.swing.application.utils;

public final class SenhaUtils {

	private SenhaUtils() {
	}
	
	public static String getSenha(char[] senha) {
		StringBuilder senhaBuilder = new StringBuilder();
		for(int i = 0; i < senha.length; i++) {
			senhaBuilder.append(senha[i]);
		}
		
		return senhaBuilder.toString();
	}
}
