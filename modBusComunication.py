#!/usr/bin/env python
import minimalmodbus
#
import numpy as np
import serial.tools.list_ports


# instrument.serial.port                     # this is the serial port name
# instrument.serial.bytesize = 8
# instrument.serial.parity   = serial.PARITY_NONE
# instrument.serial.stopbits = 1
# instrument.serial.timeout  = 0.05          # seconds

def listar_portas_seriais():
    # Lista as portas seriais disponíveis no PC atual
    portas = list(serial.tools.list_ports.comports())
    # Cria uma lista com os nomes das portas seriais
    nomes_portas = [porta.device for porta in portas]
    return nomes_portas


def read_Input_Registers(instrument):
    # EndereçoDeMemória -> 00 C0 -> 192
    leituraDados = instrument.read_register(registeraddress=192)
    print(f"registros de entrada(decimal): {leituraDados}")
    input_reg = decimal_para_vetor(leituraDados)
    print(f"registros de entrada(binario): {input_reg}")
    return input_reg


def write_Output_Registers(output_reg, instrument):
    # Conversão do vetor(binario) para decimal(valor)
    valorWrite = vetor_para_decimal(output_reg)
    # EndereçoDeMemória -> 00 70 -> 112
    instrument.write_register(registeraddress=112, value=valorWrite)


def vetor_para_decimal(vetor):
    # Converte o vetor em uma string binária
    binario = ''.join([str(bit) for bit in vetor])
    # Converte a string binária em um número decimal
    decimal = int(binario, 2)
    return decimal


def decimal_para_vetor(decimal):
    # Converte o número decimal em uma string binária de 8 bits
    binario = '{0:08b}'.format(decimal)
    # Converte a string binária em um vetor de inteiros
    vetor = [int(bit) for bit in binario]
    return vetor


def obterInstrumento(portaSerialSelecionada):
    # port name, slave address (in decimal)
    instrument = minimalmodbus.Instrument(portaSerialSelecionada, 1)
    instrument.mode = minimalmodbus.MODE_RTU   # rtu or ascii mode
    instrument.serial.baudrate = 9600   # Baud
    instrument.clear_buffers_before_each_transaction = True
    return instrument
