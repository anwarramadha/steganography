# from bitstring import BitArray
import binascii
import io
import sys
import readline
readline.parse_and_bind("tab: complete")

class LSB_Hide_Message:

	def __init__(self, imagePath):
		self.imagePath = imagePath
		self.imageName = imagePath[:imagePath.index('.')]
		self.imageBin = []
		self.messageBin = []
		self.STX = "\x02" 		# the message start with STX to check if the image has secret message 
		self.ETX = "\x03"		# ETX used to sign the end of message, so the iteration will stop
		self.startIndex = 60	# Insertion start from index 60, because if you insert from 0, the image will corrupt 

	def extractBytes(self):
		# open image and save image as binary
		with open(self.imagePath, "rb") as imageFile:
			f = imageFile.read()
			b = bytearray(f)

			imageFile.close()

			for byte in b:
				self.imageBin.append(bin(byte)[2:].zfill(8))


	def getHiddenData(self):

		print("Enter Secret Message : ")
		message = self.STX
		message = message + raw_input()
		message = message + self.ETX

		messageByte = bytearray(message.encode('UTF-8'))	# convert image to binary

		for char in messageByte:
			self.messageBin.append(bin(char)[2:].zfill(8))


	def compressImage(self):

		imageInDecimal = []

		for byte in self.imageBin:
			imageInDecimal.append(int(byte, 2))

		try:

			data = bytearray(imageInDecimal)
			with open(self.imageName + "-stego.bmp", "wb") as out_file:
				out_file.write(data)
				out_file.close()

			return True
		except Exception, e:
			print(str(e))
			return False

	def canUsed(self, bin):
		return int(bin, 2) <= 254	# this program use 8 bit data, so if there are negative number, it will convert to positive
									# such as if you have -1 then it will be 255. It will broke the image.

	def hide_message(self):
		try:
			self.extractBytes()
		except:
			return False

		self.getHiddenData()

		counter = self.startIndex

		# insert all message character to 1 digit image's LSB.
		# you can use 2 digits or 3 digits, but it will make the big differences with original image.
		for byte in self.messageBin:

			bitArray = str(byte.encode('UTF-8'))

			i = 0
			while i < len(bitArray):

				if self.canUsed(self.imageBin[counter]):

					self.imageBin[counter] = self.imageBin[counter][:1] + str(bitArray[i])	# insert bit per bit of message char to image by replacing
																							# the LSB digit

					i += 1

				counter += 1

		if self.compressImage():
			return True
		return False

	def extract_message(self):
		try:
			self.extractBytes()
		except:
			return False

		counter = 0
		checkStx = True
		resultInBinary = ""
		result = ""
		i = self.startIndex

		while i < len(self.imageBin):
			if self.canUsed(self.imageBin[i]):
				if counter == len(self.imageBin[i]):
					resultInChar = chr(int(resultInBinary, 2))

					if checkStx:
						if resultInChar != self.STX:
							return False

						checkStx = False

					else:
						if resultInChar != self.ETX:
							result = result + resultInChar
						else :
							return result;

					resultInBinary = ""
					counter = 0
					i -= 1

				else :
					resultInBinary = resultInBinary + self.imageBin[i][-1]
					counter += 1
			
			i += 1


if __name__ == '__main__':

	while True:
		print("Select option")
		print("1. Create hidden message")
		print("2. Open hidden message")
		print("3. Exit")
		sys.stdout.write("Your choice : ")
		choice = input()

		if choice == 1 :
			sys.stdout.write("Open image (only bmp image can be used) : ")
			filename = raw_input()
			lsb = LSB_Hide_Message(filename)
			if lsb.hide_message():
				print("\n=======================================================")
				print("Secret image created successfully")
			else:
				print("Failed to create secret image")
		elif choice == 2 :
			sys.stdout.write("Open secret image (only bmp image can be used) : ")
			filename = raw_input()
			lsb = LSB_Hide_Message(filename)
			message = lsb.extract_message()

			if not(message):
				print("\n=======================================================")
				print("This message is not contain any message")
			else :
				print("\n=======================================================")
				new_msg = ""
				counter1 = 0
				counter2 = 46

				for i in range(0, len(message)/46 + 1):
					new_msg = new_msg + message[counter1:counter2] + "\n"
					counter1 += 46
					counter2 += 46

				print("Secret message : \n")
				print(new_msg)

		elif choice == 3 :
			sys.exit()

		print("=======================================================\n")
