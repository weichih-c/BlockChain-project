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
  var Client = function(id, name, pubKeyHashAddress, isOnline) {
	this.name = name;
	this.id = id;
	this.pubKeyHashAddress = pubKeyHashAddress;
	this.isOnline = isOnline;
  }

  return {
	addClient : function(id, name, pubKeyHashAddress) {
	  var client = new Client(id, name, pubKeyHashAddress, true);
	  clientList.push(client);
	},

	removeClient : function(id) {
	  for(var i = (clientList.length - 1); i >= 0; i--)
	  {
		if(clientList[i].id == id)
			clientList.splice(i, 1);
	  }
	},

	// update the isOnline status of a specific name client
	update : function(name, isOnline) {
	  var client = clientList.find(function(element, i, array) {
		return element.name == name;
	  });
	  client.isOnline = isOnline;
	},

	getClients : function() {
	  return clientList;
	},

	getClientByName : function(name) {
		var client = clientList.find(function(element, i, array) {
			return (element.name == name)
		});
		return client;
	},
  }
};
