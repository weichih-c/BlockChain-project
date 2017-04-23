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
    var list = clientList.getClients();
    // JSON exploit to clone clientList.public
    var data = (JSON.parse(JSON.stringify(list)));

    res.status(200).json(data);
  };

  var getClientByName = function(req, res) {
    var data = null;
    var client = clientList.getClientByName(req.params.name);
    // JSON exploit to clone clientList.public
    if(client)
      data = (JSON.parse(JSON.stringify(client)));

    res.status(200).json(data);
  };
  app.get('/clientList.json', displayclientList);
  app.get('/clientList.json/:name', getClientByName);
}
