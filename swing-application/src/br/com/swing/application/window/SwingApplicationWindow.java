package br.com.swing.application.window;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import br.com.swing.application.exception.RequiredException;
import br.com.swing.application.utils.SenhaUtils;

public class SwingApplicationWindow {
	
	private static final String CAMPO_OBRIGATORIO = "Campo(s) [ %s ] não imformado(s)";

	private JFrame frame;
	private JTextField textFieldConexao;
	private JTextField textFieldUsuario;
	private JPasswordField passwordField;
	private JLabel lblParmetrosIn;
	private JLabel lblQueryIn;
	private JTextArea textAreaQuery;
	private JTextArea textAreaParams;
	private JLabel lblResultado;
	private JTextArea textAreaResultado;
	private static Connection connection;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SwingApplicationWindow window = new SwingApplicationWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Create the application.
	 */
	public SwingApplicationWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		configureFrame();
		configureURLConexao();
		configureUsuarioConexao();
		configureSenha();
		configureParamsIN();		
		configureQuery();
		configureResultado();
		
		JButton btnPesquisar = new JButton("Pesquisar");
		btnPesquisar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					validarRequired();
					realizarConsulta();
				} 
				catch (RequiredException requiredException) {
					JOptionPane.showMessageDialog(frame, requiredException.getMessage());
				}
			}
		});
		btnPesquisar.setBounds(506, 585, 117, 25);
		frame.getContentPane().add(btnPesquisar);
		
		JButton btnLimpar = new JButton("Limpar");
		btnLimpar.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				textAreaParams.setText("");
				textAreaQuery.setText("");
				textAreaResultado.setText("");
			}
		});
		btnLimpar.setBounds(354, 585, 117, 25);
		frame.getContentPane().add(btnLimpar);
	}
	
	private void validarRequired() throws RequiredException {
		StringBuilder camposBuilder = new StringBuilder();
		if(textFieldConexao.getText().trim().isEmpty()) {
			camposBuilder.append(" ")
						 .append("URL Conexão,");
		}
		if(textFieldUsuario.getText().trim().isEmpty()) {
			camposBuilder.append(" ")
			 			 .append("Usuário banco,");
		}
		if(SenhaUtils.getSenha(passwordField.getPassword()).trim().isEmpty()) {
			camposBuilder.append(" ")
			 			 .append("Senha");
		}
		
		if(!camposBuilder.toString().trim().isEmpty()) {
			throw new RequiredException(String.format(CAMPO_OBRIGATORIO, camposBuilder.toString()));
		}
	}
	
	private void realizarConsulta() {
		try {
			conectar();
			if(connection != null) {
				List<String> parametros = getParams();
				String resultado = extrairResultado(parametros);
				textAreaResultado.setText(resultado);
				connection.close();
			}
		}
		catch (SQLException e) {
			String mensagem = "Erro ao conectar-se com o banco de dados---> ERRO [ %s ]";
			JOptionPane.showMessageDialog(frame, String.format(mensagem, e.getMessage()));
		} 
		catch (ClassNotFoundException e) {
			String mensagem = "Driver oracle nao encontrado---> ERRO [ %s ]";
			JOptionPane.showMessageDialog(frame, String.format(mensagem, e.getMessage()));
		} 
	}

	private String extrairResultado(List<String> parametros) throws SQLException {
		StringBuilder resultadoBuilder = new StringBuilder();
		for(String parametro: parametros) {
			ResultSet resultado = executarQuery(parametro);
			if(!resultado.next()) {
				resultadoBuilder.append(" ")
								.append(parametro);
			}
			aguardar();
		}
		
		return resultadoBuilder.toString();
	}
	
	private void aguardar() {
		try {
			Thread.sleep(1000l);
		} 
		catch (InterruptedException exception) {
			String mensagem = "Erro ao aguardar ERRO---> [ %s ]";
			JOptionPane.showMessageDialog(frame, String.format(mensagem, exception.getMessage()));
		}
	}

	private ResultSet executarQuery(String parametro) throws SQLException {
		Statement statement = connection.createStatement();  
		String queryFormat = getQuery(parametro);
		return statement.executeQuery(queryFormat);
	}

	private String getQuery(String parametro) {
		String parametrosQuery = getStringQueryParam(parametro);
		return textAreaQuery.getText()
							.concat("(")
							.concat(parametrosQuery)
							.concat(")");
	}
	
	private String getStringQueryParam(String parametro) {
		return new StringBuilder().append("'")
								  .append(parametro)
								  .append("'")		
								  .toString();
	}
	
	private List<String> getParams() {
		String[] ids = textAreaParams.getText().split(" ");
		List<String> parametros = new ArrayList<String>();
		for(String id : ids) {
			if(!id.trim().equals("")) {
				parametros.add(id);
			}
		}
		
		return parametros;
	}

	private void conectar() throws SQLException, ClassNotFoundException {
		if(connection == null || connection.isClosed()) {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			String stringConexao = textFieldConexao.getText();
			String usuario = textFieldUsuario.getText();
			String senha = SenhaUtils.getSenha(passwordField.getPassword());
			connection = DriverManager.getConnection(stringConexao, usuario, senha);
		}
	}

	private void configureResultado() {
		lblResultado = new JLabel("Resultado");
		lblResultado.setBounds(23, 456, 114, 15);
		frame.getContentPane().add(lblResultado);
		
		textAreaResultado = new JTextArea();
		textAreaResultado.setLineWrap(true);
		textAreaResultado.setBorder(new LineBorder(new Color(0, 0, 0)));
		textAreaResultado.setBounds(12, 513, 600, 57);
		frame.getContentPane().add(textAreaResultado);
	}

	private void configureQuery() {
		lblQueryIn = new JLabel("Query IN");
		lblQueryIn.setBounds(12, 319, 70, 19);
		frame.getContentPane().add(lblQueryIn);
		
		textAreaQuery = new JTextArea();
		textAreaQuery.setLineWrap(true);
		textAreaQuery.setBorder(new LineBorder(new Color(0, 0, 0)));
		textAreaQuery.setBounds(12, 361, 600, 45);
		frame.getContentPane().add(textAreaQuery);
	}

	private void configureParamsIN() {
		lblParmetrosIn = new JLabel("Parâmetros IN");
		lblParmetrosIn.setBounds(12, 172, 103, 15);
		frame.getContentPane().add(lblParmetrosIn);
		
		textAreaParams = new JTextArea();
		textAreaParams.setLineWrap(true);
		textAreaParams.setBorder(new LineBorder(new Color(0, 0, 0)));
		textAreaParams.setBounds(12, 225, 600, 59);
		frame.getContentPane().add(textAreaParams);
	}

	private void configureSenha() {
		JLabel lblSenha = new JLabel("Senha");
		lblSenha.setBounds(12, 111, 70, 15);
		frame.getContentPane().add(lblSenha);
		
		passwordField = new JPasswordField();
		passwordField.setBounds(117, 109, 212, 19);
		frame.getContentPane().add(passwordField);
	}

	private void configureUsuarioConexao() {
		JLabel lblUsurioBanco = new JLabel("Usuário banco");
		lblUsurioBanco.setBounds(12, 68, 103, 15);
		frame.getContentPane().add(lblUsurioBanco);
		
		textFieldUsuario = new JTextField();
		textFieldUsuario.setBounds(117, 66, 212, 19);
		frame.getContentPane().add(textFieldUsuario);
		textFieldUsuario.setColumns(10);
	}

	private void configureURLConexao() {
		textFieldConexao = new JTextField();
		textFieldConexao.setBounds(117, 26, 212, 19);
		frame.getContentPane().add(textFieldConexao);
		textFieldConexao.setColumns(10);
		
		JLabel lblUrlConexo = new JLabel("URL conexão");
		lblUrlConexo.setBounds(12, 28, 114, 15);
		frame.getContentPane().add(lblUrlConexo);
	}

	private void configureFrame() {
		frame = new JFrame();
		frame.setBounds(new Rectangle(0, 0, 0, 0));
		frame.setLocation(new Point(0, 0));
		frame.setResizable(false);
		frame.setBounds(100, 100, 664, 692);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
	}
}
