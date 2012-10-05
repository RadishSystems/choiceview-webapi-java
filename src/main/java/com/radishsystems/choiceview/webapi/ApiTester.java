package com.radishsystems.choiceview.webapi;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.SpringLayout;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JButton;

public class ApiTester {

	private JFrame frmWebApiTester;
	private JTextField txtNewCallerId;
	private JTextField txtNewCallId;
	private JTextField dispSessionId;
	private JTextField dispCallerId;
	private JTextField dispCallId;
	private JTextField dispConnectionState;
	private JTextField dispNetworkType;
	private JTextField dispNetworkQuality;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ApiTester window = new ApiTester();
					window.frmWebApiTester.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public ApiTester() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmWebApiTester = new JFrame();
		frmWebApiTester.setResizable(false);
		frmWebApiTester.setTitle("Web API Tester");
		frmWebApiTester.setBounds(100, 100, 325, 413);
		frmWebApiTester.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		SpringLayout springLayout = new SpringLayout();
		frmWebApiTester.getContentPane().setLayout(springLayout);
		
		JLabel lblMobileClientId = new JLabel("Mobile Client ID (Caller ID)");
		springLayout.putConstraint(SpringLayout.WEST, lblMobileClientId, 10, SpringLayout.WEST, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(lblMobileClientId);
		
		txtNewCallerId = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, lblMobileClientId, 6, SpringLayout.NORTH, txtNewCallerId);
		springLayout.putConstraint(SpringLayout.NORTH, txtNewCallerId, 4, SpringLayout.NORTH, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, txtNewCallerId, -8, SpringLayout.EAST, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(txtNewCallerId);
		txtNewCallerId.setColumns(10);
		
		JLabel lblIvrCallId = new JLabel("IVR Call ID");
		springLayout.putConstraint(SpringLayout.NORTH, lblIvrCallId, 18, SpringLayout.SOUTH, lblMobileClientId);
		frmWebApiTester.getContentPane().add(lblIvrCallId);
		
		txtNewCallId = new JTextField();
		springLayout.putConstraint(SpringLayout.EAST, lblIvrCallId, -6, SpringLayout.WEST, txtNewCallId);
		springLayout.putConstraint(SpringLayout.NORTH, txtNewCallId, 6, SpringLayout.SOUTH, txtNewCallerId);
		springLayout.putConstraint(SpringLayout.WEST, txtNewCallId, 0, SpringLayout.WEST, txtNewCallerId);
		springLayout.putConstraint(SpringLayout.EAST, txtNewCallId, 0, SpringLayout.EAST, txtNewCallerId);
		frmWebApiTester.getContentPane().add(txtNewCallId);
		txtNewCallId.setColumns(10);
		
		JButton btnStartSession = new JButton("Start Session");
		springLayout.putConstraint(SpringLayout.WEST, btnStartSession, 191, SpringLayout.WEST, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(btnStartSession);
		
		JButton btnSendUrl = new JButton("Send URL");
		springLayout.putConstraint(SpringLayout.NORTH, btnSendUrl, 0, SpringLayout.NORTH, btnStartSession);
		springLayout.putConstraint(SpringLayout.WEST, btnSendUrl, 0, SpringLayout.WEST, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnSendUrl, -45, SpringLayout.SOUTH, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnSendUrl, 21, SpringLayout.WEST, lblIvrCallId);
		frmWebApiTester.getContentPane().add(btnSendUrl);
		
		JButton btnSendText = new JButton("Send Text");
		springLayout.putConstraint(SpringLayout.WEST, btnSendText, 0, SpringLayout.WEST, btnSendUrl);
		springLayout.putConstraint(SpringLayout.SOUTH, btnSendText, -10, SpringLayout.SOUTH, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(btnSendText);
		
		JButton btnGetProperties = new JButton("Get Properties");
		springLayout.putConstraint(SpringLayout.WEST, btnGetProperties, 193, SpringLayout.WEST, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.SOUTH, btnGetProperties, -10, SpringLayout.SOUTH, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnGetProperties, 0, SpringLayout.EAST, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.EAST, btnSendText, -61, SpringLayout.WEST, btnGetProperties);
		springLayout.putConstraint(SpringLayout.EAST, btnStartSession, 0, SpringLayout.EAST, btnGetProperties);
		springLayout.putConstraint(SpringLayout.NORTH, btnSendText, 0, SpringLayout.NORTH, btnGetProperties);
		springLayout.putConstraint(SpringLayout.SOUTH, btnStartSession, -6, SpringLayout.NORTH, btnGetProperties);
		frmWebApiTester.getContentPane().add(btnGetProperties);
		
		JLabel lblSessionId = new JLabel("Session ID");
		springLayout.putConstraint(SpringLayout.EAST, lblSessionId, -150, SpringLayout.EAST, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(lblSessionId);
		
		dispSessionId = new JTextField();
		dispSessionId.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, dispSessionId, 28, SpringLayout.SOUTH, txtNewCallId);
		springLayout.putConstraint(SpringLayout.EAST, dispSessionId, -12, SpringLayout.EAST, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, lblSessionId, 6, SpringLayout.NORTH, dispSessionId);
		frmWebApiTester.getContentPane().add(dispSessionId);
		dispSessionId.setColumns(10);
		
		JLabel lblCallerId = new JLabel("Caller ID");
		springLayout.putConstraint(SpringLayout.EAST, lblCallerId, -150, SpringLayout.EAST, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(lblCallerId);
		
		dispCallerId = new JTextField();
		dispCallerId.setEditable(false);
		springLayout.putConstraint(SpringLayout.NORTH, dispCallerId, 6, SpringLayout.SOUTH, dispSessionId);
		springLayout.putConstraint(SpringLayout.EAST, dispCallerId, -10, SpringLayout.EAST, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, lblCallerId, 6, SpringLayout.NORTH, dispCallerId);
		frmWebApiTester.getContentPane().add(dispCallerId);
		dispCallerId.setColumns(10);
		
		JLabel lblCallId = new JLabel("Call ID");
		springLayout.putConstraint(SpringLayout.EAST, lblCallId, -150, SpringLayout.EAST, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(lblCallId);
		
		dispCallId = new JTextField();
		dispCallId.setEditable(false);
		springLayout.putConstraint(SpringLayout.EAST, dispCallId, -12, SpringLayout.EAST, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, lblCallId, 6, SpringLayout.NORTH, dispCallId);
		springLayout.putConstraint(SpringLayout.NORTH, dispCallId, 6, SpringLayout.SOUTH, dispCallerId);
		frmWebApiTester.getContentPane().add(dispCallId);
		dispCallId.setColumns(10);
		
		JLabel lblConnectionState = new JLabel("Connection State");
		springLayout.putConstraint(SpringLayout.EAST, lblConnectionState, -150, SpringLayout.EAST, frmWebApiTester.getContentPane());
		frmWebApiTester.getContentPane().add(lblConnectionState);
		
		dispConnectionState = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, btnStartSession, 93, SpringLayout.SOUTH, dispConnectionState);
		dispConnectionState.setEditable(false);
		springLayout.putConstraint(SpringLayout.EAST, dispConnectionState, -10, SpringLayout.EAST, frmWebApiTester.getContentPane());
		springLayout.putConstraint(SpringLayout.NORTH, lblConnectionState, 6, SpringLayout.NORTH, dispConnectionState);
		springLayout.putConstraint(SpringLayout.NORTH, dispConnectionState, 6, SpringLayout.SOUTH, dispCallId);
		frmWebApiTester.getContentPane().add(dispConnectionState);
		dispConnectionState.setColumns(10);
		
		JLabel lblNetworkType = new JLabel("Network Type");
		springLayout.putConstraint(SpringLayout.NORTH, lblNetworkType, 18, SpringLayout.SOUTH, lblConnectionState);
		springLayout.putConstraint(SpringLayout.EAST, lblNetworkType, 0, SpringLayout.EAST, lblMobileClientId);
		frmWebApiTester.getContentPane().add(lblNetworkType);
		
		dispNetworkType = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, dispNetworkType, 6, SpringLayout.SOUTH, dispConnectionState);
		springLayout.putConstraint(SpringLayout.WEST, dispNetworkType, 0, SpringLayout.WEST, txtNewCallerId);
		frmWebApiTester.getContentPane().add(dispNetworkType);
		dispNetworkType.setColumns(10);
		
		JLabel lblNetworkQuality = new JLabel("Network Quality");
		springLayout.putConstraint(SpringLayout.NORTH, lblNetworkQuality, 14, SpringLayout.SOUTH, lblNetworkType);
		springLayout.putConstraint(SpringLayout.EAST, lblNetworkQuality, 0, SpringLayout.EAST, lblMobileClientId);
		frmWebApiTester.getContentPane().add(lblNetworkQuality);
		
		dispNetworkQuality = new JTextField();
		springLayout.putConstraint(SpringLayout.NORTH, dispNetworkQuality, 6, SpringLayout.SOUTH, dispNetworkType);
		springLayout.putConstraint(SpringLayout.WEST, dispNetworkQuality, 5, SpringLayout.EAST, lblNetworkQuality);
		frmWebApiTester.getContentPane().add(dispNetworkQuality);
		dispNetworkQuality.setColumns(10);
	}
}
