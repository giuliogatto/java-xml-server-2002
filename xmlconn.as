// ACTIONSCRIPT FUNCTIONS (AS2)
// for flash client

////////////////////////////////////////////////////////////////////////////////////////

// connect to java serversocket xml

function connect () {
	
	mySocket = new XMLSocket();
	mySocket.onConnect = handleConnect;
	mySocket.onClose = handleClose;
	mySocket.onXML = myOnXML; 
    
    // this is local IP address
	if (!mySocket.connect("192.168.2.15", "8080")) trace("connectionFailed");
	mySocket.host = host;
	mySocket.port = port;
}

//////////////////////////////////////////////////////////////////////////////////////////

// callback function for connection

function handleConnect (succeeded) {

	if(succeeded) {
		mySocket.connected = true;
		//gotoAndStop("chat");
		trace ("connessione ok");
		//Selection.setFocus("_level0.outgoing");
		if (mySocket && mySocket.connected) {
			// send a message with my name
			var registrazione="<S register='yes' name='"+_root.login+"'></N>\n";
			registraObj = new XML();
			registraObj.parseXML(registrazione);
			registraObj.removeWhitespace;
			mySocket.send(registraObj);
		}
	} else {

		gotoAndStop("connectionFailed");
		trace("connessione fallita");
	}
}

/////////////////////////////////////////////////////////////////////////////////////////

// the server has disconected us

function handleClose() {

	_root.incoming="connessione chiusa";
}


////////////////////////////////////////////////////////////////////////////////////////

// funzioni di spedizione generale 

function sendMessage(messaggio) {

	var messaggio = messaggio+'\n';	
	// FONDAMENTALE la \n alla fine perchè la funzione dal lato server in java
	// è in.readLine cioè ha bisogno di un a caporiga per leggere dal buffer!!!
	messageObj = new XML();
	messageObj.parseXML(messaggio);

	// check what we're sending
	trace("Sending: " + messageObj);

	// if a socket object has been created and is connected, send the xml message
	// otherwise warn the user that they need to connect
	if (mySocket && mySocket.connected) {
		mySocket.send(messageObj);
		//trace ("sono in send");
		
	} else {
		// the server must have kicked us off...
		incoming += "<FONT COLOR='#507373'><B>connessione saltata"
			+ "</B></FONT>\n";
		
	}
}


///////////////////////////////////////////////////////////////////////////////////////////////

// funzione di ricezione 

function myOnXML(doc) {
	
	doc.removeWhitespace;
	stripWhitespaceDoublePass(doc);	
	//doc.parseXml(doc);
	trace ("ricevuto ");
	trace ("doc="+doc);
	// devo capire che tipo di messaggio è!
	var nodenamente=doc.firstChild.firstChild.nodeName; 
	trace ("firstchild.nodeName="+nodenamente);
	if (doc.firstChild.firstChild.nodeName=="N"){
			trace("siamo in lista="+doc);
			// significa che questa è una lista dei clients in sala d'attesa
		    	rimuoviVecchiaLista();
		        clients = new Array();
			_root.clientsalattesa = int(doc.firstChild.firstChild.firstChild.toString());
			trace ("numero clients="+int (clientsalattesa));
			// mi fisso un livello iniziale di elementi da scorrere tramite nextSibling
			puntatoreiniziale=doc.firstChild.firstChild.nextSibling;
			//cancello il fantoccio iniziale
			_root.fantoccio._visible=0;
			for (z=0;z<clientsalattesa; z++)
			{
				trace ("z="+z);
				// popolo il mio array di clients
				_root.clients[z]=puntatoreiniziale;
				trace ("clients "+z+"="+_root.clients[z]);
				// faccio un passo avanti
				puntatoreiniziale=puntatoreiniziale.nextSibling;
				// creo la variabile per il campo testo
				_root["client"+(z)]=clients[z];
				//trace ("client1="+_root.client1);
                                
				_root.fantoccio.duplicateMovieClip("fantoccio"+z, z);
				_root["fantoccio"+z]._x = 50;
				_root["fantoccio"+z]._y = 200+(z*30);				
				_root["fantoccio"+z]._visible=true;
				_root["fantoccio"+z].nomcli=clients[z];	
			}
		}
	if (doc.firstChild.firstChild.nodeName=="P"){

		reg = doc.firstChild.attributes.register;
		//trace ("il nome dell attributo è: "+reg);
		if (reg=="sfida"){
			// significa che questa è una proposta per una partita
			proponente=doc.firstChild.firstChild;
			trace ("ho ricevuto una sfida da"+proponente);
			// avanti di uno
			decidente=proponente.nextSibling;
			//trace(accettante);		
			//faccio apparore la proposta a video
			_root.messaggi.messaggiosfida=proponente+" ha chiesto di giocare con lui, accetti?";
			_root.accettadigiocare._visible=yes;
			_root.siaccetta._visible=true;
			_root.noaccetta._visible=true;	
			}
		if (reg=="accetto"){
			// significa che questa è una RISPOSTA positiva per una partita
			trace ("risposta positiva ricevuta!");
			proponente=doc.firstChild.firstChild;
			decidente=proponente.nextSibling;
			// adesso bisogna avvertire il server che stiamo giocando
			// il server dovrà toglierci dalla sala d'attesa!
			_root.avvertiServer(decidente);
			_root.play();

			}
		}
		
		if (doc.firstChild.nodeName=="C"){
			mossa = doc.firstChild.attributes.mossa;
			trace("mossa fatta da avversario: "+mossa);
			_root.mettiPedina (mossa,_root.gioca_avversario);
			tuoturno=1;
		}
	
	
}
////////////////////////////////////////////////////////////////////////////////////////////////////

// funzioni di creazione messaggi Xml della sala d'attesa 

function creaProposta(decidente) {
	trace("sono in creaProposta");
	//_root.incoming+="hai chiesto a "+decidente+" di giocare.. attendi la sua risposta..";
	messaggio="<S register='sfida'><P>"+_root.login+"</P>"+decidente+"</S>";
	sendMessage (messaggio);
}
 
function creaRisposta(proponente,risposta) {
	//_root.incoming+="hai risposto a "+proponente+" "+risposta;
	messaggio="<S register='"+risposta+"'>"+proponente+"<P>"+_root.login+"</P></S>";
	sendMessage (messaggio);
}

function avvertiServer(decidente) {
	messaggio="<S register='eliminaci'>"+decidente+"<P>"+_root.login+"</P></S>";
	sendMessage (messaggio);

}

function passamossa(settore) {
	messaggio="<C mossa='"+settore+"'>"+avversario+"<P>"+_root.login+"</P></S>";
	sendMessage (messaggio);

}

function partitafinita() {
	messaggio="<S register='ritorno'><P>"+_root.login+"</P></S>";
	sendMessage (messaggio);

}


///////////////////////////////////////////////////////////////////////////////////////////////////
function rimuoviVecchiaLista(){
	for (z=0;z<clientsalattesa; z++)
			{
		removeMovieClip("fantoccio"+z);
		}
}

///////////////////////////////////////////////////////////////////////////////////////////////////
// Strips whitespace nodes from an XML document 
// by passing twice through each level in the tree

function stripWhitespaceDoublePass(XMLnode) {
  // Loop through all the children of XMLnode
  for (var i = 0; i < XMLnode.childNodes.length; i++) {
    // If the current node is a text node...
    if(XMLnode.childNodes[i].nodeType == 3) {

      // ...check for any useful characters in the node.
      var j = 0;
      var emptyNode = true;
      for(j = 0;j < XMLnode.childNodes[i].nodeValue.length; j++) {
        // A useful character is anything over 32 (space, tab, 
        // new line, etc are all below).
        if(XMLnode.childNodes[i].nodeValue.charCodeAt(j) > 32) {
          emptyNode = false;
          break;
        }
      }
		
      // If no useful charaters were found, delete the node.	
      if(emptyNode) {
        XMLnode.childNodes[i].removeNode();
      }
    }
  }

  // Now that all the whitespace nodes have been removed from XMLnode,
  // call stripWhitespaceDoublePass on its remaining children.
  for(var k = 0; k < XMLnode.childNodes.length; k++) {
    stripWhitespaceDoublePass(XMLnode.childNodes[k]);
  }
}
///////////////////////////////////////////////////////////////////////////////////////////////////