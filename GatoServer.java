//
// JAVA XML SERVER FOR MULTIPLAYER FLASH ACTIONSCRIPT GAME
// based on java.net package and threads
// warning: this code was developed in 2002 
//

// import packages
import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import org.xml.sax.*;
import org.jdom.*;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.*;

// server class
public class GatoServer {

	// list of connected clients
	public Vector clients = new Vector();  

	// constructor
	public GatoServer () throws IOException {
		ServerSocket server = new ServerSocket (8080);
		System.out.println("Server started on port 8080");
		
		// waits for a connection on port 8080 and starts a new thread for each new client
		while (true) {

			Socket client = server.accept ();			
			GatoThread c=new GatoThread (this, client);
			clients.addElement(c);
			//listaClients();

		}
	}

	/////////////////////////////////////////////////////////////
	// function to send a list of available clients (players) to all the players

	public void listaClients() {

	 		Enumeration enum = clients.elements();
			//int lunghezzalista=clients.size();
			int lunghezzalista=0;
			String lungh = String.valueOf(lunghezzalista); 
			
			Document listaCl = new Document (new Element ("C"));
			Element numcli=  new Element("N");
			numcli.setText(lungh);
			//listaCl.getRootElement().setAttribute ("numcli", lungh);
			Element root=listaCl.getRootElement();
			root.addContent(numcli);
			System.out.println ("listaCl: "+listaCl);
			// prima costruisco la lista in formato xml
			System.out.println ("listaclients,lunghezza: "+clients.size());
		        while (enum.hasMoreElements()) {
		            GatoThread c = (GatoThread)enum.nextElement();
		            
					// creo la lista dei clients che non siano impegnati in partite
					if (c.ingioco<1)
						{
						lunghezzalista++;
						Element clia =new Element("P");
						clia.setText(c.nome);
						root.addContent(clia);
						System.out.println (c+" con nome:"+c.nome);
						}
		        }
			// riaggiorno il numero clients disponibili nella lista
			lungh = String.valueOf(lunghezzalista); 
			numcli.setText(lungh);
			// ora spedisco la lista a tutti i clients
			Enumeration spedi = clients.elements();
			while (spedi.hasMoreElements()) {
				GatoThread c = (GatoThread)spedi.nextElement();
				System.out.println("spedisco a:"+c.nome);
				c.invia(listaCl);
			}
	}

	/////////////////////////////////////////////////////////////
	// function to remove a client(player) from the list

	public void eliminaClient(GatoThread clientElim) {

	    clients.removeElement(clientElim);
		listaClients();
		

	}

	/////////////////////////////////////////////////////////////
	// function to insert a client(player) back in the list

	public void tornaInLista(String ritornante){

			Enumeration ritorn = clients.elements();
			while (ritorn.hasMoreElements()) {
				GatoThread c = (GatoThread)ritorn.nextElement();
				
				if (c.nome.equals(ritornante))
				{
					
					c.ingioco=0;
					System.out.println("ritorno in lista:"+c.nome);
				
				}
							
			}

			listaClients();
			System.out.println("ho appena stampato la lista clients!");
	}

	/////////////////////////////////////////////////////////////
	// function to remove a client(player) from the list

	public void togliDaLista(String sfidante, String accettante) {

			Enumeration elimin = clients.elements();
			while (elimin.hasMoreElements()) {
				GatoThread c = (GatoThread)elimin.nextElement();
				
				if (c.nome.equals(sfidante))
				{
					//clients.removeElement(c);
					c.ingioco=1;
					System.out.println("elimino dalla lista sfidante:"+c.nome);
				
				}
				if (c.nome.equals(accettante))
				{
					//clients.removeElement(c);
					c.ingioco=1;
					System.out.println("elimino dalla lista accettante:"+c.nome);
				}
				
			}
			listaClients();
			System.out.println("ho appena stampato la lista clients!");


	}

	
	////////////////////////////////////////////////////////////
	// function to send an Xml file to a single client(player) 


	public void inviaSingolo(String accettante, Document xmlDoc){
		
			Enumeration spedit = clients.elements();
			while (spedit.hasMoreElements()) {
				GatoThread c = (GatoThread)spedit.nextElement();
				String controlloachi=c.nome;
				System.out.println("spedisco a:"+controlloachi);
				if (controlloachi.equals(accettante))
				{
					c.invia(xmlDoc);
				}
			}


	}

	////////////////////////////////////////////////////////////
	// MAIN function

	public static void main (String args[]) throws IOException {
		
		new GatoServer ();
		
	}

}

/////////////////////////////////////////////////////////////
// thread class, started from server for each client connected

class GatoThread extends Thread {

	 
  private Socket socket;
  private BufferedReader in;
  private PrintWriter out;
  private Thread thrThis; 
  private GatoServer server;
  protected InputStream i;
  protected OutputStream o;
  public volatile boolean thisThreadRunning;
  StringReader perxml;
  int ingioco;
  String messaggio;
  String nome;
	 

	  public GatoThread(GatoServer server, Socket s) throws IOException 
			
			{
				System.out.println ("ciao, io sono il client  "+s);
				s.setTcpNoDelay(true);
				System.out.println ("valore di getTcpNoDelay(); "+s.getTcpNoDelay());
				socket=s;
				this.server=server;

			try {
		     
				 //i=socket.getInputStream();
				 o=	socket.getOutputStream();
				 in = 
					 new BufferedReader(
					 new InputStreamReader(
						  socket.getInputStream()));
				  // Enable auto-flush:
				 out = 
					 new PrintWriter(
				     new BufferedWriter(
					 new OutputStreamWriter(
						  socket.getOutputStream())), true);
				    // se per caso viene lanciata una eccezione
				    // sarà il chiamante ad essere responsabile 
				    // di gestirla e chiudere il socket
			 } catch(IOException ioe) {

					System.out.println("trovata eccezione");
					eliminaClient();
			 }
			  		 
			  start(); 
			  
	  }

		
	  
	   public void run() {

	        try {

				SAXBuilder saxb = new SAXBuilder(false); //get a SAXBuilder
				Document xmlDoc;   
	            char charBuffer[] = new char[1];
	            
	            while(in.read(charBuffer,0,1) != -1) {

	                StringBuffer stringBuffer = new StringBuffer(8192);
	                while(charBuffer[0] != '\0') 
						{
	                    stringBuffer.append(charBuffer[0]);
	                    in.read(charBuffer, 0 ,1);
	                }

					messaggio=stringBuffer.toString();
					perxml= new StringReader(messaggio);
					try
							{
								//get the doc
								xmlDoc = saxb.build(perxml); 

								//process it
								process(xmlDoc);
							}
							catch (JDOMException ex)
							{
								System.out.println ("JDOM XCeption");
						
							}

					
	            }
	        } catch(IOException ioe) {

	           	// found and exception

	        } finally {

	            eliminaClient();

	        }

	    }

	///////////////////////////////////////////////////////////////////////
	/////  xml  processing function

	protected void process (Document xmlDoc) {
			
		//System.out.println(xmlDoc);
		String primotag = xmlDoc.getRootElement().getName();
		Element el = xmlDoc.getRootElement();
		
		if (primotag.equals("S"))
		{
			System.out.println("messaggio per il server");
			
			String register= el.getAttributeValue("register");
			if (register.equals("yes"))
			{
				String name= el.getAttributeValue("name");
				System.out.println("registro nuovo utente, name="+name);
				this.nome=name;
				server.listaClients();
			}
			if (register.equals("sfida"))
			{
				// trattasi di una richiesta di sfida!
				List concorrenti=el.getChildren();
				Element sfidant=(Element)concorrenti.get(0);
				String sfidante= sfidant.getText();
				Element accett=(Element)concorrenti.get(1);
				String accettante= accett.getText();
				System.out.println("sfida da "+sfidante+" a "+accettante);
				// rimbalziamo al giusto client la domanda (tramite server)
				server.inviaSingolo(accettante,xmlDoc);

			}
			if (register.equals("accetto"))
			{
				// la sfida è stata accettata, messaggio per lo sfidante
				List concorrenti=el.getChildren();
				Element sfidant=(Element)concorrenti.get(0);
				String sfidante= sfidant.getText();
				Element accett=(Element)concorrenti.get(1);
				String accettante= accett.getText();
				System.out.println("sfida accettata "+sfidante+" a "+accettante);
				// rimbalziamo al giusto client la domanda (tramite server)
				server.inviaSingolo(sfidante,xmlDoc);

			}
			if (register.equals("eliminaci"))
			{
				// rimuovere i players dalla lista
				List concorrenti=el.getChildren();
				Element sfidant=(Element)concorrenti.get(0);
				String sfidante= sfidant.getText();
				Element accett=(Element)concorrenti.get(1);
				String accettante= accett.getText();
				System.out.println("adesso elimino dalla lista d'attesa "+sfidante+" e "+accettante);
				// rimbalziamo al giusto client la domanda (tramite server)
				server.togliDaLista(sfidante,accettante);

			}
			if (register.equals("ritorno"))
			{
				// ritorno in lista
				List concorrenti=el.getChildren();
				Element ritornant=(Element)concorrenti.get(0);
				String ritornante= ritornant.getText();
				System.out.println("adesso rientra in lista: "+ritornante);
				// rimbalziamo al giusto client la domanda (tramite server)
				server.tornaInLista(ritornante);

			}


		}
		if (primotag.equals("C"))
		{
			List concorrenti=el.getChildren();
			Element avversar=(Element)concorrenti.get(0);
			String avversario= avversar.getText();
			server.inviaSingolo(avversario,xmlDoc);
			System.out.println("messaggio per un client singolo (nella partita)");
		}

	}

	///////////////////////////////////////////////////////////////////////

	// function to remove a client 

	public void eliminaClient() {
	        
	       
	        try {
				server.eliminaClient(this);
				System.out.println(this+"client eliminato!"+socket);
				//i.close();
	            in.close();
	            out.close();
	            socket.close();

	            // stop the thread safely
	            return;
				
	            
	        } catch (IOException ioe) {

	           System.err.println("IO Exception");

	        }       
	    }

	///////////////////////////////////////////////////////////////////////

	// function to send an Xml document

	public void invia(Document xmlDoc) {
	        
	       
	       try
				{
					synchronized (o) {
						XMLOutputter xmlOut = new XMLOutputter ();
						xmlOut.output(xmlDoc, o) ;
						o.write (0);
						o.flush();
					}
				}
				catch (IOException ex)
				{

					eliminaClient();

				}
	    
	    }





}
