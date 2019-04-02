from btServer import BluetoothServer
import time
import threading


class ReadThread(threading.Thread):

	def read(self):
		stringRecv = self.btSock.read()
		if(stringRecv is not None):
			print("Read from Android: " + stringRecv)
		time.sleep(0.1) #a hack so we dont busy wait

	def __init__(self, btSock):
		threading.Thread.__init__(self)
		self.continueRunning = True
		self.btSock = btSock

	def run(self):
		while self.continueRunning:
			self.read()



class WriteThread(threading.Thread):

	def write(self, stringToPut):
		self.btSock.write(stringToPut)

	def __init__(self, btSock):
		threading.Thread.__init__(self)
		self.continueRunning = True
		self.btSock = btSock

	def run(self):
		while self.continueRunning:
			passThisString = input("String to send: ")
			self.write(passThisString)

newServer = BluetoothServer()
newServer.start()

while(not newServer.isConnected):
	time.sleep(0.5) #admittedly a hack to force the system to wait until a connection
					#has been established

reader = ReadThread(newServer)
writer = WriteThread(newServer)

reader.start()
writer.start()

#We should never get passed this point as it should just loop forever
reader.join()
writer.join()
