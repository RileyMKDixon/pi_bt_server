#Author: Riley Dixon
#Description: A python implementation of a bluetooth server for use to
#			 connecting with an Android phone for our ECE492 capstone
#			 project.
#Acknowledgements: The base server implementation is based off of the
#				example code provided by the official Linux based
#				repossitory for bluetooth connectivity.
#				URL:www.github.com/pybluez/pybluez/blob/master
#

from  bluetooth import *
import traceback

class BluetoothServer:
	
	BUFFER_SIZE = 2048
	
	def __init__(self, name):
		self.server_sock = BluetoothSocket(RFCOMM)
		self.server_sock.bind(("", PORT_ANY))
		self.client_sock = None
		self.client_info = None
		
		self.uuid = "efd4d135-d043-4fca-b99e-c2ae5ece6471"
		self.deviceName = name
		self.isConnected = False
		self.Port = None

	def waitForConnection(self):
		self.server_sock.listen(1)
		self.Port = self.server_sock.getsockname()[1]
		advertise_service(self.server_sock, self.deviceName,
						  service_id = self.uuid,
						  service_classes = [self.uuid, SERIAL_PORT_CLASS],
						  profiles = [SERIAL_PORT_PROFILE])
		
		print("Waiting for attempted connection...")
		print("Looking on RFCOMM channel: " + str(self.Port))
		
		#next call is blocking
		self.client_sock, self.client_info = self.server_sock.accept()
		print("Client connected: " + str(self.client_info))
		self.isConnected = True
	
	def closeConnection(self):
		print("Closing connection")
		self.client_sock.close()
		self.server_sock.close()
		self.client_info = None
		self.isConnected = None
		self.Port = None
	
	def read(self):
		pass
		
	def write(self, msgToSend):
		pass

while(True):
	newServer = BluetoothServer("Smart AVL")
	newServer.waitForConnection()
	while(True):
		try:
			bytesReceived = newServer.client_sock.recv(newServer.BUFFER_SIZE)
			stringReceived = bytesReceived.decode(sys.stdout.encoding)
			if(len(stringReceived) == 0):
				break
			print("Client sent: " + str(stringReceived))
			newServer.client_sock.send(stringReceived.encode(sys.stdout.encoding))
		except BluetoothError as bte:
			print("Bluetooth error occured")
			traceback.print_tb(bte.__traceback__)
			break;
			
	newServer.closeConnection()

			
	
	
