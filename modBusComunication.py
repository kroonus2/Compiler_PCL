#!/usr/bin/env python
import minimalmodbus

def array_to_decimal(array):
    binary = ''.join([str(bit) for bit in array])
    decimal = int(binary, 2)
    return decimal

def decimal_to_array(decimal):
    binary = '{0:08b}'.format(decimal)
    array = [int(bit) for bit in binary]
    return array

class Modbus:
    def __init__(self):
        self.instrument = False;
        self.input_reg = False;
    
    def get_instrument(self, port):
        self.instrument = minimalmodbus.Instrument(port, 1)
        self.instrument.mode = minimalmodbus.MODE_RTU
        self.instrument.serial.baudrate = 9600
        self.instrument.clear_buffers_before_each_transaction = True

    def read_input_registers(self):
        self.input_reg = decimal_to_array(self.instrument.read_register(192))
        return self.input_reg

    def write_output_registers(self, output_reg):
        self.instrument.write_register(112, array_to_decimal(output_reg))

