#!/usr/bin/env python3

import http.server as srv
import socketserver
import time
import json
import cgi
from urllib.parse import unquote


class Player:
  def __init__(self, name):
    self.name = name
    self.team_id = None
    self.dose = 0

  def addDose(self, delta_dose):
    self.dose += delta_dose

  def isDead(self):
    return self.dose > 120

  def getStatus(self):
    return {"name": self.name, "team_id": self.team_id, "dose": self.dose, "is_dead": self.isDead()}

class Beacon:
  def __init__(self, bid):
    self.bid = bid
    self.active = True
    self.power = 1

  def deactivate(self):
    self.active = False

class GameSession:
  def __init__(self):
    self.players = []
    self.beacons = []
    self.stime = time.time()

  def start(self):
    self.stime = time.time()

  def deactivate(self, bid):
    self.beacons[bid].deactivate()

  def join(self, name, team_id):
    if len(list(filter(lambda p: p.name == name, self.players))) != 0:
      return

    player = Player(name)
    player.team_id = team_id
    self.players.append(player)

  def getStatus(self):
    status = dict()
    status["players"] = [p.getStatus() for p in self.players]
    status["cumulative_dose"] = sum([p.dose for p in self.players])
    status["active_beacons"] = len([b for b in self.beacons if b.active])
    status["beacons"] = [{"bid": b.bid, "active": b.active, "power": b.power} for b in self.beacons]
    status["time"] = 0 #time.time() - self.stime

    return json.dumps(status)

  def addDose(self, name, delta_dose):
    list(filter(lambda p: p.name == name, self.players))[0].addDose(delta_dose)

  def deactivateBeacon(self, bid):
    list(filter(lambda b: b.bid == bid, self.beacons))[0].active = False

  def addBeacon(self, bid, power=5):
    self.bid.append(bid)

  def reset(self):
    self.players = []
    self.beacons = []
    self.stime = time.time()

  def handleRequest(self, request):
    if request["what"] == "add_dose":
      self.addDose(request["name"], request["delta_dose"])
    elif request["what"] == "deactivate":
      self.deactivateBeacon(request["bid"])
    elif request["what"] == "join":
      self.join(request["name"], request["team_id"])
    elif request["what"] == "reset":
      self.reset()
    elif request["what"] == "start":
      self.start()
    elif request["what"] == "add_beacon":
      self.addBeacon(request["bid"], request["power"])
    elif request["what"] == "update":
      pass

    return self.getStatus()

session = GameSession()

class ClientRequestHandler(srv.BaseHTTPRequestHandler):
    def __init__(self, request, client_address, server):
        super(ClientRequestHandler, self).__init__(request, client_address, server)
        pass

    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        request = json.loads('{"what": "update"}')
        self.wfile.write(bytes(session.handleRequest(request), "UTF-8"))
        self.server.path = self.path
        print("get: ", self.path)

    def do_POST(self):
        self.server.path = self.path

        ctype, pdict = cgi.parse_header(self.headers.get('content-type'))
        if ctype == 'application/json':
          length = int(self.headers.get('content-length'))
          payload = self.rfile.read(length).decode("UTF-8")
          payload = unquote(payload[5:].replace("+", " "))
          print(payload)
          data = json.loads(payload)
          print(data)
        else:
          data = {"what":"update"}

        self.send_response(200)
        self.send_header("Content-Type", "application/json")
        self.end_headers()
        self.wfile.write(bytes(json.dumps(session.handleRequest(data)), "UTF-8"))
        print(data)


def run(server_class=srv.HTTPServer, handler_class=srv.BaseHTTPRequestHandler):
    server_address = ('', 8000)
    httpd = server_class(server_address, handler_class)
    httpd.serve_forever()

def main():
    run(handler_class=ClientRequestHandler)

if __name__ == "__main__":
    main()
