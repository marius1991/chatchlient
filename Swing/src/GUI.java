import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JLabel;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JTextPane;


public class GUI extends JFrame {

	private JPanel contentPane;
	private JTextField txtName;
	private JTextField txtBenutzername;
	private JPasswordField pwdPasswort;
	private JPasswordField pwdPasswort1;
	private UseCases usecases = new UseCases();
	private JTextField txtEmpfaenger;
	private JTextField txtAbsender;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		final JButton btnRegistrieren = new JButton("Registrieren");
		btnRegistrieren.setBounds(50, 112, 133, 25);
		contentPane.add(btnRegistrieren);
		
		final JButton btnLogin = new JButton("Login");
		btnLogin.setBounds(244, 112, 117, 25);
		contentPane.add(btnLogin);
		
		txtName = new JTextField();
		txtName.setText("Name");
		txtName.setBounds(94, 66, 114, 19);
		contentPane.add(txtName);
		txtName.setColumns(10);
		txtName.setVisible(false);
		
		final JButton btnLogin_1 = new JButton("Login");
		btnLogin_1.setBounds(220, 63, 117, 25);
		contentPane.add(btnLogin_1);
		btnLogin_1.setVisible(false);
		
		txtBenutzername = new JTextField();
		txtBenutzername.setText("Benutzername");
		txtBenutzername.setBounds(94, 66, 114, 19);
		contentPane.add(txtBenutzername);
		txtBenutzername.setColumns(10);
		txtBenutzername.setVisible(false);
		
		pwdPasswort = new JPasswordField();
		pwdPasswort.setText("Passwort");
		pwdPasswort.setBounds(94, 97, 114, 25);
		contentPane.add(pwdPasswort);
		pwdPasswort.setVisible(false);
		
		pwdPasswort1 = new JPasswordField();
		pwdPasswort1.setText("Passwort");
		pwdPasswort1.setBounds(94, 97, 114, 25);
		contentPane.add(pwdPasswort1);
		pwdPasswort1.setVisible(false);
		
		final JButton btnRegistrieren_1 = new JButton("Registrieren");
		btnRegistrieren_1.setBounds(94, 159, 133, 25);
		contentPane.add(btnRegistrieren_1);
		btnRegistrieren_1.setVisible(false);
		
		final JLabel lblFehler = new JLabel("Fehler!");
		lblFehler.setForeground(Color.RED);
		lblFehler.setBounds(143, 273, 171, 15);
		contentPane.add(lblFehler);
		lblFehler.setVisible(false);

		final JLabel lblSuccess = new JLabel("Erfolgreich angemeldet");
		lblSuccess.setForeground(Color.GREEN);
		lblSuccess.setBounds(143, 273, 171, 15);
		contentPane.add(lblSuccess);
		lblSuccess.setVisible(false);
		
		final JLabel lblName = new JLabel("Name bereits vergeben");
		lblName.setForeground(Color.RED);
		lblName.setBounds(143, 273, 194, 15);
		contentPane.add(lblName);
		lblName.setVisible(false);
		
		final JLabel lblBenutzer = new JLabel("Benutzer existiert nicht");
		lblBenutzer.setForeground(Color.RED);
		lblBenutzer.setBounds(143, 245, 194, 15);
		contentPane.add(lblBenutzer);
		lblBenutzer.setVisible(false);
		
		final JLabel lblSuccess1 = new JLabel("Login erfolgreich!");
		lblSuccess1.setForeground(Color.GREEN);
		lblSuccess1.setBounds(143, 245, 171, 15);
		contentPane.add(lblSuccess1);
		lblSuccess1.setVisible(false);
		
		final JLabel lblSuccess2 = new JLabel("Versand erfolgreich!");
		lblSuccess2.setForeground(Color.GREEN);
		lblSuccess2.setBounds(143, 245, 171, 15);
		contentPane.add(lblSuccess2);
		lblSuccess2.setVisible(false);
		
		txtEmpfaenger = new JTextField();
		txtEmpfaenger.setText("Empfänger");
		txtEmpfaenger.setBounds(69, 34, 114, 19);
		contentPane.add(txtEmpfaenger);
		txtEmpfaenger.setColumns(10);
		txtEmpfaenger.setVisible(false);
		
		final JTextPane txtpnNachricht = new JTextPane();
		txtpnNachricht.setText("Nachricht");
		txtpnNachricht.setBounds(60, 66, 144, 125);
		contentPane.add(txtpnNachricht);
		txtpnNachricht.setVisible(false);
		
		final JButton btnSend = new JButton("Send");
		btnSend.setBounds(70, 208, 117, 25);
		contentPane.add(btnSend);
		btnSend.setVisible(false);
		
		final JButton btnAbrufen = new JButton("Abrufen");
		btnAbrufen.setBounds(272, 63, 117, 25);
		contentPane.add(btnAbrufen);
		btnAbrufen.setVisible(false);
		
		txtAbsender = new JTextField();
		txtAbsender.setText("Absender");
		txtAbsender.setBounds(272, 115, 114, 19);
		contentPane.add(txtAbsender);
		txtAbsender.setColumns(10);
		txtAbsender.setVisible(false);
		
		final JTextPane txtpnEmpfnachricht = new JTextPane();
		txtpnEmpfnachricht.setText("EmpfNachricht");
		txtpnEmpfnachricht.setBounds(272, 149, 125, 70);
		contentPane.add(txtpnEmpfnachricht);
		txtpnEmpfnachricht.setVisible(false);
		
		final JLabel lblVon = new JLabel("Von:");
		lblVon.setBounds(272, 102, 70, 15);
		contentPane.add(lblVon);
		lblVon.setVisible(false);
		
		final JButton btnLogout = new JButton("Logout");
		btnLogout.setBounds(321, 0, 117, 25);
		contentPane.add(btnLogout);
		btnLogout.setVisible(false);





		
		
		//ActionListener für btnRegistrieren
        btnRegistrieren.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                //System.out.println("You clicked the button register");
                btnRegistrieren.setVisible(false);
                btnLogin.setVisible(false);
                txtBenutzername.setVisible(true);
                pwdPasswort.setVisible(true);
                btnRegistrieren_1.setVisible(true);
            }
        });
        
        //ActionListener für btnLogin
        btnLogin.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                //System.out.println("You clicked the button login");
                btnRegistrieren.setVisible(false);
                btnLogin.setVisible(false);
                txtName.setVisible(true);
                btnLogin_1.setVisible(true);
        		pwdPasswort1.setVisible(true);
            }
        });    
        
        //ActionListener für btnLogin_1
        btnLogin_1.addActionListener(new ActionListener() {
        	 
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                //System.out.println("You clicked the button login1");
                int success = usecases.login(txtName.getText(),pwdPasswort1.getPassword());
                //System.out.println(success);
            	lblFehler.setVisible(false);
        		lblSuccess1.setVisible(false);
        		lblSuccess.setVisible(false);
        		txtEmpfaenger.setVisible(false);
        		txtpnNachricht.setVisible(false);
        		btnSend.setVisible(false);
        		lblBenutzer.setVisible(false);
            	lblFehler.setVisible(false);
                if (success == 1) {
            		lblSuccess1.setVisible(true);
            		txtEmpfaenger.setVisible(true);
            		txtpnNachricht.setVisible(true);
            		btnSend.setVisible(true);
            		txtName.setVisible(false);
                    btnLogin_1.setVisible(false);
            		pwdPasswort1.setVisible(false);
            		btnAbrufen.setVisible(true);
            		txtAbsender.setVisible(true);
            		txtpnEmpfnachricht.setVisible(true);
            		lblVon.setVisible(true);
            		btnLogout.setVisible(true);
                }
                else {
                	lblFehler.setVisible(true);
                }
            }
        });    
        
		//ActionListener für btnRegistrieren_1
        btnRegistrieren_1.addActionListener(new ActionListener() {
 
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                //System.out.println("You clicked the button register1");
                int success = usecases.register(txtBenutzername.getText(),pwdPasswort.getPassword());
                //System.out.println(success);
                txtName.setVisible(false);
                pwdPasswort1.setVisible(false);
                btnLogin_1.setVisible(false);
                lblSuccess.setVisible(false);
            	lblName.setVisible(false);
            	lblFehler.setVisible(false);
                if (success == 1) {
                	txtBenutzername.setVisible(false);
                    pwdPasswort.setVisible(false);
                    btnRegistrieren_1.setVisible(false);
                    txtName.setVisible(true);
                    pwdPasswort1.setVisible(true);
                    btnLogin_1.setVisible(true);
                    lblSuccess.setVisible(true);
                }
                else {
                	lblFehler.setVisible(true);
                }
            }
            
        });
        
      //ActionListener für btnSend
        btnSend.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                //System.out.println("You clicked the button send");
                int success = usecases.sendMessage(txtName.getText(), txtEmpfaenger.getText(), txtpnNachricht.getText());
        		lblSuccess1.setVisible(false);
        		lblSuccess2.setVisible(false);
        		lblBenutzer.setVisible(false);
        		lblFehler.setVisible(false);
                if (success == 1) {
            		lblSuccess2.setVisible(true);
                }
                else {
            		lblFehler.setVisible(true);
                }

            }
        });    
		
        //ActionListerner für btnLogout
        btnLogout.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e)
            {
                //Execute when button is pressed
                //System.out.println("You clicked the button Logout");
        		lblSuccess1.setVisible(false);
        		txtEmpfaenger.setVisible(false);
        		txtpnNachricht.setVisible(false);
        		btnSend.setVisible(false);
        		btnAbrufen.setVisible(false);
        		txtAbsender.setVisible(false);
        		txtpnEmpfnachricht.setVisible(false);
        		lblVon.setVisible(false);
        		btnLogout.setVisible(false);
        		btnRegistrieren.setVisible(true);
        		btnLogin.setVisible(true);
            }
        });   
        
      //ActionListerner für btnLogout
        btnAbrufen.addActionListener(new ActionListener() {
            
            public void actionPerformed(ActionEvent e)
            {
            	//Execute when button is pressed
                System.out.println("You clicked the button Abrufen");
                ArrayList<String[]> messages = usecases.receiveMessage(txtName.getText());
                txtAbsender.setText(messages.get(0)[0]);
        		txtpnEmpfnachricht.setText(messages.get(0)[1]);
            }
        });
       
        
	}
}
