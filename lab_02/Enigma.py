from random import sample, randint
import numpy as np

alphabet = np.arange(256)
alphabet = np.ubyte(alphabet)

def generate_reflector():
    indexes = np.arange(256)
    for i in range(len(alphabet) // 2):
        ind = randint(1, len(indexes) - 1)
        alphabet[indexes[0]], alphabet[indexes[ind]] = alphabet[indexes[ind]], alphabet[indexes[0]]
        indexes = np.delete(indexes, ind)
        indexes = np.delete(indexes, 0)

rotor1 = np.fromfile("rotor1.txt", dtype=np.dtype('B'), sep=',')
rotor1Shift = 0

rotor2 = np.fromfile("rotor2.txt", dtype=np.dtype('B'), sep=',')
rotor2Shift = 0

rotor3 = np.fromfile("rotor3.txt", dtype=np.dtype('B'), sep=',')
rotor3Shift = 0

reflector = np.fromfile("reflector.txt", dtype=np.dtype('B'), sep=',')

def rotate():
    global rotor1Shift, rotor2Shift, rotor3Shift
    
    rotor1Shift = (rotor1Shift + 1) % len(alphabet)
    
    if (rotor1Shift == len(alphabet)):
        rotor2Shift = (rotor2Shift + 1) % len(alphabet)
        
        if (rotor2Shift == len(alphabet)):
            rotor3Shift = (rotor3Shift + 1) % len(alphabet)


fileName = input()

try:
    with open(fileName, "rb") as f:
        inputFile = np.fromfile(f, np.dtype('B'))
    print(inputFile)
except IOError:
    print('Error While Opening the file!')

result = np.array([], np.dtype('B'))

for byte in inputFile:
    rotate()

    r1Output = rotor1[(byte + rotor1Shift) % len(alphabet)]
    r2Output = rotor2[(r1Output - rotor1Shift + rotor2Shift) % len(alphabet)]
    r3Output = rotor3[(r2Output - rotor2Shift + rotor3Shift) % len(alphabet)]

    reflecOutput = reflector[(r3Output - rotor3Shift) % len(alphabet)]

    r3BackOutput = np.where(rotor3 == (reflecOutput + rotor3Shift) % len(alphabet))[0][0]
    r2BackOutput = np.where(rotor2 == (r3BackOutput - rotor3Shift + rotor2Shift) % len(alphabet))[0][0]
    r1BackOutput = np.where(rotor1 == (r2BackOutput - rotor2Shift + rotor1Shift) % len(alphabet))[0][0]

    result = np.append(result, (r1BackOutput - rotor1Shift) % len(alphabet))
    
print(result)

fileName = input()

try:
    result.astype('int8').tofile(fileName)
except IOError:
    print('Error While Opening the file!')

