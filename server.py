#Author: Riley Dixon
#Description: A python implementation of a bluetooth server for use to
#			 connecting with an Android phone for our ECE492 capstone
#			 project.
#Acknowledgements: The base server implementation is based off of the
#				example code provided by the official Linux based
#				repossitory for bluetooth connectivity.
#				URL:www.github.com/pybluez/pybluez/blob/master
#

from bluetooth import *
import traceback
import threading
import os
import queue
import copy
import sys
import time
import select

class BluetoothServer(threading.Thread):
	
	BUFFER_SIZE = 2048

	class WriteThread(threading.Thread):
		def stringPresent(self):
			return True if self.stringToSend is not None else False

		def asyncWrite(self):
			with self.stringAvailable:
				self.stringAvailable.wait_for(self.stringPresent)
				if(self.continueRunning): #If false, skip as stopRunning() has been called.
					self.stringSemaphore.acquire()
					self.client_sock.send(self.stringToSend.encode(sys.stdout.encoding))
					self.stringToSend = None
					self.stringSemaphore.release()

		#This is the function to be called externally.
		#asyncWrite is called internally by the thread.
		def write(self, passedString):
			self.stringSemaphore.acquire()
			self.stringToSend = copy.deepcopy(passedString)
			self.stringSemaphore.release()
			with self.stringAvailable:
				self.stringAvailable.notify()


		def __init__(self, client_sock):
			threading.Thread.__init__(self)
			self.continueRunning = True
			self.stringAvailable = threading.Condition()
			self.stringSemaphore = threading.BoundedSemaphore(1)
			self.client_sock = client_sock
			self.stringToSend = None


		def run(self):
			while self.continueRunning:
				self.asyncWrite()

		def stopRunning(self):
			self.continueRunning = False #Allow our thread to exit normally
			try:
				with self.stringAvailable: #In case we are currently blocked, unblock
					self.stringToSend = ""
					self.stringAvailable.notify()
			except RuntimeError:
				print("Failed to notify lock") #If runtime error this is okay as it means the lock
					#was never acquired.
			
	
	#class ReadThread(threading.Thread):
	#Like WriteThread, this function is too be called internally only
	def asyncRead(self):
		with self.queueNotFullCV:
			self.queueNotFullCV.wait_for(self.queueNotFull)
			recvList,_,_ = select.select([self.client_sock], [], [], 2)#2sec timeout
			if len(recvList) != 0:
				bytesReceived = self.client_sock.recv(2048)
				stringReceived = bytesReceived.decode(sys.stdout.encoding)
				self.RWqueue.put(stringReceived)
				print("RT: " + stringReceived)
			
	def queueNotFull(self):
		return not self.RWqueue.full()

	def read(self):
		if self.RWqueue.empty():
			result = None
		else:
			result = self.RWqueue.get()
		return result
		
	def write(self, msgToSend):
		if(self.writer is None):
			raise TypeError("Writer not initialized. Has BluetoothServer been started?")
		if(not isinstance(msgToSend, str)):
			raise TypeError("Passed Variable must be of type String")
		self.writer.write(msgToSend)


	#def run(self):
	#	while self.continueRunning:
	#		self.read()

	#def stopRunning(self):
	#	raise SystemExit

	#-------------END INNER CLASSES----------------

	def __init__(self):
		threading.Thread.__init__(self)
		self.server_sock = BluetoothSocket(RFCOMM)
		self.server_sock.bind(("", PORT_ANY))
		self.client_sock = None
		self.client_info = None
		
		self.uuid = "efd4d135-d043-4fca-b99e-c2ae5ece6471"
		self.deviceName = "SmartAVL-RaspPi"
		self.isConnected = False
		self.Port = None

		self.RWqueue = queue.Queue()
		self.continueRunning = True
		self.queueNotFullCV = threading.Condition()
		self.writer = None

	
	def run(self):
		while(self.continueRunning):
			self.waitForConnection()
			#self.reader = BluetoothServer.ReadThread(self.RWqueue, self.client_sock)
			self.writer = BluetoothServer.WriteThread(self.client_sock)
			self.writer.start()
			while(self.isConnected and self.continueRunning):
				try:
					self.asyncRead()
				except BluetoothError as bte:
					print("Bluetooth Error Occurred")
					traceback.print_tb(bte.__traceback__)
					self.isConnected = False
			if(self.continueRunning): #If we want to stop running, we have already closed everything.
				self.closeConnection()
		print("End main Thread run()")


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
		self.client_sock.close()
		self.server_sock.close()
		self.writer.stopRunning()
		self.writer.join()
		self.client_info = None
		self.isConnected = False
		self.Port = None
		self.writer = None

	def stopServer(self):
		self.continueRunning = False
		self.closeConnection()
	

newServer = BluetoothServer()
newServer.start()

while(not newServer.isConnected):
	time.sleep(0.5) #admittedly a hack to force the system to wait until a connection
					#has been established
try:
	for i in range(1,10):
		newServer.write(str(i))
except Exception:
	print("Oops")
finally:
	#newServer.closeConnection()
	print("Stopping Server")
	newServer.stopServer()
	
newServer.join()
print("Closed")

# newServer = BluetoothServer()
# newServer.start()
# while(True):
# 	newServer.waitForConnection()
# 	while(newServer.isConnected):
# 		try:
# 			bytesReceived = newServer.client_sock.recv(newServer.BUFFER_SIZE)
# 			stringReceived = bytesReceived.decode(sys.stdout.encoding)
# 			if(len(stringReceived) == 0):
# 				break
# 			print("Client sent: " + str(stringReceived))
# 			newServer.client_sock.send(stringReceived.encode(sys.stdout.encoding))
# 		except BluetoothError as bte:
# 			print("Bluetooth error occured")
# 			traceback.print_tb(bte.__traceback__)
# 			newServer.isConnected = False
			
# 	newServer.closeConnection()

			
	
	
