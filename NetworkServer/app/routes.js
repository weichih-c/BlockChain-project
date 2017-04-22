///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
//  routes.js
//
//  負責routing的功能
//
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
module.exports = function(app, clientList) {

    // GET clientList as JSON
  var displayclientList = function(req, res) {
    var clientList = clientList.getClients();
    // JSON exploit to clone clientList.public
    var data = (JSON.parse(JSON.stringify(clientList)));

    res.status(200).json(data);
  };

  var getClient4MqttByDeviceID = function(req, res) {
    var data = null;
    var client = clientList.getClientByDeviceID(req.params.deviceID);
    // JSON exploit to clone clientList.public
    if(client)
      data = (JSON.parse(JSON.stringify(client)));

    res.status(200).json(data);
  };

  var getClient4MqttByEventID = function(req, res) {
    var data = null;
    var client = clientList.getClientByEventID(req.params.eventID, req.params.deviceID);
    // JSON exploit to clone clientList.public
    if(client)
      data = (JSON.parse(JSON.stringify(client)));

    res.status(200).json(data);
  };
  app.get('/clientList.json', displayclientList);
  app.get('/clientList.json/:deviceID', getClient4MqttByDeviceID);
  app.get('/clientList.json/:eventID/:deviceID', getClient4MqttByEventID);
}
