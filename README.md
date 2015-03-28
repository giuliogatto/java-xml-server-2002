# java-xml-server-2002
Java Xml server for multiplayer Flash Actionscript game
based on java.net package and threads
warning: this code was developed in 2002 

The server waits on port 8080 for new client to connect. 
Each time a client connects the list of players is updated and broadcasted to all clients.
A player can then invite another player for a private game, if the second player accepts they are removed from the list of the players.
