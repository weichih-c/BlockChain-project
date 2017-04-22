///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//	socketHandler.js
//
//	處理socket連線與資訊轉發
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
module.exports = function(io, request, clientList) {

	var TYPE_CONNECTION = 0x01;

	io.on('connection', function(client) {

		console.log('[INFO] ' + client.id + ' connected');
		client.emit('id', client.id);

		client.on('message', function (details) {
			var otherClient = io.sockets.connected[details.to];	// 把otherClient設為訊息的收件人

			if (!otherClient){
				return;
			}
			delete details.to;
			details.from = client.id;	// 發送來源設為發出的client id
			otherClient.emit('message', details);	// 對收件人emit訊息
		});


		function leave() {
			console.log('[INFO] ' + client.id + ' left');

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

			clientList.removeClient(client.id);
		}

		client.on('disconnect', leave);
		client.on('leave', leave);

		
		client.on('mqttMessage', function (details) {
			var type = details.payload.type;
			if(type == MSG_TYPE_EVENT)
			{
				handleConnected(details.payload);
			}else{
				// handleForward(details.payload);
			}
		});


		/**
		 *	處理連線事件, 當client連上線時要回傳連線成功,
		 */
		function handleConnected(payload){
			var name = payload.name;
			var customDeviceID = payload.CustomDeviceID;
			clientList.addClient(client.id, payload.name, null, customDeviceID);	// add a stream to list

			client.emit("connectMessage", {"type" : "connected_success"});	// 回應子機連線成功
			
		}	// end function handleConnected

	});
};
