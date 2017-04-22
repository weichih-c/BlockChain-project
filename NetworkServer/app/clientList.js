///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//	clientList.js
//
//	記綠各個遠端client 的socketIO連線資訊
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

module.exports = function() {
  /**
   * available clients
   * the id value is considered unique (provided by socket.io)
   */
  var clientList = [];
  /**
   * Stream object
   */
  var Client = function(id, name, eventID, deviceID) {
	this.name = name;
	this.id = id;
	this.eventID = eventID;
	this.deviceID = deviceID;
  }

  return {
	addClient : function(id, name, eventID, deviceID) {
	  var client = new Client(id, name, eventID, deviceID);
	  clientList.push(client);
	},

	removeClient : function(id) {
	  for(var i = (clientList.length - 1); i >= 0; i--)
	  {
		if(clientList[i].id == id)
			clientList.splice(i, 1);
	  }
	},

	// update function
	update : function(deviceID, eventID) {
	  var client = clientList.find(function(element, i, array) {
		return element.deviceID == deviceID;
	  });
	  client.eventID = eventID;
	},

	getClients : function() {
	  return clientList;
	},

	getClientByDeviceID : function(deviceID) {
		var client = clientList.find(function(element, i, array) {
			return (element.deviceID == deviceID && element.eventID == null);
		});

		return client;
	},

	getClientByEventID : function(eventID, deviceID) {
		var client = clientList.find(function(element, i, array) {
			return (element.deviceID == deviceID && element.eventID == eventID);
		});
		return client;
	},

	getClientByName : function(name) {
		var client = clientList.find(function(element, i, array) {
			return (element.name == name)
		});
		return client;
	},
  }
};
