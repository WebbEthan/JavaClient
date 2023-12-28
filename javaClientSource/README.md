This is the client package contains everything to connect to the makemaking server package.
---------------------------------------------------------
Setup
    1. Call ThreadMangager.StartThreads();
    2. Create a class that inhearites from Client
    3. Call Connect() on the client object you created
---------------------------------------------------------
Methods
    Overrideable
        - AddClient(string clientID) Called when another client joins the match you are in.
        - RemoveClient(string clientID) Called when another client leaves the match you are in.
        - OnAuthentication() Called when the client successfully authenticates on the server.
        - OnMatchJoin() Called when the client joins a match.
        - OnMatchKicked() Called when you are kicked from a match.
        - OnDisconnect() Called when the client disconnects from the server.
    Runnable
        - JoinMatch(string code) Call this to join a match pass the match code for a specific match
        or leave blank for a random match.
        - CreateMatch() Call this to create a new match on the server.
        - LeaveMatch() Call this to leave the current match the client is in.
        - Disconnect() Call this to disconnect from the server
        - SendData(Packet packet, ProtocolType protocolType) Used to send data to server pass in the Packet
        and set protocolType to either TCP or UDP.
----------------------------------------------------------
Sending Data
    - Create a new instance of Packet with a byte passed in
        * initial byte corolates to what method set on the recieving end will be run.
    - use the Write method to pass values into the packet
    - call SendData on your client object