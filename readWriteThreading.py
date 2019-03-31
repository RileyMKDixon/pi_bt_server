import os
import threading
import queue

queueNotEmptyCV = None
queueNotFullCV = None
RWqueue = None

class ReadThread(threading.Thread):
    
    def queueNotEmpty(self):
        return not RWqueue.empty()

    def read(self):
        with queueNotEmptyCV:
            queueNotEmptyCV.wait_for(self.queueNotEmpty)
            stringToPrint = RWqueue.get()
            print("Reader: " + stringToPrint)
            with queueNotFullCV:
                queueNotFullCV.notify()

    def __init__(self):
        threading.Thread.__init__(self)
        self.continueRunning = True

    def run(self):
        while self.continueRunning:
            self.read()



class WriteThread(threading.Thread):

    def write(self, stringToPut):
        with queueNotFullCV:
            queueNotFullCV.wait_for(self.queueNotFull)
            RWqueue.put(stringToPut)
            with queueNotEmptyCV:
                queueNotEmptyCV.notify()
            
    def queueNotFull(self):
        return not RWqueue.full()


    def __init__(self):
        threading.Thread.__init__(self)
        self.continueRunning = True

    def run(self):
        while self.continueRunning:
            passThisString = input("String to send: ")
            self.write(passThisString)

RWqueue = queue.Queue()

queueNotFullCV = threading.Condition()
queueNotEmptyCV = threading.Condition()

reader = ReadThread()
writer = WriteThread()

reader.start()
writer.start()

#We should never get passed this point as it should just loop forever
reader.join()
writer.join()

