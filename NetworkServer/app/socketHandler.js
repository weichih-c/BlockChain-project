///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//	socketHandler.js
//
//	處理socket連線與資訊轉發
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
module.exports = function(io, request, clientList) {

	var INIT_PEER_PROVIDING_SIZE = 1;

	var TYPE_CONNECTION = 0x01;
	var TYPE_EVENT = 0x02;
	var EVENT_ASK_PEER = 0x101;
	var EVENT_PROVIDE_INIT_PEER = 0x102;

	io.on('connection', function(client) {

		// console.log('[INFO] ' + client.id + ' connected');
		client.emit('id', client.id);

		client.on('clientMessage', function (details) {
			console.log("relay client message");
			// console.log(details);
			var otherClient = io.sockets.connected[details.payload.to];	// 把otherClient設為訊息的收件人
			// console.log(otherClient);

			if (!otherClient){
				return;
			}
			delete details.to;
			details.from = client.id;	// 發送來源設為發出的client id
			otherClient.emit('clientMessage', details);	// 對收件人emit訊息
		});


		function leave() {
			var leaveClient = clientList.getClientByID(client.id);
			// console.log('[INFO] ' + client.id + ' left');
			console.log('[INFO] ' + leaveClient.name + ' left');
			clientList.update(client.id, false);

			// 檢查是哪個client斷線了
			// var onlineList = clientList.getClients();

			// 廣播告訴所有client某人斷線
			// 
			// if(onlineList != null){
			// 	for (var i = 0; i < onlineList.length; i++) {
			// 		if(onlineList[i] != null && onlineList[i].id == client.id){
			// 			var customDeviceID = onlineList[i].CustomDeviceID;
			// 			sendBroadcastMessage(onlineList[i].name, "leave", customDeviceID);
			// 			break;
			// 		}
			// 	};	// end for
			// }

			// clientList.removeClient(client.id);
			// console.log(client.name + "leave");

		}

		client.on('disconnect', leave);
		client.on('leave', leave);

		
		client.on('serverMessage', function (details) {
			// console.log("listen server message");
			var systemCode = details.payload.systemCode;
			if(systemCode == TYPE_CONNECTION)
			{
				handleConnected(details.payload);


			}else{
				var event = details.payload.event;
				if(event == EVENT_ASK_PEER){
					provideInitPeers();

				}
				// handleForward(details.payload);
			}
		});


		/**
		 *	處理連線事件, 當client連上線時要回傳連線成功,
		 */
		function handleConnected(payload){
			var name = payload.name;
			var customDeviceID = payload.CustomDeviceID;

			var recordedClient = clientList.getClientByName(name);

			if(recordedClient == null){
				console.log('[INFO] ' + payload.name + " initial connected");
				clientList.addClient(client.id, payload.name, null);	// add a new client to list

			}else{
				console.log('[INFO] ' + payload.name + " reconnected");
				clientList.updateClientReconnect(recordedClient, client.id, true);	// update status online
			}
			
			// console.log(clientList.getClients());

			client.emit("connectMessage", {"type" : "connected_success"});	// 回應子機連線成功
			
		}	// end function handleConnected

		function provideInitPeers(){
			var list = clientList.getClients();
			var peerList = [];
			var randArray = shuffle(list);
			for(var a=0; a<INIT_PEER_PROVIDING_SIZE; a++){
				peerList[a] = randArray[a];
			}

			var payload = {"type": EVENT_PROVIDE_INIT_PEER, "peerList": peerList };	// create JSON format payload
			
			client.emit("serverMessage", payload);
		}


		// random shuffle array generator
		function shuffle(array) {
			var i = array.length,
			j = 0,
			temp;

		    while (i--) {

				j = Math.floor(Math.random() * (i+1));

				// swap randomly chosen element with current element
				temp = array[i];
				array[i] = array[j];
				array[j] = temp;

			}

			return array;
		}

	});
};
