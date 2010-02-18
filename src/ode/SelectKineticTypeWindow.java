package ode;

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;

public class SelectKineticTypeWindow extends JFrame implements ActionListener
	{
	    //UI MEMBERS
	    JButton Select;
	    JScrollPane scrollPane;
	    JTextArea outputArea;
	    JComboBox selectionField;
	    JLabel askForReaction; 

	    
	    private int selectedKineticType = 1;
	    private String askedReaction="Reaction:  ";
	    
	    public SBMLsqueezerPlugin plugin;

	    //CONSTRUCTORS

	    public SelectKineticTypeWindow(String caption, String reaction)
	    	{
	    		
	    		super(caption);
	    		this.askedReaction=this.askedReaction+reaction; 
	    		buildUI();
	    		this.plugin=plugin;
	    		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);		// schlieﬂt das fenster wie dispose()
	    	};
		public SelectKineticTypeWindow(String caption, SBMLsqueezerPlugin plugin, String reaction)
	    	{
	    		super(caption);
	    		this.askedReaction=this.askedReaction+reaction; 
	    		buildUI();
	    		this.plugin=plugin;
	    		this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);		// schlieﬂt das fenster wie dispose()
	    	};
		    
	    //buid UserInterface
	    private void buildUI(){
	    	this.getContentPane().setLayout(new FlowLayout());
	        //this.getContentPane().setLayout(null);
	        this.setSize(400,180);
	        
	        this.Select = new JButton("Select");
	        selectionField = new JComboBox();
	        this.outputArea = new JTextArea("",5,35);
	        this.scrollPane = new JScrollPane(this.outputArea);
	        this.askForReaction = new JLabel(this.askedReaction);
	        
	        this.Select.setName("Select");
	        this.Select.addActionListener(this);
	        this.askForReaction.setFont( new Font("Serif", Font.PLAIN, 12) );
	        this.askForReaction.setForeground(Color.black);
	        
	        this.getContentPane().add(askForReaction);
	        this.getContentPane().add(this.scrollPane);
	        this.getContentPane().add(selectionField);
	        this.getContentPane().add(this.Select);
	        	        
	        String selectionItems[] = {
	        	      "default", "auto select", "mmk", "convenience", "no.3", "no.4",
	        	      "no.5"};
	        //add items so ui member selectionfield

	        for ( int i = 0; i < selectionItems.length; i++ )
	        	selectionField.addItem(selectionItems[i]);
	        
	        selectionField.addItemListener( new ItemListener() {
	            public void itemStateChanged( ItemEvent e ) {
	              JComboBox selectedChoice = (JComboBox)e.getSource();
	              
	              if(selectedChoice.getSelectedItem().equals("default"))
	            	  selectedKineticType=1;
	              if ( selectedChoice.getSelectedItem().equals("auto select"))
	            	  selectedKineticType=2;
	              if ( selectedChoice.getSelectedItem().equals("mmk"))
	            	  selectedKineticType=3;
	              if ( selectedChoice.getSelectedItem().equals("convenience"))
	            	  selectedKineticType=4;
	              if ( selectedChoice.getSelectedItem().equals("no.3"))
	            	  selectedKineticType=5;
	              if ( selectedChoice.getSelectedItem().equals("no.4"))
	            	  selectedKineticType=6;
	              if ( selectedChoice.getSelectedItem().equals("no.5"))
	            	  selectedKineticType=7;        
	            }
	          });

	        
	    }

	    public void actionPerformed(ActionEvent e)
	    {
	        if (((JButton)(e.getSource())).getName()=="Select")   
	        { 
	            this.select();
	        }
	    }
	    
	    private void select()
	    {//aufrufen von ExistKineticLaw mit dem Model des aktuellen models
	    	this.print("auswahl getroffen:"+this.selectedKineticType);
	    }
	    
		private void clear() {
			this.outputArea.setText("");
		}

	    public void print(String text){
	        this.outputArea.append (text+"\n");
	    }
	    
	    public void print(int text){
	        this.outputArea.append (text+"\n");
	    }

	    /* put this into its own App class, if needed*/
	    public static void main(String args[])
	    {
	        SelectKineticTypeWindow ui = new SelectKineticTypeWindow("Please select type of reaction:", " Reaktant1 + Reaktant2 ----> Produkt1");
	        ui.setVisible(true);
	    }
	}